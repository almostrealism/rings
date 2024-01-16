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
import io.almostrealism.relation.Factor;
import org.almostrealism.heredity.SimpleChromosome;

@Deprecated
public class DelayChromosome extends WavCellChromosome implements OptimizeFactorFeatures {
	public static final int SIZE = 7;

	public DelayChromosome(Chromosome<PackedCollection<?>> source, int sampleRate) {
		super(source, SIZE, sampleRate);
		setTransform(0, g -> oneToInfinity(g.valueAt(0).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
		setTransform(1, g -> oneToInfinity(g.valueAt(1).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
		setTransform(2, g -> oneToInfinity(g.valueAt(2).getResultant(c(1.0)), 0.5).multiply(c(10.0)));
		setTransform(3, g -> oneToInfinity(g.valueAt(3).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
		setTransform(4, g -> g.valueAt(4).getResultant(c(1.0)));
		setTransform(5, g -> oneToInfinity(g.valueAt(5).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
		setTransform(6, g -> oneToInfinity(g.valueAt(6).getResultant(c(1.0)), 1.0).multiply(c(10.0)));
		// addFactor(g -> g.valueAt(0).getResultant(c(1.0)));
		setFactor((p, in) -> {
			CollectionProducerComputation speedUpWavelength = c(p, 1).multiply(c(2.0));
			CollectionProducerComputation speedUpAmp = c(p, 2);
			CollectionProducerComputation slowDownWavelength = c(p, 3).multiply(c(2.0));
			CollectionProducerComputation slowDownAmp = c(p, 4);
			CollectionProducerComputation polySpeedUpWaveLength = c(p, 5);
			CollectionProducerComputation polySpeedUpExp = c(p, 6);
//			return c(1.0).add(_sinw(in, speedUpWavelength, speedUpAmp).pow(c(2.0)))
//					.multiply(c(1.0).subtract(_sinw(in, slowDownWavelength, slowDownAmp).pow(c(2.0))))
//					.multiply(c(1.0).add(polySpeedUpWaveLength.pow(c(-1.0)).multiply(in).pow(polySpeedUpExp)));

			return c(1.0).relativeAdd(sinw(in, speedUpWavelength, speedUpAmp).pow(c(2.0)))
					.relativeMultiply(c(1.0).relativeSubtract(sinw(in, slowDownWavelength, slowDownAmp).pow(c(2.0))))
					.relativeMultiply(c(1.0).relativeAdd(polySpeedUpWaveLength.pow(c(-1.0)).relativeMultiply(in).pow(polySpeedUpExp)));
		});
	}

	public void setDelayRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(0, factorForDelay(min), factorForDelay(max));
	}

	public void setPeriodicSpeedUpDurationRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(1,
				factorForSpeedUpDuration(min),
				factorForSpeedUpDuration(max));
	}

	public void setPeriodicSpeedUpPercentageRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(2,
				factorForSpeedUpPercentage(min),
				factorForSpeedUpPercentage(max));
	}

	public void setPeriodicSlowDownDurationRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(3,
				factorForSlowDownDuration(min),
				factorForSlowDownDuration(max));
	}

	public void setPeriodicSlowDownPercentageRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(4,
				factorForSlowDownPercentage(min),
				factorForSlowDownPercentage(max));
	}

	public void setOverallSpeedUpDurationRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(5,
				factorForPolySpeedUpDuration(min),
				factorForPolySpeedUpDuration(max));
	}

	public void setOverallSpeedUpExponentRange(double min, double max) {
		((SimpleChromosome) getSource()).setParameterRange(6,
				factorForPolySpeedUpExponent(min),
				factorForPolySpeedUpExponent(max));
	}

	public double factorForSpeedUpDuration(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	public double speedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public double factorForSpeedUpPercentage(double decimal) {
		return invertOneToInfinity(decimal, 10, 0.5);
	}

	public double speedUpPercentageForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 0.5, 10);
	}

	public double factorForSlowDownDuration(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	public double slowDownDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public double factorForSlowDownPercentage(double decimal) {
		return decimal;
	}

	public double slowDownPercentageForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f);
	}

	public double factorForPolySpeedUpDuration(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	public double polySpeedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public double factorForPolySpeedUpExponent(double exp) {
		return invertOneToInfinity(exp, 10, 1);
	}

	public double polySpeedUpExponentForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 1, 10);
	}

}
