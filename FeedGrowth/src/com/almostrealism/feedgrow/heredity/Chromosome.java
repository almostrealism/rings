package com.almostrealism.feedgrow.heredity;

import com.almostrealism.feedgrow.breeding.Breedable;

public interface Chromosome<T> extends Breedable {
	public Gene<T> getGene(int index);
	
	public int length();
}
