package com.almostrealism.feedgrow.heredity;

public class DefaultRandomChromosomeFactory implements ChromosomeFactory<Long> {
	private double min, max;
	private int genes, factors;
	
	public DefaultRandomChromosomeFactory() { this (1.0); }
	
	public DefaultRandomChromosomeFactory(double largestScale) {
		this.max = largestScale;
	}
	
	public DefaultRandomChromosomeFactory(double smallestScale, double largestScale) {
		this.min = smallestScale;
		this.max = largestScale;
	}
	
	public void setChromosomeSize(int genes, int factors) {
		this.genes = genes;
		this.factors = factors;
	}
	
	public Chromosome<Long> generateChromosome(double arg) {
		ArrayListChromosome<Long> c = new ArrayListChromosome<Long>();
		
		for (int i = 0; i < genes; i++) {
			ArrayListGene<Long> g = new ArrayListGene<Long>();
			
			for (int j = 0; j < factors; j++) {
				g.add(new LongScaleFactor(min + StrictMath.random() * arg * (max - min)));
			}
			
			c.add(g);
		}
		
		return c;
	}
}
