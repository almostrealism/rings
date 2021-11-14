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
import org.almostrealism.organs.GeneticTemporalFactory;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.population.Population;
import io.almostrealism.uml.ModelEntity;

@ModelEntity
public class SimpleOrganPopulation<G, T> implements Population<G, T, SimpleOrgan<T>> {
	private final List<Genome<G>> genomes;
	private GeneticTemporalFactory<G, T, SimpleOrgan<T>> factory;
	private SimpleOrgan<T> currentOrgan;

	private List<? extends Receptor<T>> measures;
	private Receptor<T> output;
	
	public SimpleOrganPopulation() {
		this(new ArrayList<>());
	}
	
	public SimpleOrganPopulation(List<Genome<G>> g) {
		genomes = g;
	}

	public SimpleOrganPopulation(int size, Supplier<Genome> generator) {
		genomes = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			genomes.add(generator.get());
		}
	}

	@Override
	public void init(GeneticTemporalFactory<G, T, SimpleOrgan<T>> factory, Genome<G> templateGenome, List<? extends Receptor<T>> measures, Receptor<T> output) {
		this.factory = factory;
		this.measures = measures;
		this.output = output;
	}

	@Override
	public void merge(Population<G, T, SimpleOrgan<T>> pop) {
		this.genomes.addAll(pop.getGenomes());
	}

	@Override
	public List<Genome<G>> getGenomes() { return genomes; }

	@Override
	public SimpleOrgan<T> enableGenome(int index) {
		if (currentOrgan != null) {
			throw new IllegalStateException();
		}

		// TODO  This won't work - a simple organ genome must be used
		currentOrgan = factory.generateOrgan(getGenomes().get(index), measures, output);
		return currentOrgan;
	}

	@Override
	public void disableGenome() {
		currentOrgan = null;
	}

	@Override
	public int size() { return this.genomes.size(); }
}
