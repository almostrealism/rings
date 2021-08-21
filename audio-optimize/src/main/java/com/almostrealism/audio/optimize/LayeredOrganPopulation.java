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

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.AudioMeter;
import org.almostrealism.breeding.AssignableGenome;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.OrganFactory;
import org.almostrealism.population.Population;
import org.almostrealism.util.CodeFeatures;

public class LayeredOrganPopulation<G, O, A, R> implements Population<O, AdjustmentLayerOrganSystem<G, O, A, R>>, CodeFeatures {
	private final List<Genome> pop;
	private final AssignableGenome genome;
	private Genome currentGenome;
	private AdjustmentLayerOrganSystem<G, O, A, R> organ;

	public LayeredOrganPopulation() { this(new ArrayList<>()); }
	
	public LayeredOrganPopulation(List<Genome> population) {
		this.pop = population;
		this.genome = new AssignableGenome();
	}

	@Override
	public void init(OrganFactory<O, AdjustmentLayerOrganSystem<G, O, A, R>> organFactory, Genome templateGenome, Receptor<O> meter) {
		enableGenome(templateGenome);
		this.organ = organFactory.generateOrgan(genome);
		this.organ.setMonitor(meter);
		disableGenome();
	}

	@Override
	public void merge(Population<O, AdjustmentLayerOrganSystem<G, O, A, R>> pop) {
		this.pop.addAll(pop.getGenomes());
	}

	@Override
	public List<Genome> getGenomes() { return pop; }

	@Override
	public AdjustmentLayerOrganSystem<G, O, A, R> enableGenome(int index) {
		enableGenome(getGenomes().get(index));
		return organ;
	}

	private void enableGenome(Genome newGenome) {
		if (currentGenome != null) {
			throw new IllegalStateException();
		}

		currentGenome = newGenome;
		genome.assignTo(currentGenome);
	}

	@Override
	public void disableGenome() {
		this.currentGenome = null;
		this.organ.reset();
	}

	@Override
	public int size() { return getGenomes().size(); }
}
