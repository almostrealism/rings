/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.optimize;

import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.SimpleChromosome;

public class DurationAdjustmentChromosome extends WavCellChromosome implements OptimizeFactorFeatures {
	public static final int SIZE = 2;
	private Chromosome<PackedCollection<?>> speedUp;

	protected DurationAdjustmentChromosome(Chromosome<PackedCollection<?>> source, int sampleRate) {
		super(source, SIZE, sampleRate);
	}

	public DurationAdjustmentChromosome(Chromosome<PackedCollection<?>> repeat, Chromosome<PackedCollection<?>> speedUp, int sampleRate) {
		this(combinedChromosome(repeat, speedUp), sampleRate);
		this.speedUp = speedUp;
		setTransform(0, g -> g.valueAt(0).getResultant(c(1.0)));
		setTransform(1, g -> oneToInfinity(g.valueAt(1), 3.0).multiply(c(60.0)));
		setFactor((p, in) -> {
			CollectionProducerComputation rp = c(p, 0);
			CollectionProducerComputation speedUpDuration = c(p, 1);

			CollectionProducerComputation initial = pow(c(2.0), c(16).multiply(c(-0.5).add(rp)));

			return initial.divide(pow(c(2.0), floor(divide(in, speedUpDuration))));
			// return initial.divide(pow(c(2.0), floor(speedUpDuration.pow(c(-1.0)).multiply(in))));
		});
	}

	public void setRepeatSpeedUpDurationRange(double min, double max) {
		((SimpleChromosome) speedUp).setParameterRange(0,
				factorForRepeatSpeedUpDuration(min),
				factorForRepeatSpeedUpDuration(max));
	}

	public double factorForRepeatSpeedUpDuration(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	public double repeatSpeedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	protected static Chromosome<PackedCollection<?>> combinedChromosome(Chromosome<PackedCollection<?>> repeat,
																		Chromosome<PackedCollection<?>> speedUp) {
		return new Chromosome<>() {
			@Override
			public int length() { return repeat.length(); }

			@Override
			public Gene<PackedCollection<?>> valueAt(int pos) {
				return new Gene<>() {
					@Override
					public int length() {
						return 2;
					}

					@Override
					public Factor<PackedCollection<?>> valueAt(int factor) {
						return switch (factor) {
							case 0 -> repeat.valueAt(pos, 0);
							case 1 -> speedUp.valueAt(pos, 0);
							default -> throw new IllegalArgumentException("Invalid position " + factor);
						};
					}
				};
			}
		};
	}


	// 		BiFunction<Gene<PackedCollection<?>>, Gene<PackedCollection<?>>, IntFunction<Cell<PackedCollection<?>>>> generator = (g, p) -> channel -> {
	//			Producer<PackedCollection<?>> duration = g.valueAt(2).getResultant(c(getTempo().l(1)));
	//
	//			Producer<PackedCollection<?>> x = p.valueAt(0).getResultant(c(1.0));
	//			Producer<PackedCollection<?>> y = p.valueAt(1).getResultant(c(1.0));
	//			Producer<PackedCollection<?>> z = p.valueAt(2).getResultant(c(1.0));
	//
	//			if (sourceOverride == null) {
	//				return getWaves().getChoiceCell(channel,
	//						toScalar(g.valueAt(0).getResultant(Ops.ops().c(1.0))),
	//						toScalar(x), toScalar(y), toScalar(z), toScalar(g.valueAt(1).getResultant(duration)),
	//						enableRepeat ? toScalar(duration) : null);
	//			} else {
	//				return sourceOverride.getChoiceCell(channel, toScalar(g.valueAt(0).getResultant(Ops.ops().c(1.0))),
	//						v(0.0), v(0.0), v(0.0), v(0.0), null);
	//			}
	//		};
}
