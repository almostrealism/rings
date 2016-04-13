package com.almostrealism.feedgrow.heredity;

public class FloatingPointRandomChromosomeFactory implements ChromosomeFactory<Double> {
	private int genes, factors;
	
	public void setChromosomeSize(int genes, int factors) {
		this.genes = genes;
		this.factors = factors;
	}
	
	public Chromosome<Double> generateChromosome(double arg) {
		ArrayListChromosome<Double> c = new ArrayListChromosome<Double>();
		
		for (int i = 0; i < genes; i++) {
			ArrayListGene<Double> g = new ArrayListGene<Double>();
			
			for (int j = 0; j < factors; j++) {
				g.add(new DoubleScaleFactor(StrictMath.random() * arg));
			}
			
			c.add(g);
		}
		
		return c;
	}
}
