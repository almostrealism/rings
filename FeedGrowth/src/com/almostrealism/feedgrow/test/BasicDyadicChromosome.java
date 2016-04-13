package com.almostrealism.feedgrow.test;

import java.util.List;

import com.almostrealism.feedgrow.breeding.Breedable;
import com.almostrealism.feedgrow.breeding.Breeder;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.Factor;
import com.almostrealism.feedgrow.heredity.Gene;
import com.almostrealism.feedgrow.heredity.LongScaleFactor;

public class BasicDyadicChromosome implements Chromosome<Long> {
	private Factor<Long> factorA, factorB;
	private Gene<Long> geneA, geneB;
	
	public BasicDyadicChromosome(double scaleA, double scaleB) {
		this.factorA = new LongScaleFactor(scaleA);
		this.factorB = new LongScaleFactor(scaleB);
		
		// Gene A sends to Cell index 1 by factor B
		this.geneA = new Gene<Long>() {
			public Factor<Long> getFactor(int index) {
				return index == 1 ? factorB : null;
			}
			
			public int length() { return 2; }
		};
		
		// Gene B sends to Cell index 0 by factor A
		this.geneB = new Gene<Long>() {
			public Factor<Long> getFactor(int index) {
				return index == 0 ? factorA : null;
			}
			
			public int length() { return 2; }
		};
	}
	
	public Gene<Long> getGene(int index) {
		if (index == 0) {
			return geneA;
		} else if (index == 1) {
			return geneB;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public int length() { return 2; }
	
	public Breedable breed(Breedable b, List<Breeder> l) {
		return l.get(0).combine(this, (Chromosome) b);
	}
}
