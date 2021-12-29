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

import java.util.List;

import io.almostrealism.uml.Lifecycle;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.GeneticTemporalFactory;
import org.almostrealism.population.Population;
import org.almostrealism.util.CodeFeatures;

public class LayeredOrganPopulation<G, O, A, R> implements Population<G, O, AdjustmentLayerOrganSystem<G, O, A, R>>, CodeFeatures {
	private final List<Genome<G>> pop;
	private final DefaultAudioGenome genome;
	private Genome currentGenome;
	private AdjustmentLayerOrganSystem<G, O, A, R> organ;

	public LayeredOrganPopulation(List<Genome<G>> population, int sources, int delayLayers) {
		this(population, sources, delayLayers, OutputLine.sampleRate);
	}

	public LayeredOrganPopulation(List<Genome<G>> population, int sources, int delayLayers, int sampleRate) {
		this.pop = population;
		this.genome = new DefaultAudioGenome(sources, delayLayers, sampleRate);
	}

	@Override
	public void init(GeneticTemporalFactory<G, O, AdjustmentLayerOrganSystem<G, O, A, R>> organFactory, Genome<G> templateGenome, List<? extends Receptor<O>> measures, Receptor<O> output) {
		enableGenome(templateGenome);
		this.organ = organFactory.generateOrgan((Genome) genome, measures, output);
		disableGenome();
	}

	@Override
	public void merge(Population<G, O, AdjustmentLayerOrganSystem<G, O, A, R>> pop) {
		this.pop.addAll(pop.getGenomes());
	}

	@Override
	public List<Genome<G>> getGenomes() { return pop; }

	@Override
	public AdjustmentLayerOrganSystem<G, O, A, R> enableGenome(int index) {
		enableGenome(getGenomes().get(index));
		organ.reset();
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
