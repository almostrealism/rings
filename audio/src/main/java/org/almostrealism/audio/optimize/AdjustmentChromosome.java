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

import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.SimpleChromosome;

@Deprecated
public class AdjustmentChromosome extends WavCellChromosome implements OptimizeFactorFeatures {
	public static final int SIZE = 6;

	private boolean relative;

	public AdjustmentChromosome(Chromosome<PackedCollection<?>> source, double min, double max, boolean relative, int sampleRate) {
		super(source, 6, sampleRate);
		this.relative = relative;
		setTransform(0, g -> oneToInfinity(g.valueAt(0), 3.0).multiply(c(60.0)));
		setTransform(1, g -> oneToInfinity(g.valueAt(1), 3.0).multiply(c(60.0)));
		setTransform(2, g -> oneToInfinity(g.valueAt(2), 1.0).multiply(c(10.0)));
		setTransform(3, g -> oneToInfinity(g.valueAt(3), 1.0).multiply(c(10.0)));
		setTransform(4, g -> g.valueAt(4).getResultant(c(1.0)));
		setTransform(5, g -> oneToInfinity(g.valueAt(5), 3.0).multiply(c(60.0)));
		setFactor((p, in) -> {
			CollectionProducerComputation periodicWavelength = c(p, 0);
			CollectionProducerComputation periodicAmp = c(1.0);
			CollectionProducerComputation polyWaveLength = c(p, 1);
			CollectionProducerComputation polyExp = c(p, 2);
			CollectionProducerComputation initial = c(p, 3);
			CollectionProducerComputation scale = c(p, 4);
			CollectionProducerComputation offset = c(p, 5);

			if (relative) scale = scale.multiply(initial);
			CollectionProducerComputation pos = subtract(in, offset);
			return _bound(pos._greaterThan(c(0.0),
					polyWaveLength.pow(c(-1.0))
							.multiply(pos).pow(polyExp)
							.multiply(scale).add(initial), initial),
					min, max);
		});
	}

	public void setPeriodicDurationRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(0,
				factorForPeriodicAdjustmentDuration(min),
				factorForPeriodicAdjustmentDuration(max));
	}

	public void setOverallDurationRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(1,
				factorForPolyAdjustmentDuration(min),
				factorForPolyAdjustmentDuration(max));
	}

	public void setOverallExponentRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(2,
				factorForPolyAdjustmentExponent(min),
				factorForPolyAdjustmentExponent(max));
	}

	public void setOverallInitialRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(3,
				factorForAdjustmentInitial(min),
				factorForAdjustmentInitial(max));
	}

	public void setOverallScaleRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(4, min, max);
	}

	public void setOverallOffsetRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(5,
				factorForAdjustmentOffset(min),
				factorForAdjustmentOffset(max));
	}
}
