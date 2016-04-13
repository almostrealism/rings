package com.almostrealism.feedgrow.breeding;

import com.almostrealism.feedgrow.heredity.ArrayListChromosome;
import com.almostrealism.feedgrow.heredity.ArrayListGene;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.Gene;
import com.almostrealism.feedgrow.heredity.LongScaleFactor;

public class LongFactorAverageBreeder implements Breeder<Long> {
	private double mutation = 0.0;
	
	public void setMutationAmount(double m) { this.mutation = m; }
	
	public Chromosome<Long> combine(Chromosome<Long> c1, Chromosome<Long> c2) {
		ArrayListChromosome<Long> chrom = new ArrayListChromosome<Long>();

		for (int i = 0; i < c1.length() && i < c2.length(); i++) {
			Gene<Long> g1 = c1.getGene(i);
			Gene<Long> g2 = c2.getGene(i);
			chrom.add(combine(g1, g2));
		}

		return chrom;
	}

	private Gene<Long> combine(Gene<Long> g1, Gene<Long> g2) {
		ArrayListGene<Long> gene = new ArrayListGene<Long>();
		
		for (int i = 0; i < g1.length() && i < g2.length(); i++) {
			LongScaleFactor f1 = (LongScaleFactor) g1.getFactor(i);
			LongScaleFactor f2 = (LongScaleFactor) g2.getFactor(i);
			
			double scale = (f1.getScale() + f2.getScale()) / 2.0;
			double r = StrictMath.random() - 0.5;
			
			LongScaleFactor factor = new LongScaleFactor(scale + r * mutation);
			gene.add(factor);
		}
		
		return gene;
	}
}
