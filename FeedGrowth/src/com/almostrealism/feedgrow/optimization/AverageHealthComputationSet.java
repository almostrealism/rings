package com.almostrealism.feedgrow.optimization;

import java.util.HashSet;
import java.util.Iterator;

import com.almostrealism.feedgrow.organ.Organ;

public class AverageHealthComputationSet<T> extends HashSet<HealthComputation<T>> implements HealthComputation<T> {
	public double computeHealth(Organ<T> organ) {
		double total = 0;
		
		Iterator<HealthComputation<T>> itr = iterator();
		
		while (itr.hasNext()) {
			total += itr.next().computeHealth(organ);
		}
		
		return total / size();
	}
}
