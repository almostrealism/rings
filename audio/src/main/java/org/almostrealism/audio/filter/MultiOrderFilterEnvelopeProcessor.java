/*
 * Copyright 2024 Michael Murray
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

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.almostrealism.time.computations.MultiOrderFilter;

import java.io.File;

// TODO  This should implement AudioProcessor
public class MultiOrderFilterEnvelopeProcessor implements EnvelopeProcessor, CellFeatures, EnvelopeFeatures {
	public static double filterPeak = 20000;
	public static int filterOrder = 40;

	private PackedCollection<?> duration;
	private PackedCollection<?> attack;
	private PackedCollection<?> decay;
	private PackedCollection<?> sustain;
	private PackedCollection<?> release;

	private Evaluable<PackedCollection<?>> lowPassCoefficients;
	private Evaluable<PackedCollection<?>> multiOrderFilter;

	public MultiOrderFilterEnvelopeProcessor(int sampleRate, double maxSeconds) {
		duration = new PackedCollection<>(1);
		attack = new PackedCollection<>(1);
		decay = new PackedCollection<>(1);
		sustain = new PackedCollection<>(1);
		release = new PackedCollection<>(1);

		EnvelopeSection envelope = envelope(cp(duration), cp(attack), cp(decay), cp(sustain), cp(release));
		Producer<PackedCollection<?>> env =
				sampling(sampleRate, () -> envelope.get().getResultant(c(filterPeak)));

		lowPassCoefficients = lowPassCoefficients(env, sampleRate, filterOrder).get();

		multiOrderFilter = MultiOrderFilter.create(
					v(shape((int) (maxSeconds * sampleRate)), 0),
					v(shape(1, filterOrder + 1).traverse(1), 1))
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

		TraversalPolicy coeffShape = shape(frames, filterOrder + 1);
		PackedCollection<?> coeff = PackedCollection.factory().apply(coeffShape.getTotalSize()).reshape(coeffShape);
		lowPassCoefficients.into(coeff.traverse(1)).evaluate();

		multiOrderFilter.into(output.traverse(1)).evaluate(input.traverse(1), coeff.traverse(1));
	}
}
