/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.filter.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.audio.tone.WesternChromatic;
import  org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.hardware.mem.MemoryBankAdapter.CacheLevel;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class AssignableGenomeTest implements CellFeatures {
	@BeforeClass
	public static void init() {
		// AcceleratedTimeSeries.defaultCacheLevel = CacheLevel.ALL;
		StableDurationHealthComputation.enableVerbose = true;
	}

	@AfterClass
	public static void shutdown() {
		AcceleratedTimeSeries.defaultCacheLevel = CacheLevel.NONE;
		StableDurationHealthComputation.enableVerbose = false;
	}

	protected AudioScene<?> scene() {
		return new AudioScene<>(null, 120, 2, 2, OutputLine.sampleRate);
	}

	protected Genome genome1() {
		ArrayListChromosome<Scalar> x = new ArrayListChromosome();
		x.add(new ArrayListGene<>(0.5468, 0.7491));
		x.add(new ArrayListGene<>(0.1541, 0.8454));

		ArrayListChromosome<Scalar> y = new ArrayListChromosome();
		y.add(new ArrayListGene<>(0.5637, 0.5149));
		y.add(new ArrayListGene<>(0.2710, 0.4959));

		ArrayListChromosome<Scalar> z = new ArrayListChromosome();
		z.add(new ArrayListGene<>(0.6777, 0.3041));
		z.add(new ArrayListGene<>(0.9222, 0.0999));

		ArrayListChromosome<Scalar> a = new ArrayListChromosome();
		a.add(new ArrayListGene<>(0.8690, 0.3474, 0.5589));
		a.add(new ArrayListGene<>(0.7410, 0.4526, 0.4011));

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(x);
		genome.add(y);
		genome.add(z);
		genome.add(a);
		return genome;
	}

	protected Cells cells(Receptor<PackedCollection<?>> meter) {
		List<Frequency> frequencies = new DefaultKeyboardTuning().getTones(WesternScales.major(WesternChromatic.G3, 1));

		CellList cells =
					w(frequencies.iterator().next(), frequencies.iterator().next())
							.d(i -> scalar(1.0))
							.mself(fc(i -> new AudioPassFilter(OutputLine.sampleRate, c(0.0), scalar(0.1), true)
										.andThen(new AudioPassFilter(OutputLine.sampleRate, c(20000), scalar(0.1), false))),
									i -> {
										if (i == 0) {
											return g(0.0, 1.0);
										} else {
											return g(1.0, 0.0);
										}
									});

			((CellAdapter) cells.get(cells.size() - 1)).setMeter(meter);
			return cells;
	}

	@Test
	public void cellExamples() {
		AcceleratedTimeSeries.defaultCacheLevel = CacheLevel.ALL;

		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/assignable-genome-cells-example.wav")).get(0);

		Cells organ = cells(out);

		TemporalRunner runner = new TemporalRunner(organ, 8 * OutputLine.sampleRate);
		Runnable run = runner.get();

		run.run();
		((WaveOutput) out.getReceptor()).write().get().run();
		((WaveOutput) out.getReceptor()).reset();
		organ.reset();

		run.run();
		((WaveOutput) out.getReceptor()).write().get().run();
		((WaveOutput) out.getReceptor()).reset();
		organ.reset();
	}
}
