/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.optimize;

import java.util.List;
import java.util.function.Supplier;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.Cells;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.optimize.Population;
import org.almostrealism.CodeFeatures;

public class AudioScenePopulation<G> implements Population<G, PackedCollection<?>, TemporalCellular>, CodeFeatures {
	private AudioScene<?> scene;

	private List<Genome<G>> pop;
	private Genome currentGenome;
	private Cells cells;
	private TemporalCellular temporal;

	public AudioScenePopulation(AudioScene<?> scene, List<Genome<G>> population) {
		this.scene = scene;
		this.pop = population;
	}

	public void init(Genome<G> templateGenome,
					 List<? extends Receptor<PackedCollection<?>>> measures,
					 List<? extends Receptor<PackedCollection<?>>> stems,
					 Receptor<PackedCollection<?>> output) {
		enableGenome(templateGenome);
		this.cells = scene.getCells(measures, stems, output);

		this.temporal = new TemporalCellular() {
			@Override
			public Supplier<Runnable> setup() {
				OperationList setup = new OperationList("AudioScenePopulation Cellular Setup");
				setup.addAll((List) scene.setup());
				setup.addAll((List) cells.setup());
				return setup;
			}

			@Override
			public Supplier<Runnable> tick() {
				return cells.tick();
			}
		};

		disableGenome();
	}

	@Override
	public List<Genome<G>> getGenomes() { return pop; }

	public void setGenomes(List<Genome<G>> pop) { this.pop = pop; }

	@Override
	public TemporalCellular enableGenome(int index) {
		enableGenome(getGenomes().get(index));
		cells.reset();
		return temporal;
	}

	private void enableGenome(Genome newGenome) {
		if (currentGenome != null) {
			throw new IllegalStateException();
		}

		currentGenome = newGenome;
		scene.assignGenome(currentGenome);
	}

	@Override
	public void disableGenome() {
		this.currentGenome = null;
		this.cells.reset();
	}

	@Override
	public int size() { return getGenomes().size(); }
}
