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
import com.almostrealism.audio.optimize.GeneticTemporalFactoryFromDesirables;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.computations.PairFromScalars;
import org.almostrealism.algebra.computations.ScalarProduct;
import org.almostrealism.algebra.computations.ScalarSum;
import org.almostrealism.algebra.computations.StaticScalarComputation;
import org.almostrealism.audio.filter.AdjustableDelayCell;
import org.almostrealism.audio.filter.CellAdjustment;
import org.almostrealism.audio.filter.PeriodicAdjustment;
import org.almostrealism.graph.AdjustmentCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.mem.MemoryBankAdapter.CacheLevel;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.almostrealism.util.TestFeatures;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

	protected AdjustmentLayerOrganSystemFactory<Scalar, Scalar, Scalar, Scalar> factory() {
		TieredCellAdjustmentFactory<Scalar, Scalar> tca =
				new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));

		DefaultDesirablesProvider<WesternChromatic> provider = new DefaultDesirablesProvider<>(120, WesternScales.major(WesternChromatic.G3, 1));
//		provider.getSamples().add(new File("src/main/resources/health-test-in.wav"));

		return new AdjustmentLayerOrganSystemFactory<>(tca, new GeneticTemporalFactoryFromDesirables().from(provider));
	}

	protected AdjustmentLayerOrganSystem organ(boolean adjust, Receptor<Scalar> meter) {
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

		return factory().generateOrgan(genome, meter);
	}

	@Test
	public void adjustment() {
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = organ(true, null);

		Runnable setup = organ.setup().get();
		Runnable tick = organ.tick().get();

		List<Supplier<Evaluable<? extends Pair>>> bounds =
				IntStream.range(0, 2)
					.mapToObj(i ->
						(PeriodicAdjustment) ((AdjustmentCell) ((SimpleOrgan) organ.getOrgan(1)).getCell(i)).getAdjustment())
					.map(CellAdjustment::getBounds).collect(Collectors.toList());

		PairFromScalars s = (PairFromScalars) bounds.get(0);
		ScalarProduct l = (ScalarProduct) s.getInputs().get(1);
		ScalarSum r = (ScalarSum) s.getInputs().get(2);
		ScalarProduct r1 = (ScalarProduct) r.getInputs().get(1);
		ScalarProduct r2 = (ScalarProduct) r.getInputs().get(2);

		ScalarSum r2_1 = (ScalarSum) r2.getInputs().get(1);

		StaticScalarComputation r2_1_1 = (StaticScalarComputation) r2_1.getInputs().get(1);
		ScalarProduct r2_1_2 = (ScalarProduct) r2_1.getInputs().get(2);

		assertEquals(1.0, r2_1_1.get().evaluate());

		IntStream.range(0, 20).forEach(i -> {
			setup.run();
			assertEquals(1.0, r.get().evaluate());
			tick.run();
		});

		System.out.println(((AdjustableDelayCell) organ.getCell(0)).getScale());
	}

	@Test
	public void healthTestNoAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/periodic-test-noadjust.wav");

		AdjustmentLayerOrganSystem organ = organ(false, health.getMonitor());
		organ.setMonitor(health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}

	@Test
	public void healthTestWithAdjustment() {
		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile("health/periodic-test-adjust.wav");

		AdjustmentLayerOrganSystem organ = organ(true, health.getMonitor());
		organ.setMonitor(health.getMonitor());
		organ.reset();
		health.computeHealth(organ);
	}
}
