/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.optimize;

import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;

public class LinearInterpolationChromosome extends WavCellChromosomeExpansionNew {
	public static final int SIZE = 2;

	private final PackedCollection duration;

	public LinearInterpolationChromosome(Chromosome<PackedCollection<?>> source, double min, double max, int sampleRate) {
		super(source, SIZE, sampleRate);
		duration = new PackedCollection(1);

		setTransform(0, id(0));
		setTransform(1, id(1));
		setFactor((p, in) -> {
			CollectionProducerComputation scale = subtract(c(max), c(min));
			CollectionProducerComputation start = c(min).add(c(p, 0).multiply(scale));
			CollectionProducerComputation end = c(min).add(c(p, 1).multiply(scale));
			CollectionProducerComputation pos = divide(in, p(duration));

			return add(start, multiply(end.subtract(start), pos));
		});
	}

	public void setDuration(double seconds) {
		if (seconds == 0) throw new IllegalArgumentException("Duration must be greater than 0");
		duration.setMem(0, seconds);
	}
}
