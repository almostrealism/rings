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
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.Scale;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import io.almostrealism.code.OperationAdapter;
import org.almostrealism.algebra.Scalar;
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
import org.almostrealism.organs.SimpleOrgan;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.stream.IntStream;

public class StableDurationHealthComputationTest extends AdjustableDelayCellTest {
	public static final boolean enableDelay = true;
	public static final boolean enableFilter = true;

	@BeforeClass
	public static void init() {
		StableDurationHealthComputation.enableVerbose = true;
	}

	@AfterClass
	public static void shutdown() {
		StableDurationHealthComputation.enableVerbose = false;
	}

	protected DefaultDesirablesProvider notes() {
		Scale<WesternChromatic> scale = Scale.of(WesternChromatic.G4, WesternChromatic.A3);
		return new DefaultDesirablesProvider<>(120, scale);
	}

	protected DefaultDesirablesProvider samples() {
		DefaultDesirablesProvider desirables = new DefaultDesirablesProvider(120);
		desirables.getSamples().add(new File("src/test/resources/Snare Perc DD.wav"));
		return desirables;
	}

	protected SimpleOrgan<Scalar> organ(DesirablesProvider desirables) {
		ArrayListChromosome<Double> generators = new ArrayListChromosome();
		generators.add(new ArrayListGene<>(0.4, 0.6));
		generators.add(new ArrayListGene<>(0.8, 0.2));

		ArrayListChromosome<Double> processors = new ArrayListChromosome();
		processors.add(new ArrayListGene<>(1.0, 0.4));
		processors.add(new ArrayListGene<>(1.0, 0.2));

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();

		if (enableDelay) {
			transmission.add(new ArrayListGene<>(0.0, 0.4));
			transmission.add(new ArrayListGene<>(0.4, 0.0));
		} else {
			transmission.add(new ArrayListGene<>(0.0, 0.0));
			transmission.add(new ArrayListGene<>(0.0, 0.0));
		}

		ArrayListChromosome<Double> filters = new ArrayListChromosome();

		if (enableFilter) {
			filters.add(new ArrayListGene<>(0.15, 1.0));
			filters.add(new ArrayListGene<>(0.15, 1.0));
		} else {
			filters.add(new ArrayListGene<>(0.0, 1.0));
			filters.add(new ArrayListGene<>(0.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(processors);
		genome.add(transmission);
		genome.add(filters);

		SimpleOrganGenome organGenome = new SimpleOrganGenome(2);
		organGenome.assignTo(genome);

		return SimpleOrganFactory.getDefault(desirables).generateOrgan(organGenome);
	}

	@Test
	public void cells() {
		WaveOutput output1 = new WaveOutput(new File("health/health-test-firstcell.wav"));
		WaveOutput output2 = new WaveOutput(new File("health/health-test-lastcell.wav"));
		// WaveOutput output3 = new WaveOutput(new File("health/health-test-firstcell-processed.wav"));
		// WaveOutput output4 = new WaveOutput(new File("health/health-test-lastcell-processed.wav"));

		SimpleOrgan<Scalar> organ = organ(notes());
		((CellAdapter) organ.firstCell()).setMeter(output1);
		((CellAdapter) organ.lastCell()).setMeter(output2);

		organ.setup().get().run();
		Runnable push = organ.push(v(0.0)).get();
		((OperationAdapter) push).compile();

		Runnable tick = organ.tick().get();
		((OperationAdapter) tick).compile();
		System.out.println(((DynamicAcceleratedOperation) tick).getFunctionDefinition());

		IntStream.range(0, 5 * OutputLine.sampleRate).forEach(i -> {
			push.run();
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

		SimpleOrgan<Scalar> organ = organ(notes());
		organ.setMonitor(health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void simpleOrganHealthSamples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/simple-organ-samples-test.wav");

		SimpleOrgan<Scalar> organ = organ(samples());
		organ.setMonitor(health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}
}
