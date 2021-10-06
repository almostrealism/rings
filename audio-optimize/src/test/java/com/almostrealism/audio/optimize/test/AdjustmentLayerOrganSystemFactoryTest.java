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
import com.almostrealism.audio.health.OrganRunner;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory;
import com.almostrealism.audio.optimize.LayeredOrganGenome;
import com.almostrealism.audio.optimize.LayeredOrganOptimizer;
import com.almostrealism.audio.optimize.SimpleOrganFactory;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.junit.Test;

import java.io.File;

public class AdjustmentLayerOrganSystemFactoryTest extends SimpleOrganFactoryTest {
	protected AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar> factory(DesirablesProvider desirables) {
		TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(DefaultCellAdjustmentFactory.Type.PERIODIC));
		return new AdjustmentLayerOrganSystemFactory(tca, SimpleOrganFactory.getDefault(desirables));
	}

	protected AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> layeredOrgan(DesirablesProvider desirables) {
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
		genome.add(processors);
		genome.add(transmission);
		genome.add(filters);
		genome.add(c(g(0.0, 0.0, 0.0), g(0.0, 0.0, 0.0)));

		LayeredOrganGenome organGenome = new LayeredOrganGenome(2);
		organGenome.assignTo(genome);

		return factory(desirables).generateOrgan(organGenome);
	}

	public AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> randomOrgan(DesirablesProvider desirables) {
		return factory(desirables).generateOrgan(LayeredOrganOptimizer.generator(2).get());
	}

	@Test
	public void compare() {
		ReceptorCell outa = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-a.wav")).get(0);
		SimpleOrgan<Scalar> organa = organ(samples());
		organa.setMonitor(outa);
		organa.reset();

		ReceptorCell outb = (ReceptorCell) o(1, i -> new File("layered-organ-factory-comp-b.wav")).get(0);
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organb = layeredOrgan(samples());
		organb.setMonitor(outb);
		organb.reset();

		Runnable organRunA = new OrganRunner(organa, 8 * OutputLine.sampleRate).get();
		Runnable organRunB = new OrganRunner(organb, 8 * OutputLine.sampleRate).get();

		organRunA.run();
		((WaveOutput) outa.getReceptor()).write().get().run();

		organRunB.run();
		((WaveOutput) outb.getReceptor()).write().get().run();
	}

	@Test
	public void layered() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-factory-test.wav")).get(0);
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = layeredOrgan(samples());
		organ.setMonitor(out);
		organ.reset();

		Runnable organRun = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	@Test
	public void layeredRandom() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-factory-rand-test.wav")).get(0);
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = randomOrgan(samples());
		organ.setMonitor(out);
		organ.reset();

		Runnable organRun = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
