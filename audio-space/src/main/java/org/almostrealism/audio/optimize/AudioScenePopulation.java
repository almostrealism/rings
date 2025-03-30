/*
 * Copyright 2025 Michael Murray
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.profile.OperationProfile;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.health.HealthComputationAdapter;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.io.Console;
import org.almostrealism.optimize.HealthCallable;
import org.almostrealism.optimize.Population;
import org.almostrealism.CodeFeatures;

public class AudioScenePopulation implements Population<PackedCollection<?>, TemporalCellular>, Destroyable, CodeFeatures {
	public static boolean enableFlatten = true;

	private AudioScene<?> scene;

	private List<Genome<PackedCollection<?>>> pop;
	private Genome currentGenome;
	private Cells cells;
	private TemporalCellular temporal;

	private String outputPath;
	private File outputFile;

	public AudioScenePopulation(AudioScene<?> scene) {
		this(scene, new ArrayList<>());
	}

	public AudioScenePopulation(AudioScene<?> scene, List<Genome<PackedCollection<?>>> population) {
		this.scene = scene;
		this.pop = population;
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
					 Receptor<PackedCollection<?>> output) {
		init(templateGenome, Collections.emptyList(), Collections.emptyList(), output);
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
					 Receptor<PackedCollection<?>> output,
					 List<Integer> channels) {
		init(templateGenome, Collections.emptyList(), Collections.emptyList(), output, channels);
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
					 List<? extends Receptor<PackedCollection<?>>> measures,
					 List<? extends Receptor<PackedCollection<?>>> stems,
					 Receptor<PackedCollection<?>> output) {
		init(templateGenome, measures, stems, output, null);
	}

	public void init(Genome<PackedCollection<?>> templateGenome,
					 List<? extends Receptor<PackedCollection<?>>> measures,
					 List<? extends Receptor<PackedCollection<?>>> stems,
					 Receptor<PackedCollection<?>> output, List<Integer> channels) {
		enableGenome(templateGenome);
		this.cells = channels == null ? scene.getCells(measures, stems, output) :
									scene.getCells(measures, stems, output, channels);

		// TODO  Replace with scene.runner()
		this.temporal = new TemporalCellular() {
			@Override
			public Supplier<Runnable> setup() {
				OperationList setup = new OperationList("AudioScenePopulation Cellular Setup");
				setup.addAll((List) scene.setup());
				setup.addAll((List) cells.setup());
				return enableFlatten ? setup.flatten() : setup;
			}

			@Override
			public Supplier<Runnable> tick() {
				return cells.tick();
			}
		};

		disableGenome();
	}

	public AudioScene<?> getScene() {
		return scene;
	}

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

		if (this.cells != null) {
			this.cells.reset();
		}
	}

	public boolean validateGenome(Genome genome) {
		try {
			enableGenome(genome);
			return true;
		} catch (Exception e) {
			warn("Genome incompatible with current scene (" + e.getClass().getSimpleName() + ")");
			return false;
		} finally {
			disableGenome();
		}
	}

	@Override
	public int size() { return getGenomes().size(); }

	public Runnable generate(int channel, int frames, Supplier<String> destinations,
							 Consumer<GenerationResult> output) {
		return () -> {
			WaveOutput out = new WaveOutput(() ->
					Optional.ofNullable(destinations).map(s -> {
						outputPath = s.get();
						outputFile = new File(outputPath);
						return outputFile;
					}).orElse(null), 24);

			init(getGenomes().get(0), out, List.of(channel));

			OperationProfile profile = new OperationProfile("AudioScenePopulation");

			Runnable gen = null;

			for (int i = 0; i < getGenomes().size(); i++) {
				TemporalCellular cells = null;
				long start = System.currentTimeMillis();

				try {
					outputPath = null;
					cells = enableGenome(i);

					if (gen == null) {
						Supplier<Runnable> op = cells.iter(frames, false);

						if (op instanceof OperationList) {
							gen = ((OperationList) op).get(profile);
						} else {
							gen = op.get();
						}
					}

					log("Starting generation for genome " + i + " of " + getGenomes().size());
					gen.run();
				} finally {
					long generationTime = System.currentTimeMillis() - start;
					StableDurationHealthComputation.recordGenerationTime(frames, generationTime);

					out.write().get().run();

					if (outputPath != null) {
						try {
							File fftFile = HealthComputationAdapter.getAuxFile(new File(outputPath),
									HealthComputationAdapter.FFT_SUFFIX);
							out.getWaveData().fft().store(fftFile);
						} catch (IOException e) {
							warn("Could not store FFT", e);
						}
					}

					out.reset();
					if (cells != null) cells.reset();

					disableGenome();

					if (outputPath != null)
						output.accept(new GenerationResult(outputPath, getGenomes().get(i), generationTime));
				}
			}
		};
	}


	public void store(OutputStream s) {
		store(getGenomes(), s);
	}

	@Override
	public void destroy() {
		Destroyable.super.destroy();
		if (cells instanceof Destroyable) ((Destroyable) cells).destroy();
		cells = null;
	}

	@Override
	public Console console() { return HealthCallable.console; }

	public static class GenerationResult {
		private String outputPath;
		private Genome<PackedCollection<?>> genome;
		private long generationTime;

		public GenerationResult(String outputPath, Genome<PackedCollection<?>> genome, long generationTime) {
			this.outputPath = outputPath;
			this.genome = genome;
			this.generationTime = generationTime;
		}

		public String getOutputPath() {
			return outputPath;
		}

		public Genome<PackedCollection<?>> getGenome() {
			return genome;
		}

		public long getGenerationTime() {
			return generationTime;
		}
	}

	public static <G> void store(List<Genome<G>> genomes, OutputStream s) {
		try (XMLEncoder enc = new XMLEncoder(s)) {
			for (int i = 0; i < genomes.size(); i++) {
				enc.writeObject(genomes.get(i));
			}

			enc.flush();
		}
	}

	public static List<Genome<PackedCollection<?>>> read(InputStream in) {
		List<Genome<PackedCollection<?>>> genomes = new ArrayList<>();

		try (XMLDecoder dec = new XMLDecoder(in)) {
			Object read = null;

			while ((read = dec.readObject()) != null) {
				genomes.add((Genome) read);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// End of file
		}

		return genomes;
	}
}
