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

package com.almostrealism.audio.filter.test;

import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.Cells;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.mem.MemoryBankAdapter.CacheLevel;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.almostrealism.util.TestFeatures;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class PeriodicCellAdjustmentTest implements TestFeatures {
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

	protected GeneticTemporalFactory<Scalar, Scalar, Cells> factory() {
		DefaultDesirablesProvider<WesternChromatic> provider = new DefaultDesirablesProvider<>(120, WesternScales.major(WesternChromatic.G3, 1));
//		provider.getSamples().add(new File("src/main/resources/health-test-in.wav"));
		return new GeneticTemporalFactoryFromDesirables().from(provider);
	}

	protected Cells organ(boolean adjust, List<? extends Receptor<Scalar>> measures, Receptor<Scalar> output) {
		ArrayListChromosome<Scalar> x = new ArrayListChromosome();
		x.add(new ArrayListGene<>(0.4, 0.6));
		x.add(new ArrayListGene<>(0.8, 0.2));

		ArrayListChromosome<Scalar> y = new ArrayListChromosome();
		y.add(new ArrayListGene<>(1.0, 0.2));
		y.add(new ArrayListGene<>(1.0, 0.2));

		ArrayListChromosome<Scalar> z = new ArrayListChromosome();
		z.add(new ArrayListGene<>(new ScaleFactor(0.0), new ScaleFactor(1.0)));
		z.add(new ArrayListGene<>(new ScaleFactor(1.0), new ScaleFactor(0.0)));

		ArrayListChromosome<Scalar> a = new ArrayListChromosome();

		if (adjust) {
			a.add(new ArrayListGene<>(0.1, 0.0, 1.0));
			a.add(new ArrayListGene<>(0.1, 0.0, 1.0));
		} else {
			a.add(new ArrayListGene<>(0.0, 1.0, 1.0));
			a.add(new ArrayListGene<>(0.0, 1.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(x);
		genome.add(y);
		genome.add(z);
		genome.add(a);

		return factory().generateOrgan(genome, measures, output);
	}

	@Test
	public void healthTestNoAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setOutputFile("health/periodic-test-noadjust.wav");

		Cells organ = organ(false, health.getMeasures(), health.getOutput());
		organ.reset();
		health.setTarget(organ);
		health.computeHealth();
	}

	@Test
	public void healthTestWithAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setOutputFile("health/periodic-test-adjust.wav");

		Cells organ = organ(true, health.getMeasures(), health.getOutput());
		organ.reset();
		health.setTarget(organ);
		health.computeHealth();
	}
}
