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
import org.almostrealism.time.TemporalRunner;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory;
import com.almostrealism.audio.optimize.LayeredOrganGenome;
import com.almostrealism.audio.optimize.LayeredOrganOptimizer;
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
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class AdjustmentLayerOrganSystemFactoryTest extends GeneticTemporalFactoryFromDesirablesTest {
	protected AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar> factory(DesirablesProvider desirables) {
		TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(DefaultCellAdjustmentFactory.Type.PERIODIC));
		return new AdjustmentLayerOrganSystemFactory(tca, new GeneticTemporalFactoryFromDesirables().from(desirables));
	}

	protected Genome layeredOrganGenome() {
		ArrayListChromosome<Double> generators = new ArrayListChromosome();
		generators.add(new ArrayListGene<>(0.4, 0.6));
		generators.add(new ArrayListGene<>(0.8, 0.2));

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

		if (enableFilter) {
			filters.add(new ArrayListGene<>(0.15, 1.0));
			filters.add(new ArrayListGene<>(0.15, 1.0));
		} else {
			filters.add(new ArrayListGene<>(0.0, 1.0));
			filters.add(new ArrayListGene<>(0.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(c(g(0.8), g(0.8))); // VOLUME
		genome.add(processors);
		genome.add(transmission);
		genome.add(filters);
		genome.add(c(g(0.0, 0.0, 0.0), g(0.0, 0.0, 0.0)));

		return genome;
	}

	protected AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> layeredOrgan(DesirablesProvider desirables, List<? extends Receptor<Scalar>> measures, Receptor<Scalar> meter) {
		LayeredOrganGenome organGenome = new LayeredOrganGenome(2);
		organGenome.assignTo(layeredOrganGenome());
		return factory(desirables).generateOrgan((Genome) organGenome, measures, meter);
	}

	public AdjustmentLayerOrganSystem<Scalar, Scalar, Double, Scalar> randomLayeredOrgan(DesirablesProvider desirables,  List<? extends Receptor<Scalar>> measures, Receptor<Scalar> meter) {
		LayeredOrganGenome g = new LayeredOrganGenome(2);
		g.assignTo(LayeredOrganOptimizer.generator(2).get().get());
		return factory(desirables).generateOrgan((Genome) g, measures, meter);
	}

	@Test
	public void compare() {
		dc(() -> {
			ReceptorCell outa = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-a.wav")).get(0);
			Cells organa = organ(samples(), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), outa);
			organa.reset();

			ReceptorCell outb = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-b.wav")).get(0);
			AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organb = layeredOrgan(samples(), null, outb); // TODO
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
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = layeredOrgan(samples(), null, out); // TODO
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
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = randomLayeredOrgan(samples(), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}