package com.almostrealism.feedgrow.population;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.ChromosomeFactory;

public class SimpleOrganPopulationGenerator<T> {
	private ChromosomeFactory<Double> xFactory;
	private ChromosomeFactory<T> yFactory;
	
	public SimpleOrganPopulationGenerator(ChromosomeFactory<Double> xFactory, ChromosomeFactory<T> yFactory) {
		this.xFactory = xFactory;
		this.yFactory = yFactory;
	}
	
	public SimpleOrganPopulation<T> generatePopulation(int size, double argX, double argY) {
		List<Chromosome<Double>> xSomes = new ArrayList<Chromosome<Double>>();
		List<Chromosome<T>> ySomes = new ArrayList<Chromosome<T>>();
		
		for (int i = 0; i < size; i++) {
			xSomes.add(xFactory.generateChromosome(argX));
			ySomes.add(yFactory.generateChromosome(argY));
		}
		
		return new SimpleOrganPopulation<T>(xSomes, ySomes);
	}
}
