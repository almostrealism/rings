package com.almostrealism.feedgrow.optimization;

import com.almostrealism.feedgrow.organ.Organ;

public interface HealthComputation<T> {
	public double computeHealth(Organ<T> organ);
}
