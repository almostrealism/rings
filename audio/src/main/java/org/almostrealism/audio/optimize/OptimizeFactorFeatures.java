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
}
