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
import com.almostrealism.audio.filter.test.AdjustableDelayCellTest;
import com.almostrealism.audio.health.OrganRunner;
import com.almostrealism.audio.optimize.LayeredOrganOptimizer;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.audio.optimize.SimpleOrganGenome;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.Scale;
import com.almostrealism.tone.WesternChromatic;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.junit.Test;

import java.io.File;
import java.util.stream.IntStream;

public class GeneticTemporalFactoryFromDesirablesTest extends AdjustableDelayCellTest implements CellFeatures {
	public static final boolean enableDelay = true;
	public static final boolean enableFilter = true;

	public static final double delayParam = 0.35;
	public static final double delay = 60 * ((1 / (1 - Math.pow(delayParam, 3))) - 1);

	public static final double feedbackParam = 0.1;

	public static final String sampleFile = "src/test/resources/Snare Perc DD.wav";

	protected DefaultDesirablesProvider notes() {
		Scale<WesternChromatic> scale = Scale.of(WesternChromatic.G4, WesternChromatic.A3);
		return new DefaultDesirablesProvider<>(120, scale);
	}

	protected DefaultDesirablesProvider samples() {
		DefaultDesirablesProvider desirables = new DefaultDesirablesProvider(120);
		desirables.getSamples().add(new File(sampleFile));
		return desirables;
	}

	protected Cells organ(DesirablesProvider desirables, Receptor<Scalar> meter) {
		return organ(desirables, meter, enableFilter);
	}

	protected Cells organ(DesirablesProvider desirables, Receptor<Scalar> meter, boolean filter) {
		ArrayListChromosome<Double> generators = new ArrayListChromosome();
		generators.add(new ArrayListGene<>(0.4, 0.6));
		generators.add(new ArrayListGene<>(0.8, 0.2));

		ArrayListChromosome<Scalar> volume = new ArrayListChromosome();
		volume.add(g(0.8));
		volume.add(g(0.8));

		ArrayListChromosome<Double> processors = new ArrayListChromosome();
		processors.add(new ArrayListGene<>(1.0, delayParam));
		processors.add(new ArrayListGene<>(1.0, delayParam));

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();

		if (enableDelay) {
			transmission.add(new ArrayListGene<>(0.0, feedbackParam));
			transmission.add(new ArrayListGene<>(feedbackParam, 0.0));
		} else {
			transmission.add(new ArrayListGene<>(0.0, 0.0));
			transmission.add(new ArrayListGene<>(0.0, 0.0));
		}

		ArrayListChromosome<Double> filters = new ArrayListChromosome();

		if (filter) {
			filters.add(new ArrayListGene<>(0.15, 1.0));
			filters.add(new ArrayListGene<>(0.15, 1.0));
		} else {
			filters.add(new ArrayListGene<>(0.0, 1.0));
			filters.add(new ArrayListGene<>(0.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(volume);
		genome.add(processors);
		genome.add(transmission);
		genome.add(filters);

		SimpleOrganGenome organGenome = new SimpleOrganGenome(2);
		organGenome.assignTo(genome);

		return new GeneticTemporalFactoryFromDesirables().from(desirables).generateOrgan(organGenome, meter);
	}

	public Cells randomOrgan(DesirablesProvider desirables, Receptor<Scalar> meter) {
		LayeredOrganOptimizer.GeneratorConfiguration conf = new LayeredOrganOptimizer.GeneratorConfiguration();
		conf.minDelay = delay;
		conf.maxDelay = delay;
		conf.minTransmission = feedbackParam;
		conf.maxTransmission = feedbackParam;
		conf.minHighPass = 0;
		conf.maxHighPass = 0;
		conf.minLowPass = 20000;
		conf.maxLowPass = 20000;

		Genome g = LayeredOrganOptimizer.generator(2, conf).get().get();
		System.out.println(g);

		SimpleOrganGenome sog = new SimpleOrganGenome(2);
		sog.assignTo(g);

		return new GeneticTemporalFactoryFromDesirables().from(desirables).generateOrgan(sog, meter);
	}

	public void comparison(boolean twice) {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("organ-factory-test-a.wav")).get(0);
		Cells organ = organ(samples(), out);
		organ.reset();

		CellList list = poly(2, PolymorphicAudioData::new, i -> v(0.5), sampleFile, sampleFile)
				 .d(i -> new Scalar(delay))
//				 .m(fc(i -> hp(2000, 0.1)),
//						c(g(0.0, feedbackParam), g(feedbackParam, 0.0)))
				.o(i -> new File("organ-factory-test-b" + i + ".wav"));

		Runnable organRun = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();
		Runnable listRun = list.sec(8).get();

		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();

		if (twice) {
			organRun.run();
			((WaveOutput) out.getReceptor()).write().get().run();
		}

		listRun.run();
	}

	@Test
	public void comparisonOnce() { comparison(false); }

	@Test
	public void comparisonTwice() { comparison(true); }

	@Test
	public void many() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("organ-factory-many-test.wav")).get(0);
		Cells organ = organ(samples(), out);

		Runnable run = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();

		IntStream.range(0, 10).forEach(i -> {
			run.run();
			((WaveOutput) out.getReceptor()).write().get().run();
			organ.reset();
		});
	}

	@Test
	public void random() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("organ-factory-rand-test.wav")).get(0);
		Cells organ = randomOrgan(samples(), out);
		organ.reset();

		Runnable organRun = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
