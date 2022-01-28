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

package com.almostrealism.audio.health.test;

import com.almostrealism.audio.health.SilenceDurationHealthComputation;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.AudioPopulationOptimizer;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import com.almostrealism.audio.optimize.test.LayeredOrganPopulationTest;
import io.almostrealism.code.OperationAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.heredity.Genome;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class StableDurationHealthComputationTest extends LayeredOrganPopulationTest {

	@BeforeClass
	public static void init() {
		StableDurationHealthComputation.enableVerbose = true;
	}

	@AfterClass
	public static void shutdown() {
		StableDurationHealthComputation.enableVerbose = false;
	}

	@Test
	public void cells() {
		WaveOutput output1 = new WaveOutput(new File("health/health-test-firstcell.wav"));
		WaveOutput output2 = new WaveOutput(new File("health/health-test-lastcell.wav"));
		// WaveOutput output3 = new WaveOutput(new File("health/health-test-firstcell-processed.wav"));
		// WaveOutput output4 = new WaveOutput(new File("health/health-test-lastcell-processed.wav"));

		CellList organ = (CellList) organ(notes(), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), null);
		((CellAdapter) organ.get(0)).setMeter(output1);
		((CellAdapter) organ.get(1)).setMeter(output2);

		organ.setup().get().run();

		Runnable tick = organ.tick().get();
		((OperationAdapter) tick).compile();

		IntStream.range(0, 5 * OutputLine.sampleRate).forEach(i -> {
			tick.run();
			if ((i + 1) % 1000 == 0) System.out.println("StableDurationHealthComputationTest: " + (i + 1) + " iterations");
		});

		System.out.println("StableDurationHealthComputationTest: Writing WAVs...");
		output1.write().get().run();
		output2.write().get().run();
		// output3.write().get().run();
		// output4.write().get().run();
		System.out.println("Done");
	}

	@Test
	public void simpleOrganHealthNotes() {
		AtomicInteger index = new AtomicInteger();

		dc(() -> {
			StableDurationHealthComputation health = new StableDurationHealthComputation();
			health.setMaxDuration(8);
			health.setOutputFile(() -> "health/simple-organ-notes-test" + index.incrementAndGet() + ".wav");

			Cells organ = organ(notes(), health.getMeasures(), health.getOutput(), false);
			organ.reset();
			health.setTarget(organ);
			health.computeHealth();

			organ.reset();
			health.setTarget(organ);
			health.computeHealth();
		});
	}

	@Test
	public void simpleOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setOutputFile("health/simple-organ-samples-test.wav");

		Cells organ = organ(samples(), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), health.getOutput());

		health.setTarget(organ);
		health.computeHealth();
		health.reset();

		health.setTarget(organ);
		health.computeHealth();
		health.reset();
	}

	@Test
	public void layeredOrganHealthSamples() {
		SilenceDurationHealthComputation.enableSilenceCheck = false;
		GeneticTemporalFactoryFromDesirables.enableMainFilterUp = true;
		GeneticTemporalFactoryFromDesirables.enableEfxFilters = false;
		Hardware.getLocalHardware().setMaximumOperationDepth(9);
		StableDurationHealthComputation.setStandardDuration(150);

		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setOutputFile("health/layered-organ-samples-test.wav");

		Cells organ = layeredOrgan(samples(), health.getMeasures(), health.getOutput());

		organ.reset();
		health.setTarget(organ);
		health.computeHealth();

//		organ.reset();
//		health.setTarget(organ);
//		health.computeHealth();
	}

	@Test
	public void layeredOrganHealthSamplesRand() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setOutputFile("health/layered-organ-samples-rand-test.wav");

		Cells organ = randomLayeredOrgan(samples(), health.getMeasures(), health.getOutput());
		organ.reset();
		health.setTarget(organ);
		health.computeHealth();
	}

	@Test
	public void layeredOrganHealthSamplesPopulation() throws FileNotFoundException {
		Supplier<GeneticTemporalFactory<Scalar, Scalar, Cells>> factorySupplier = () -> new GeneticTemporalFactoryFromDesirables().from(samples());

		AtomicInteger index = new AtomicInteger();

		List<Genome<Scalar>> genomes = new ArrayList<>();
		genomes.add(layeredOrganGenome());
		genomes.add(layeredOrganGenome());

		AudioPopulationOptimizer.store(genomes, new FileOutputStream("Population.xml"));

		IntStream.range(0, 3).forEach(j ->
			dc(() -> {
				StableDurationHealthComputation health = new StableDurationHealthComputation();
				health.setMaxDuration(8);

				health.setOutputFile(() -> "health/layered-organ-samples-pop-test-" + index.incrementAndGet() + ".wav");

				System.out.println("Creating LayeredOrganPopulation...");
				LayeredOrganPopulation<Scalar, Scalar> pop =
						new LayeredOrganPopulation<>(AudioPopulationOptimizer.read(new FileInputStream("Population.xml")), 2, OutputLine.sampleRate);
				pop.init(factorySupplier.get(), pop.getGenomes().get(0), health.getMeasures(), health.getOutput());

				IntStream.range(0, 2).forEach(i -> {
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
