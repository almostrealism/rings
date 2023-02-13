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

import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;

public class AdjustmentChromosome extends WavCellChromosomeExpansion {
	private boolean relative;

	public AdjustmentChromosome(Chromosome<PackedCollection<?>> source, double min, double max, boolean relative, int sampleRate) {
		super(source, source.length(), 6, sampleRate);
		this.relative = relative;
		setTransform(0, g -> oneToInfinity(g.valueAt(0), 3.0)._multiply(c(60.0)));
		setTransform(1, g -> oneToInfinity(g.valueAt(1), 3.0)._multiply(c(60.0)));
		setTransform(2, g -> oneToInfinity(g.valueAt(2), 1.0)._multiply(c(10.0)));
		setTransform(3, g -> oneToInfinity(g.valueAt(3), 1.0)._multiply(c(10.0)));
		setTransform(4, g -> g.valueAt(4).getResultant(c(1.0)));
		setTransform(5, g -> oneToInfinity(g.valueAt(5), 3.0)._multiply(c(60.0)));
		addFactor((p, in) -> {
			CollectionProducer periodicWavelength = c(p, 0);
			CollectionProducer periodicAmp = c(1.0);
			CollectionProducer polyWaveLength = c(p, 1);
			CollectionProducer polyExp = c(p, 2);
			CollectionProducer initial = c(p, 3);
			CollectionProducer scale = c(p, 4);
			CollectionProducer offset = c(p, 5);

			if (relative) scale = scale._multiply(initial);
			CollectionProducer pos = _subtract(in, offset);
			return _bound(pos._greaterThan(c(0.0),
					polyWaveLength._pow(c(-1.0))
							._multiply(pos)._pow(polyExp)
							._multiply(scale)._add(initial), initial),
					min, max);
		});
	}
}
