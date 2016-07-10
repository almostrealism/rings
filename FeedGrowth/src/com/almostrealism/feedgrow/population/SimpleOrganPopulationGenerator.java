/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
