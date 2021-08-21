/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.audio.optimize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.OrganFactory;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.population.Population;
import io.almostrealism.uml.ModelEntity;

@ModelEntity
public class SimpleOrganPopulation<T> implements Population<T, SimpleOrgan<T>> {
	private final List<Genome> genomes;
	private OrganFactory<T, SimpleOrgan<T>> factory;
	private SimpleOrgan<T> currentOrgan;
	
	public SimpleOrganPopulation() {
		this(new ArrayList<>());
	}
	
	public SimpleOrganPopulation(List<Genome> g) {
		genomes = g;
	}

	public SimpleOrganPopulation(int size, Supplier<Genome> generator) {
		genomes = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			genomes.add(generator.get());
		}
	}

	@Override
	public void init(OrganFactory<T, SimpleOrgan<T>> factory, Genome templateGenome, Receptor<T> measure) {
		this.factory = factory;
	}

	@Override
	public void merge(Population<T, SimpleOrgan<T>> pop) {
		this.genomes.addAll(pop.getGenomes());
	}

	@Override
	public List<Genome> getGenomes() { return genomes; }

	@Override
	public SimpleOrgan<T> enableGenome(int index) {
		if (currentOrgan != null) {
			throw new IllegalStateException();
		}

		currentOrgan = factory.generateOrgan(getGenomes().get(index));
		return currentOrgan;
	}

	@Override
	public void disableGenome() {
		currentOrgan = null;
	}

	@Override
	public int size() { return this.genomes.size(); }
}
