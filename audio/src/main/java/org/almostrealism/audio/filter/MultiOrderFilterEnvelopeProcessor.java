/*
 * Copyright 2025 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.filter;

import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.collect.PackedCollection;

/**
 * A high-order low-pass filter with time-varying cutoff frequency controlled by an ADSR envelope.
 * <p>
 * This processor combines envelope generation with multi-order filtering to apply dynamic frequency
 * shaping to audio signals. The cutoff frequency varies over time according to an ADSR
 * (Attack-Decay-Sustain-Release) envelope, sweeping from 0 Hz up to a configurable peak frequency.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li><b>40th-order filtering</b>: Provides very steep frequency rolloff for precise spectral control</li>
 *   <li><b>Time-varying cutoff</b>: Cutoff frequency dynamically modulated by ADSR envelope</li>
 *   <li><b>Hardware-accelerated</b>: Operations compiled to native code via ar-common framework</li>
 *   <li><b>Memory-managed</b>: Implements {@link Destroyable} for proper resource cleanup</li>
 * </ul>
 *
 * <h3>Processing Pipeline</h3>
 * <ol>
 *   <li>ADSR envelope is evaluated to generate time-varying control signal (0.0 to 1.0)</li>
 *   <li>Control signal is scaled to cutoff frequency range (0 Hz to {@link #filterPeak})</li>
 *   <li>Multi-order low-pass filter is applied using the time-varying cutoff</li>
 * </ol>
 *
 * <h3>Typical Usage</h3>
 * <pre>
 * MultiOrderFilterEnvelopeProcessor processor = new MultiOrderFilterEnvelopeProcessor(44100, 30.0);
 * processor.setDuration(5.0);
 * processor.setAttack(0.5);
 * processor.setDecay(1.0);
 * processor.setSustain(0.7);
 * processor.setRelease(2.0);
 * processor.process(inputAudio, outputAudio);
 * processor.destroy();
 * </pre>
 *
 * @see EnvelopeProcessor
 * @see EnvelopeFeatures
 * @see FilterEnvelopeProcessor
 */
// TODO  This should implement AudioProcessor
public class MultiOrderFilterEnvelopeProcessor implements EnvelopeProcessor, Destroyable, CellFeatures, EnvelopeFeatures {
	/** Maximum cutoff frequency in Hz. Envelope peak maps to this frequency. */
	public static double filterPeak = 20000;

	/** Order of the low-pass filter. Higher values produce steeper rolloff. */
	public static int filterOrder = 40;

	private PackedCollection<?> cutoff;

	private PackedCollection<?> duration;
	private PackedCollection<?> attack;
	private PackedCollection<?> decay;
	private PackedCollection<?> sustain;
	private PackedCollection<?> release;

	private Evaluable<PackedCollection<?>> cutoffEnvelope;
	private Evaluable<PackedCollection<?>> multiOrderFilter;

	/**
	 * Constructs a new multi-order filter envelope processor.
	 * <p>
	 * This constructor pre-allocates memory for the maximum expected duration and compiles
	 * the envelope generation and filtering operations for the specified sample rate.
	 * </p>
	 *
	 * @param sampleRate  The sample rate in Hz (e.g., 44100)
	 * @param maxSeconds  Maximum duration in seconds that can be processed in a single call.
	 *                    Determines the size of internal buffers.
	 */
	public MultiOrderFilterEnvelopeProcessor(int sampleRate, double maxSeconds) {
		int maxFrames = (int) (maxSeconds * sampleRate);

		cutoff = new PackedCollection<>(maxFrames);
		duration = new PackedCollection<>(1);
		attack = new PackedCollection<>(1);
		decay = new PackedCollection<>(1);
		sustain = new PackedCollection<>(1);
		release = new PackedCollection<>(1);

		EnvelopeSection envelope = envelope(cp(duration), cp(attack), cp(decay), cp(sustain), cp(release));
		Producer<PackedCollection<?>> env =
				sampling(sampleRate, () -> envelope.get().getResultant(c(filterPeak)));

		cutoffEnvelope = env.get();
		multiOrderFilter = lowPass(v(shape(-1, maxFrames), 0),
								v(shape(-1, maxFrames), 1),
								sampleRate, filterOrder)
							.get();
	}

