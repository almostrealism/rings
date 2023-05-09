package org.almostrealism.audio.optimize;

import io.almostrealism.code.ProducerComputation;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.ScaleFactor;

public interface OptimizeFactorFeatures extends HeredityFeatures {
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

	default ProducerComputation<PackedCollection<?>> durationAdjustment(Producer<PackedCollection<?>> params,
																	   Producer<PackedCollection<?>> speedUpOffset,
																	   Producer<PackedCollection<?>> time) {
		return Ops.op(o -> {
			CollectionProducerComputation rp = o.c(params, 0);
			CollectionProducerComputation speedUpDuration = o.c(params, 1);

			CollectionProducerComputation initial = o.pow(o.c(2.0), o.c(16).multiply(o.c(-0.5).add(rp)));

			Producer<PackedCollection<?>> speedUp = _max(c(0.0), subtract(time, speedUpOffset));
//			Producer<PackedCollection<?>> speedUp = _max(c(0.0), subtract(time, c(0.0)));
			speedUp = o.floor(o.divide(speedUp, speedUpDuration));
			return initial.divide(o.pow(o.c(2.0), speedUp));
		});
	}
}
