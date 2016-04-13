package com.almostrealism.feedgrow.population;

import com.almostrealism.feedgrow.organ.Organ;

public interface Population<T> {
	public Organ<T> getOrgan(int index);
	
	public int size();
}
