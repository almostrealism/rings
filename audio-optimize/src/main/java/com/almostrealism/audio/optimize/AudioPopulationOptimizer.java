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

import com.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.optimize.HealthComputation;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.organs.Organ;
import org.almostrealism.population.Population;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AudioPopulationOptimizer<O extends Organ<Scalar>> extends PopulationOptimizer<Scalar, O> implements Runnable {
	public static final boolean enableWavOutput = true;

	private final String file;
	private final int tot;

	public AudioPopulationOptimizer(Function<List<Genome>, Population> children,
									GenomeBreeder breeder, Supplier<Genome> generator, String file) {
		this(children, breeder, generator, file, 100);
	}

	public AudioPopulationOptimizer(Function<List<Genome>, Population> children,
									GenomeBreeder breeder, Supplier<Genome> generator,
									String file, int iterationsPerRun) {
		this(healthComputation(), children, breeder, generator, file, iterationsPerRun);
	}

	public AudioPopulationOptimizer(HealthComputation<Scalar> health,
				Function<List<Genome>, Population> children,
				GenomeBreeder breeder, Supplier<Genome> generator,
									String file, int iterationsPerRun) {
		super(health, children, breeder, generator);
		this.file = file;
		this.tot = iterationsPerRun;
	}

	public void init() throws FileNotFoundException {
		if (enableWavOutput) {
			AtomicInteger count = new AtomicInteger();
			((StableDurationHealthComputation) getHealthComputation()).setDebugOutputFile(() -> "health/Output-" + count.incrementAndGet() + ".wav");
		}

		List<Genome> genomes;

		if (new File(file).exists()) {
			genomes = read(new FileInputStream(file));
			PopulationOptimizer.console.println("Read chromosome data from " + file);
		} else {
			genomes = Optional.ofNullable(getGenerator())
					.map(gen -> IntStream.range(0, PopulationOptimizer.popSize)
						.mapToObj(i -> gen.get()).collect(Collectors.toList()))
					.orElseGet(ArrayList::new);
			PopulationOptimizer.console.println("Generated initial population");
		}

		init(genomes);
	}

	public void init(List<Genome> genomes) {
		if (enableWavOutput) {
			AtomicInteger count = new AtomicInteger();
			((StableDurationHealthComputation) getHealthComputation()).setDebugOutputFile(() -> "health/Output-" + count.incrementAndGet() + ".wav");
		}

		setPopulation(getChildrenFunction().apply(genomes));
		PopulationOptimizer.console.println("Loaded genomes");
		PopulationOptimizer.console.println(getPopulation().size() + " organs in population");
	}

	@Override
	public void run() {
		for (int i = 0; i < tot; i++) {
			iterate();
			storePopulation();

			System.gc();
		}
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

	public static void store(List<Genome> genomes, OutputStream s) {
		try (XMLEncoder enc = new XMLEncoder(s)) {
			for (int i = 0; i < genomes.size(); i++) {
				enc.writeObject(genomes.get(i));
			}

			enc.flush();
		}
	}

	public static List<Genome> read(InputStream in) {
		List<Genome> genomes = new ArrayList<>();

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

	public static HealthComputation<Scalar> healthComputation() {
		return new StableDurationHealthComputation();
	}
}
