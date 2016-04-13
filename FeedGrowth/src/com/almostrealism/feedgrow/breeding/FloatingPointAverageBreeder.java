package com.almostrealism.feedgrow.breeding;

import com.almostrealism.feedgrow.heredity.ArrayListChromosome;
import com.almostrealism.feedgrow.heredity.ArrayListGene;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.DoubleScaleFactor;
import com.almostrealism.feedgrow.heredity.Gene;

public class FloatingPointAverageBreeder implements Breeder<Double> {
	private double mutation = 0.0;
	
	public void setMutationAmount(double m) { this.mutation = m; }
	
	public Chromosome<Double> combine(Chromosome<Double> c1, Chromosome<Double> c2) {
		ArrayListChromosome<Double> chrom = new ArrayListChromosome<Double>();

		for (int i = 0; i < c1.length() && i < c2.length(); i++) {
			Gene<Double> g1 = c1.getGene(i);
			Gene<Double> g2 = c2.getGene(i);
			chrom.add(combine(g1, g2));
		}

		return chrom;
	}

	private Gene<Double> combine(Gene<Double> g1, Gene<Double> g2) {
		ArrayListGene<Double> gene = new ArrayListGene<Double>();
		
		for (int i = 0; i < g1.length() && i < g2.length(); i++) {
			DoubleScaleFactor f1 = (DoubleScaleFactor) g1.getFactor(i);
			DoubleScaleFactor f2 = (DoubleScaleFactor) g2.getFactor(i);
			
			double scale = (f1.getScale() + f2.getScale()) / 2.0;
			double r = StrictMath.random() - 0.5;
			
			DoubleScaleFactor factor = new DoubleScaleFactor(scale + r * mutation);
			gene.add(factor);
		}
		
		return gene;
	}
}
