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

import io.almostrealism.code.ProducerComputation;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.ScaleFactor;

public interface OptimizeFactorFeatures extends HeredityFeatures, CodeFeatures {
	default double valueForFactor(Factor<PackedCollection<?>> value) {
		if (value instanceof ScaleFactor) {
			return ((ScaleFactor) value).getScaleValue();
		} else {
			return value.getResultant(c(1.0)).get().evaluate().toDouble(0);
		}
	}

	default double valueForFactor(Factor<PackedCollection<?>> value, double exp, double multiplier) {
		if (value instanceof ScaleFactor) {
			return oneToInfinity(((ScaleFactor) value).getScaleValue(), exp) * multiplier;
		} else {
			double v = value.getResultant(c(1.0)).get().evaluate().toDouble(0);
			return oneToInfinity(v, exp) * multiplier;
		}
	}

	default double[] repeatForFactor(Factor<PackedCollection<?>> f) {
		double v = 16 * (valueForFactor(f) - 0.5);

		if (v == 0) {
			return new double[] { 1.0, 1.0 };
		} else if (v > 0) {
			return new double[] { Math.pow(2.0, v), 1.0 };
		} else if (v < 0) {
			return new double[] { 1.0, Math.pow(2.0, -v) };
		}

		return null;
	}

	default double factorForRepeat(double beats) {
		return ((Math.log(beats) / Math.log(2)) / 16) + 0.5;
	}

	default double factorForRepeatSpeedUpDuration(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	default double repeatSpeedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	default double factorForDelay(double seconds) {
		return invertOneToInfinity(seconds, 60, 3);
	}

	default double delayForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	default double factorForExponent(double exp) {
		return invertOneToInfinity(exp, 10, 1);
	}

	default ProducerComputation<PackedCollection<?>> riseFall(double minValue, double maxValue, double minScale,
															  Producer<PackedCollection<?>> d,
															  Producer<PackedCollection<?>> m,
															  Producer<PackedCollection<?>> e,
															  Producer<PackedCollection<?>> time,
															  Producer<PackedCollection<?>> duration) {
		PackedCollection<Scalar> directionChoices = Scalar.scalarBank(2);
		directionChoices.set(0, -1);
		directionChoices.set(1, 1);

		PackedCollection<Scalar> originChoices = Scalar.scalarBank(2);
		originChoices.set(0, maxValue);
		originChoices.set(1, minValue);

		CollectionProducerComputation scale = subtract(c(maxValue), c(minValue));
		CollectionProducerComputation direction = c(choice(2, toScalar(d), p(directionChoices)), 0);

		CollectionProducerComputation magnitude = multiply(scale, m);
		CollectionProducerComputation start = c(choice(2, toScalar(d), p(originChoices)), 0);
		CollectionProducerComputation end = multiply(direction, magnitude).add(start);

		CollectionProducerComputation pos = pow(divide(time, duration), e);

		return add(start, multiply(end.subtract(start), pos));
	}

	default ProducerComputation<PackedCollection<?>> durationAdjustment(Producer<PackedCollection<?>> params,
																	   Producer<PackedCollection<?>> speedUpOffset,
																	   Producer<PackedCollection<?>> time) {
		return durationAdjustment(c(params, 0), c(params, 1), speedUpOffset, time);
	}

	default ProducerComputation<PackedCollection<?>> durationAdjustment(Producer<PackedCollection<?>> rp,
																		Producer<PackedCollection<?>> speedUpDuration,
																		Producer<PackedCollection<?>> speedUpOffset,
																		Producer<PackedCollection<?>> time) {
		CollectionProducerComputation initial = pow(c(2.0), c(16).multiply(c(-0.5).add(rp)));

		Producer<PackedCollection<?>> speedUp = _max(c(0.0), subtract(time, speedUpOffset));
//		Producer<PackedCollection<?>> speedUp = _max(c(0.0), subtract(time, c(0.0)));
		speedUp = floor(divide(speedUp, speedUpDuration));
		return initial.divide(pow(c(2.0), speedUp));
	}
}
