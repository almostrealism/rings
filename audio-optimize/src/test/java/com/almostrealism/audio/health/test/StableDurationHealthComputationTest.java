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

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.AudioPopulationOptimizer;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import com.almostrealism.audio.optimize.test.LayeredOrganPopulationTest;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import io.almostrealism.code.OperationAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.hardware.DynamicAcceleratedOperation;
import org.almostrealism.heredity.Genome;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
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

		CellList organ = (CellList) organ(notes(), null);
		((CellAdapter) organ.get(0)).setMeter(output1);
		((CellAdapter) organ.get(1)).setMeter(output2);

		organ.setup().get().run();

		Runnable tick = organ.tick().get();
		((OperationAdapter) tick).compile();
		System.out.println(((DynamicAcceleratedOperation) tick).getFunctionDefinition());

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
			health.setDebugOutputFile(() -> "health/simple-organ-notes-test" + index.incrementAndGet() + ".wav");

			Cells organ = organ(notes(), health.getMonitor(), false);
			organ.reset();
			health.computeHealth(organ);
			organ.reset();
			health.computeHealth(organ);
		});
	}

	@Test
	public void simpleOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/simple-organ-samples-test.wav");

		Cells organ = organ(samples(), health.getMonitor());
		health.computeHealth(organ);
		health.reset();
		health.computeHealth(organ);
		health.reset();
	}

	@Test
	public void layeredOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/layered-organ-samples-test.wav");

		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = layeredOrgan(samples(), health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void layeredOrganHealthSamplesRand() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/layered-organ-samples-rand-test.wav");

		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = randomLayeredOrgan(samples(), health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void layeredOrganHealthSamplesPopulation() throws FileNotFoundException {
		Supplier<AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar>> factorySupplier = () -> {
			TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(DefaultCellAdjustmentFactory.Type.PERIODIC));
			return new AdjustmentLayerOrganSystemFactory(tca, new GeneticTemporalFactoryFromDesirables().from(samples()));
		};

		AtomicInteger index = new AtomicInteger();

		List<Genome> genomes = new ArrayList<>();
		genomes.add(layeredOrganGenome());
		genomes.add(layeredOrganGenome());

		AudioPopulationOptimizer.store(genomes, new FileOutputStream("Population.xml"));

		IntStream.range(0, 3).forEach(j ->
			dc(() -> {
				StableDurationHealthComputation health = new StableDurationHealthComputation();
				health.setMaxDuration(8);

				health.setDebugOutputFile(() -> "health/layered-organ-samples-pop-test-" + index.incrementAndGet() + ".wav");

				System.out.println("Creating LayeredOrganPopulation...");
				LayeredOrganPopulation<Double, Scalar, Double, Scalar> pop =
						new LayeredOrganPopulation<>(AudioPopulationOptimizer.read(new FileInputStream("Population.xml")), 2, OutputLine.sampleRate);
				pop.init(factorySupplier.get(), pop.getGenomes().get(0), health.getMonitor());

				IntStream.range(0, 2).forEach(i -> {
					AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = pop.enableGenome(i);
					try {
						health.computeHealth(organ);
					} finally {
						health.reset();
						pop.disableGenome();
					}
				});

				return null;
			}));
	}
}