	/**
	 * Sets the total duration of the envelope in seconds.
	 * <p>
	 * This is the total time from the start of the attack phase to the end of the release phase.
	 * The envelope will reach 0 at this point.
	 * </p>
	 *
	 * @param duration  Total envelope duration in seconds (must be positive)
	 */
	public void setDuration(double duration) {
		this.duration.set(0, duration);
	}

	/**
	 * Sets the attack time in seconds.
	 * <p>
	 * During the attack phase, the cutoff frequency ramps up from 0 Hz to {@link #filterPeak}.
	 * The attack time is automatically clamped to 75% of the total duration.
	 * </p>
	 *
	 * @param attack  Attack time in seconds (must be positive)
	 */
	public void setAttack(double attack) {
		this.attack.set(0, attack);
	}

	/**
	 * Sets the decay time in seconds.
	 * <p>
	 * During the decay phase, the cutoff frequency drops from the peak to the sustain level.
	 * The decay time is automatically clamped to 25% of the total duration.
	 * </p>
	 *
	 * @param decay  Decay time in seconds (must be positive)
	 */
	public void setDecay(double decay) {
		this.decay.set(0, decay);
	}

	/**
	 * Sets the sustain level as a fraction of the peak cutoff frequency.
	 * <p>
	 * The sustain level determines the cutoff frequency during the sustain phase,
	 * as a proportion of {@link #filterPeak}.
	 * </p>
	 *
	 * @param sustain  Sustain level (0.0 to 1.0, where 1.0 = full {@link #filterPeak})
	 */
	public void setSustain(double sustain) {
		this.sustain.set(0, sustain);
	}

	/**
	 * Sets the release time in seconds.
	 * <p>
	 * During the release phase, the cutoff frequency ramps down from the sustain level to 0 Hz.
	 * The release begins at the envelope duration and extends backward by the release time.
	 * </p>
	 *
	 * @param release  Release time in seconds (must be positive)
	 */
	public void setRelease(double release) {
		this.release.set(0, release);
	}

	/**
	 * Applies the time-varying low-pass filter to the input audio.
	 * <p>
	 * This method performs the following steps:
	 * </p>
	 * <ol>
	 *   <li>Generates a cutoff frequency envelope based on current ADSR parameters</li>
	 *   <li>Applies the {@link #filterOrder}-order low-pass filter using the time-varying cutoff</li>
	 *   <li>Writes the filtered result to the output buffer</li>
	 * </ol>
	 * <p>
	 * The input and output collections must have compatible shapes. The number of frames
	 * processed is determined by the input size and must not exceed the {@code maxSeconds}
	 * specified in the constructor.
	 * </p>
	 *
	 * @param input   Input audio data as a {@link PackedCollection}
	 * @param output  Output buffer for filtered audio (must be pre-allocated)
	 * @throws IllegalArgumentException if input size exceeds maximum configured frames
	 */
	@Override
	public void process(PackedCollection<?> input, PackedCollection<?> output) {
		int frames = input.getShape().getTotalSize();

		PackedCollection<?> cf = cutoff.range(shape(frames));
		cutoffEnvelope.into(cf.traverseEach()).evaluate();
		multiOrderFilter.into(output.traverse(1))
				.evaluate(input.traverse(0), cf.traverse(0));
	}

	/**
	 * Releases all allocated resources including PackedCollection buffers.
	 * <p>
	 * This method should be called when the processor is no longer needed to free
	 * GPU/CPU memory. After calling destroy, this processor instance should not be used.
	 * </p>
	 */
	@Override
	public void destroy() {
		cutoff.destroy();
		duration.destroy();
		attack.destroy();
		decay.destroy();
		sustain.destroy();
		release.destroy();
		cutoffEnvelope = null;
		multiOrderFilter = null;
	}
}
