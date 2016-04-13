package com.almostrealism.feedgrow.heredity;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.feedgrow.breeding.Breedable;
import com.almostrealism.feedgrow.breeding.Breeder;

public class ArrayListChromosome<T> extends ArrayList<Gene<T>> implements Chromosome<T>, Breedable {
	public Gene<T> getGene(int index) { return get(index); }
	public int length() { return size(); }
	
	public Breedable breed(Breedable b, List<Breeder> l) {
		if (b instanceof Chromosome<?> == false)
			throw new IllegalArgumentException("Invalid type for breeding");
		
		Chromosome<T> c = (Chromosome<T>) b;
		return l.get(0).combine(this, c);
	}
}
