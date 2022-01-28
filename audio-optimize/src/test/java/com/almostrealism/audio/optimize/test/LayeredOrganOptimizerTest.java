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

package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.filter.test.AssignableGenomeTest;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.AudioPopulationOptimizer;
import com.almostrealism.audio.optimize.LayeredOrganOptimizer;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class LayeredOrganOptimizerTest extends AssignableGenomeTest {
	protected Supplier<GeneticTemporalFactory<Scalar, Scalar, Cells>> factorySupplier() {
		DesirablesProvider desirables = new DefaultDesirablesProvider<>(120, WesternScales.major(WesternChromatic.G3, 1));
		return () -> new GeneticTemporalFactoryFromDesirables().from(desirables);
	}

	protected LayeredOrganOptimizer optimizer() {
		int sources = 2;
		int delayLayers = 2;
		int cycles = 1;

		List<Genome<Scalar>> genomes = new ArrayList<>();
		genomes.add(genome(0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, false));

		Supplier<GeneticTemporalFactory<Scalar, Scalar, Cells>> factorySupplier = factorySupplier();

		LayeredOrganOptimizer optimizer = new LayeredOrganOptimizer(null, () -> {
			return new DefaultGenomeBreeder(
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // GENERATORS
					Breeders.averageBreeder(),  									   // VOLUME
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
					Breeders.averageBreeder(),  									   // FILTER
					Breeders.perturbationBreeder(0.005, ScaleFactor::new));  // PERIODIC
		}, null, OutputLine.sampleRate, sources, delayLayers, cycles);

		optimizer.setChildrenFunction(g -> {
			System.out.println("Creating LayeredOrganPopulation...");
			LayeredOrganPopulation<Scalar, Scalar> pop = new LayeredOrganPopulation<>(genomes, sources, delayLayers, OutputLine.sampleRate);
			AudioHealthComputation hc = (AudioHealthComputation) optimizer.getHealthComputation();
			pop.init(factorySupplier.get(), pop.getGenomes().get(0), hc.getMeasures(), hc.getOutput());
			return pop;
		});

		return optimizer;
	}

	@Test
	public void optimize() {
		Hardware.enableVerbose = true;
		PopulationOptimizer.enableVerbose = true;
		optimizer().run();
	}

	@Test
	public void healthTest() throws FileNotFoundException {
		Supplier<GeneticTemporalFactory<Scalar, Scalar, Cells>> factorySupplier = factorySupplier();

		AtomicInteger index = new AtomicInteger();

		List<Genome<Scalar>> genomes = new ArrayList<>();
		genomes.add(genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(genome(0.0, 0.0, false));

		AudioPopulationOptimizer.store(genomes, new FileOutputStream("Population.xml"));

		IntStream.range(0, 3).forEach(j ->
				dc(() -> {
					StableDurationHealthComputation health = new StableDurationHealthComputation();
					health.setMaxDuration(8);

					health.setOutputFile(() -> "health/layered-organ-optimizer-test-" + index.incrementAndGet() + ".wav");

					System.out.println("Creating LayeredOrganPopulation...");
					LayeredOrganPopulation<Scalar, Scalar> pop =
							new LayeredOrganPopulation<>(AudioPopulationOptimizer.read(new FileInputStream("Population.xml")), 2, OutputLine.sampleRate);
					pop.init(factorySupplier.get(), pop.getGenomes().get(0), health.getMeasures(), health.getOutput());

					IntStream.range(0, 4).forEach(i -> {
						Cells organ = pop.enableGenome(i);

						try {
							health.setTarget(organ);
							health.computeHealth();
						} finally {
							health.reset();
							pop.disableGenome();
						}
					});

					return null;
				}));
	}
}
