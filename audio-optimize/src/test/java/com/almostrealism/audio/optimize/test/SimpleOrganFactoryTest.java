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
import com.almostrealism.audio.optimize.SimpleOrganFactory;
import com.almostrealism.audio.optimize.SimpleOrganGenome;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.Scale;
import com.almostrealism.tone.WesternChromatic;
import io.almostrealism.code.Setup;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.time.Temporal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class SimpleOrganFactoryTest extends AdjustableDelayCellTest implements CellFeatures {
	public static final boolean enableDelay = true;
	public static final boolean enableFilter = true;

	public static final double delayParam = 0.35;
	public static final double delay = 60 * ((1 / (1 - Math.pow(delayParam, 3))) - 1);

	public static final double feedbackParam = 0.3;

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

	protected SimpleOrgan<Scalar> organ(DesirablesProvider desirables) {
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

		SimpleOrganGenome organGenome = new SimpleOrganGenome(2);
		organGenome.assignTo(genome);

		return SimpleOrganFactory.getDefault(desirables).generateOrgan(organGenome);
	}

	@Test
	public void comparison() throws IOException {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("organ-factory-test-a.wav")).get(0);
		SimpleOrgan<Scalar> organ = organ(samples());
		organ.setMonitor(out);
		organ.reset();

		CellList list = w(sampleFile, sampleFile)
				.d(i -> new Scalar(delay))
				.m(fc(i -> hp(2000, 0.1)),
						c(g(0.0, feedbackParam), g(feedbackParam, 0.0)))
				.o(i -> new File("organ-factory-test-b" + i + ".wav"));

		Runnable organRun = new OrganRunner(organ, 8 * OutputLine.sampleRate).get();
		Runnable listRun = list.sec(8).get();

		organRun.run();
		listRun.run();

		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
