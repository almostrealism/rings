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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.optimize.Population;
import org.almostrealism.CodeFeatures;

public class AudioScenePopulation implements Population<PackedCollection<?>, PackedCollection<?>, TemporalCellular>, CodeFeatures {
	private AudioScene<?> scene;

	private List<Genome<PackedCollection<?>>> pop;
	private Genome currentGenome;
	private Cells cells;
	private TemporalCellular temporal;

	private String outputPath;
	private File outputFile;

	private int channel;

	public AudioScenePopulation(AudioScene<?> scene, List<Genome<PackedCollection<?>>> population) {
		this.scene = scene;
		this.pop = population;
		this.channel = -1;
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
					 Receptor<PackedCollection<?>> output) {
		init(templateGenome, Collections.emptyList(), Collections.emptyList(), output);
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
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

	public int getChannel() { return channel; }
	public void setChannel(int channel) { this.channel = channel; }

	@Override
	public List<Genome<PackedCollection<?>>> getGenomes() { return pop; }
	public void setGenomes(List<Genome<PackedCollection<?>>> pop) { this.pop = pop; }

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

	public Runnable generate(int frames, Supplier<String> destinations, Consumer<String> output) {
		return () -> {
			WaveOutput out = new WaveOutput(() ->
					Optional.ofNullable(destinations).map(s -> {
						outputPath = s.get();
						outputFile = new File(outputPath);
						return outputFile;
					}).orElse(null), 24);

			init(getGenomes().get(0), out);

			Runnable gen = null;

			for (int i = 0; i < getGenomes().size(); i++) {
				TemporalCellular cells = null;

				try {
					cells = enableGenome(i);
					if (gen == null) gen = cells.iter(frames, false).get();

					gen.run();
					output.accept(outputPath);
				} finally {
					out.write().get().run();
					out.reset();
					if (cells != null) cells.reset();

					disableGenome();
				}
			}
		};
	}
}
