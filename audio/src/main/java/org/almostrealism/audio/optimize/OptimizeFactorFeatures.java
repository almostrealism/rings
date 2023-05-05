package org.almostrealism.audio.optimize;

import org.almostrealism.Ops;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.ScaleFactor;

public interface OptimizeFactorFeatures extends HeredityFeatures {
	default double valueForFactor(Factor<PackedCollection<?>> value) {
		if (value instanceof ScaleFactor) {
			return ((ScaleFactor) value).getScaleValue();
		} else {
			return value.getResultant(Ops.ops().c(1.0)).get().evaluate().toDouble(0);
		}
	}

	default double valueForFactor(Factor<PackedCollection<?>> value, double exp, double multiplier) {
		if (value instanceof ScaleFactor) {
			return HeredityFeatures.getInstance().oneToInfinity(((ScaleFactor) value).getScaleValue(), exp) * multiplier;
		} else {
			double v = value.getResultant(Ops.ops().c(1.0)).get().evaluate().toDouble(0);
			return HeredityFeatures.getInstance().oneToInfinity(v, exp) * multiplier;
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
}
