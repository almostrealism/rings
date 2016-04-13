package com.almostrealism.feedgrow.heredity;

public interface ChromosomeFactory<T> {
	public void setChromosomeSize(int genes, int factors);
	
	public Chromosome<T> generateChromosome(double arg);
}
