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

package com.almostrealism.audio.filter.test;

import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory.Type;
import com.almostrealism.audio.optimize.SimpleOrganFactory;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.breeding.AssignableGenome;
import org.almostrealism.hardware.mem.MemoryBankAdapter.CacheLevel;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class AssignableGenomeTest {
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

	protected AdjustmentLayerOrganSystemFactory<Scalar, Long, Scalar, Long> factory() {
		TieredCellAdjustmentFactory<Scalar, Scalar> tca =
				new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));

		DefaultDesirablesProvider<WesternChromatic> provider = new DefaultDesirablesProvider<>(120, WesternScales.major(WesternChromatic.G3, 1));
//		provider.getSamples().add(new File("src/main/resources/health-test-in.wav"));

		return new AdjustmentLayerOrganSystemFactory<>(tca, SimpleOrganFactory.getDefault(provider));
	}

	protected Genome genome(double x1a, double x1b, double x2a, double x2b, boolean adjust) {
		ArrayListChromosome<Scalar> x = new ArrayListChromosome();
		x.add(new ArrayListGene<>(x1a, x1b));
		x.add(new ArrayListGene<>(x2a, x2b));

		ArrayListChromosome<Scalar> y = new ArrayListChromosome();
		y.add(new ArrayListGene<>(1.0, 0.2));
		y.add(new ArrayListGene<>(1.0, 0.2));

		ArrayListChromosome<Scalar> z = new ArrayListChromosome();
		z.add(new ArrayListGene<>(0.0, 1.0));
		z.add(new ArrayListGene<>(1.0, 0.0));

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
		return genome;
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

	protected AdjustmentLayerOrganSystem organ(AssignableGenome genome) {
		genome.assignTo(genome(0.0, 0.0, 0.0, 0.0, false));
		return factory().generateOrgan(genome);
	}

	@Test
	public void examples() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);

		AssignableGenome genome = new AssignableGenome();
		AdjustmentLayerOrganSystem organ = organ(genome);
		organ.setMonitor(health.getMonitor());

		genome.assignTo(genome1());
		health.setDebugOutputFile("health/genome1.wav");
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void healthTestNoAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);

		AssignableGenome genome = new AssignableGenome();
		AdjustmentLayerOrganSystem organ = organ(genome);
		organ.setMonitor(health.getMonitor());

		genome.assignTo(genome(0.4, 0.6, 0.8, 0.2, false));
		health.setDebugOutputFile("health/assignable-genome-test-noadjust-1.wav");
		organ.reset();
		health.computeHealth(organ);

		genome.assignTo(genome(0.8, 0.3, 0.8, 0.2, false));
		health.setDebugOutputFile("health/assignable-genome-test-noadjust-2.wav");
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void healthTestWithAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);

		AssignableGenome genome = new AssignableGenome();
		AdjustmentLayerOrganSystem organ = organ(genome);
		organ.setMonitor(health.getMonitor());

		genome.assignTo(genome(0.4, 0.6, 0.8, 0.2, true));
		health.setDebugOutputFile("health/assignable-genome-test-adjust-1.wav");
		organ.reset();
		health.computeHealth(organ);

		genome.assignTo(genome(0.8, 0.3, 0.8, 0.2, true));
		health.setDebugOutputFile("health/assignable-genome-test-adjust-2.wav");
		organ.reset();
		health.computeHealth(organ);
	}
}
