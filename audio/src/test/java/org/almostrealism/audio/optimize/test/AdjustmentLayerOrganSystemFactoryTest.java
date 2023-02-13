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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.optimize.AudioSceneGenome;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.audio.optimize.CellularAudioOptimizer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class AdjustmentLayerOrganSystemFactoryTest extends AudioSceneTest {
	protected Genome genome() {
		int dim = 2;

		ArrayListChromosome<PackedCollection<?>> generators = new ArrayListChromosome();
		generators.add(g(0.4, 0.6, DefaultAudioGenome.factorForRepeat(8),
				DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));

		for (int i = 0; i < dim - 1; i++) {
			generators.add(g(0.8, 0.2, DefaultAudioGenome.factorForRepeat(8),
					DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));
		}

		ArrayListChromosome<PackedCollection<?>> volume = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i -> g(0.02, 0.01)).forEach(volume::add);

		ArrayListChromosome<PackedCollection<?>> mainFilterUp = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i ->
				g(DefaultAudioGenome.factorForPeriodicAdjustmentDuration(10),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(180),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(1.0))).forEach(mainFilterUp::add);

		ArrayListChromosome<PackedCollection<?>> wetIn = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i -> g(0.1, 0.0)).forEach(wetIn::add);

		ArrayListChromosome<PackedCollection<?>> processors = new ArrayListChromosome();
		processors.add(g(delayParam,
				DefaultAudioGenome.factorForSpeedUpDuration(360),
				DefaultAudioGenome.factorForSpeedUpPercentage(1.0),
				DefaultAudioGenome.factorForSlowDownDuration(360),
				DefaultAudioGenome.factorForSlowDownPercentage(1.0),
				DefaultAudioGenome.factorForPolySpeedUpDuration(360),
				DefaultAudioGenome.factorForPolySpeedUpExponent(1.0)));
		processors.add(g(delayParam,
				DefaultAudioGenome.factorForSpeedUpDuration(360),
				DefaultAudioGenome.factorForSpeedUpPercentage(1.0),
				DefaultAudioGenome.factorForSlowDownDuration(360),
				DefaultAudioGenome.factorForSlowDownPercentage(1.0),
				DefaultAudioGenome.factorForPolySpeedUpDuration(360),
				DefaultAudioGenome.factorForPolySpeedUpExponent(1.0)));

		ArrayListChromosome<PackedCollection<?>> transmission = new ArrayListChromosome();

		if (enableDelay) {
			transmission.add(new ArrayListGene<>(0.0, feedbackParam));
			transmission.add(new ArrayListGene<>(feedbackParam, 0.0));
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
		genome.add(volume);
		genome.add(mainFilterUp);
		genome.add(wetIn);
		genome.add(processors);
		genome.add(transmission);
		genome.add(c(g(0.0, 0.1)));  // WET OUT
		genome.add(filters);
		genome.add(c(g(0.0, 0.0, 0.0), g(0.0, 0.0, 0.0)));

		return genome;
	}

	@Test
	public void compare() {
		dc(() -> {
			ReceptorCell outa = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-comp-a.wav")).get(0);
			Cells organa = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), outa);
			organa.reset();

			ReceptorCell outb = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-comp-b.wav")).get(0);
			Cells organb = cells(pattern(2, 2), null, outb); // TODO
			organb.reset();

			Runnable organRunA = new TemporalRunner(organa, 8 * OutputLine.sampleRate).get();
			Runnable organRunB = new TemporalRunner(organb, 8 * OutputLine.sampleRate).get();

			organRunA.run();
			((WaveOutput) outa.getReceptor()).write().get().run();

			organRunB.run();
			((WaveOutput) outb.getReceptor()).write().get().run();
		});
	}

	@Test
	public void layered() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();

		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	@Test
	public void layeredRandom() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-rand-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
