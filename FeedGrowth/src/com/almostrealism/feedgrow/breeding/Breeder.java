package com.almostrealism.feedgrow.breeding;

import com.almostrealism.feedgrow.heredity.Chromosome;

public interface Breeder<T> {
	public Chromosome<T> combine(Chromosome<T> c1, Chromosome<T> c2);
}
