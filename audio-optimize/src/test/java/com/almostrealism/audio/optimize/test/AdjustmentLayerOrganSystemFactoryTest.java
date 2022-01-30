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

package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.almostrealism.time.TemporalRunner;
import com.almostrealism.audio.optimize.CellularAudioOptimizer;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
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

public class AdjustmentLayerOrganSystemFactoryTest extends GeneticTemporalFactoryFromDesirablesTest {
	protected GeneticTemporalFactory<Scalar, Scalar, Cells> factory(DesirablesProvider desirables) {
		return new GeneticTemporalFactoryFromDesirables().from(desirables);
	}

	protected Genome layeredOrganGenome() {
		int dim = 2;

		ArrayListChromosome<Scalar> generators = new ArrayListChromosome();
		generators.add(g(0.4, 0.6, DefaultAudioGenome.factorForRepeat(8),
				DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));

		for (int i = 0; i < dim - 1; i++) {
			generators.add(g(0.8, 0.2, DefaultAudioGenome.factorForRepeat(8),
					DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));
		}

		ArrayListChromosome<Scalar> volume = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i -> g(0.02, 0.01)).forEach(volume::add);

		ArrayListChromosome<Scalar> mainFilterUp = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i ->
				g(DefaultAudioGenome.factorForPeriodicFilterUpDuration(10),
						DefaultAudioGenome.factorForPolyFilterUpDuration(180),
						DefaultAudioGenome.factorForPolyFilterUpDuration(1.0))).forEach(mainFilterUp::add);

		ArrayListChromosome<Scalar> wetIn = new ArrayListChromosome<>();
		IntStream.range(0, dim).mapToObj(i -> g(0.1, 0.0)).forEach(wetIn::add);

		ArrayListChromosome<Scalar> processors = new ArrayListChromosome();
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

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();

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

	protected Cells layeredOrgan(DesirablesProvider desirables, List<? extends Receptor<Scalar>> measures, Receptor<Scalar> meter) {
		DefaultAudioGenome organGenome = new DefaultAudioGenome(8, 2);
		organGenome.assignTo(layeredOrganGenome());
		return factory(desirables).generateOrgan((Genome) organGenome, measures, meter);
	}

	public Cells randomLayeredOrgan(DesirablesProvider desirables,  List<? extends Receptor<Scalar>> measures, Receptor<Scalar> meter) {
		DefaultAudioGenome g = new DefaultAudioGenome(2, 2);
		g.assignTo(CellularAudioOptimizer.generator(2, 2).get().get());
		return factory(desirables).generateOrgan((Genome) g, measures, meter);
	}

	@Test
	public void compare() {
		dc(() -> {
			ReceptorCell outa = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-a.wav")).get(0);
			Cells organa = organ(samples(), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), outa);
			organa.reset();

			ReceptorCell outb = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-b.wav")).get(0);
			Cells organb = layeredOrgan(samples(), null, outb); // TODO
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
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-factory-test.wav")).get(0);
		Cells organ = layeredOrgan(samples(), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();

		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	@Test
	public void layeredRandom() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-factory-rand-test.wav")).get(0);
		Cells organ = randomLayeredOrgan(samples(), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
