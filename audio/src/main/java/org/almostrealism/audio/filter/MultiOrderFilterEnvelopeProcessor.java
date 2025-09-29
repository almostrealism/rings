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

// TODO  This should implement AudioProcessor
public class MultiOrderFilterEnvelopeProcessor implements EnvelopeProcessor, Destroyable, CellFeatures, EnvelopeFeatures {
	public static double filterPeak = 20000;
	public static int filterOrder = 40;

	private PackedCollection<?> cutoff;

	private PackedCollection<?> duration;
	private PackedCollection<?> attack;
	private PackedCollection<?> decay;
	private PackedCollection<?> sustain;
	private PackedCollection<?> release;

	private Evaluable<PackedCollection<?>> cutoffEnvelope;
	private Evaluable<PackedCollection<?>> multiOrderFilter;

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
		multiOrderFilter = lowPass(v(shape(maxFrames), 0),
								v(shape(maxFrames), 1),
								sampleRate, filterOrder)
							.get();
	}

	public void setDuration(double duration) {
		this.duration.set(0, duration);
	}

	public void setAttack(double attack) {
		this.attack.set(0, attack);
	}

	public void setDecay(double decay) {
		this.decay.set(0, decay);
	}

	public void setSustain(double sustain) {
		this.sustain.set(0, sustain);
	}

	public void setRelease(double release) {
		this.release.set(0, release);
	}

	@Override
	public void process(PackedCollection<?> input, PackedCollection<?> output) {
		int frames = input.getShape().getTotalSize();

		PackedCollection<?> cf = cutoff.range(shape(frames));
		cutoffEnvelope.into(cf.traverseEach()).evaluate();
		multiOrderFilter.into(output.traverse(1))
				.evaluate(input.traverse(0), cf.traverse(0));
	}

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
