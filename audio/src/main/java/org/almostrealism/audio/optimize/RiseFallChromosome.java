/*
 * Copyright 2023 Michael Murray
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

import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;

public class RiseFallChromosome extends WavCellChromosomeExpansion {
	public static final int SIZE = 2;

	private final PackedCollection duration;

	public RiseFallChromosome(Chromosome<PackedCollection<?>> source, double minValue, double maxValue, double minScale, int sampleRate) {
		super(source, source.length(), SIZE, sampleRate);
		duration = new PackedCollection(1);

		ScalarBank directionChoices = new ScalarBank(2);
		directionChoices.set(0, -1);
		directionChoices.set(1, 1);

		ScalarBank originChoices = new ScalarBank(2);
		originChoices.set(0, maxValue);
		originChoices.set(1, minValue);

		setTransform(0, identity(0, c(1.0)));
		setTransform(1, identity(1, c(1.0)));
		addFactor((p, in) -> {
			CollectionProducer scale = _subtract(c(maxValue), c(minValue));
			CollectionProducer direction = c(choice(2, toScalar(c(p, 0)), p(directionChoices)), 0);

			CollectionProducer magnitude = _multiply(scale, c(p, 1));
			CollectionProducer start = c(choice(2, toScalar(c(p, 0)), p(originChoices)), 0);
			CollectionProducer end = _multiply(direction, magnitude)._add(start);

			CollectionProducer pos = _divide(in, p(duration));

			return _add(start, _multiply(end._subtract(start), pos));
		});
	}

	public void setDuration(double seconds) {
		if (seconds == 0) throw new IllegalArgumentException("Duration must be greater than 0");
		duration.setMem(0, seconds);
	}
}
