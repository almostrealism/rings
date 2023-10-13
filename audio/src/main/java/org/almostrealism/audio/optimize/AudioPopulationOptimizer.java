/*
 * Copyright 2023 Michael Murray
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

import org.almostrealism.audio.health.AudioHealthScore;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.io.SystemUtils;
import org.almostrealism.optimize.HealthComputation;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.optimize.Population;
import org.almostrealism.time.Temporal;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AudioPopulationOptimizer<O extends Temporal> extends PopulationOptimizer<PackedCollection<?>, PackedCollection<?>, O, AudioHealthScore> implements Runnable {
	public static String outputDir = SystemUtils.getProperty("AR_AUDIO_OUTPUT", "health");

	public static final boolean enableWavOutput = true;
	public static boolean enableStemOutput = true;
	public static boolean enableIsolatedContext = false;

	private final String file;
	private int tot;
	private final AtomicInteger count;

	private Runnable cycleListener;

	public AudioPopulationOptimizer(int stemCount, Function<List<Genome<PackedCollection<?>>>, Population> children,
									Supplier<GenomeBreeder<PackedCollection<?>>> breeder, Supplier<Supplier<Genome<PackedCollection<?>>>> generator,
									String file, int iterationsPerRun) {
		this(() -> healthComputation(stemCount), children, breeder, generator, file, iterationsPerRun);
	}

	public AudioPopulationOptimizer(Supplier<HealthComputation<O, AudioHealthScore>> health,
									Function<List<Genome<PackedCollection<?>>>, Population> children,
									Supplier<GenomeBreeder<PackedCollection<?>>> breeder, Supplier<Supplier<Genome<PackedCollection<?>>>> generator,
									String file, int iterationsPerRun) {
		super(health, children, breeder, generator);
		this.file = file;
		this.tot = iterationsPerRun;
		this.count = new AtomicInteger();
	}

	public void init() {
		if (enableWavOutput) {
			((StableDurationHealthComputation) getHealthComputation()).setOutputFile(() -> outputDir + "/Output-" + count.incrementAndGet() + ".wav");

			if (enableStemOutput) {
				((StableDurationHealthComputation) getHealthComputation()).setStemFile(i -> outputDir + "/Output-" + count.get() + "." + i + ".wav");
			}
		}
	}

	public void setIterationsPerRun(int tot) { this.tot = tot; }

	public void setCycleListener(Runnable r) {
		this.cycleListener = r;
	}

	public void readPopulation() throws FileNotFoundException {
		List<Genome<PackedCollection<?>>> genomes = null;

		if (new File(file).exists()) {
			genomes = read(new FileInputStream(file));
			PopulationOptimizer.console.println("Read chromosome data from " + file);
		}

		if (genomes == null || genomes.isEmpty()) {
			genomes = Optional.ofNullable(getGenerator())
					.map(gen -> IntStream.range(0, PopulationOptimizer.popSize)
							.mapToObj(i -> gen.get()).collect(Collectors.toList()))
					.orElseGet(ArrayList::new);
			PopulationOptimizer.console.println("Generated initial population");
		}

		setPopulation(getChildrenFunction().apply(genomes));
		storePopulation();
		PopulationOptimizer.console.println(getPopulation().size() + " networks in population");
	}

	@Override
	public void run() {
		for (int i = 0; i < tot; i++) {
			if (cycleListener != null) cycleListener.run();

			Callable<Void> c = () -> {
				init();
				readPopulation();
				iterate();
				storePopulation();
				return null;
			};

			if (enableIsolatedContext) {
				dc(c);
			} else {
				try {
					c.call();
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			if (enableIsolatedContext) {
				resetHealth();
				resetGenerator();
			}

			System.gc();
		}
	}

	@Override
	public void breedingComplete() {
		storePopulation();
		count.set(0);
	}

	public void storePopulation() {
		try {
			store(new FileOutputStream(file));
			PopulationOptimizer.console.println("Wrote " + file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void store(OutputStream s) {
		store(getPopulation().getGenomes(), s);
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

	public static <O extends Temporal> HealthComputation<O, AudioHealthScore> healthComputation(int channels) {
		return (HealthComputation<O, AudioHealthScore>) new StableDurationHealthComputation(channels);
	}
}
