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
import com.almostrealism.audio.filter.test.AdjustableDelayCellTest;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.SimpleOrganFactory;
import com.almostrealism.audio.optimize.SimpleOrganGenome;
import com.almostrealism.audio.optimize.test.AdjustmentLayerOrganSystemFactoryTest;
import com.almostrealism.audio.optimize.test.LayeredOrganPopulationTest;
import com.almostrealism.audio.optimize.test.SimpleOrganFactoryTest;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.Scale;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import io.almostrealism.code.OperationAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.hardware.DynamicAcceleratedOperation;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.SimpleOrgan;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
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
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/simple-organ-notes-test.wav");

		Cells organ = organ(notes(), health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void simpleOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/simple-organ-samples-test.wav");

		Cells organ = organ(samples(), health.getMonitor());
		health.computeHealth(organ);
		health.computeHealth(organ);
	}

	@Test
	public void layeredOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/layered-organ-samples-test.wav");

		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = layeredOrgan(samples(), health.getMonitor());
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
	public void layeredOrganHealthSamplesPopulation() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/layered-organ-samples-pop-test.wav");

		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = organFromPopulation(samples(), health.getMonitor());
		health.computeHealth(organ);
	}
}
