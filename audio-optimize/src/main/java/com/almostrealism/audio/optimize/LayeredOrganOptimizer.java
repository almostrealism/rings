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

package com.almostrealism.audio.optimize;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Supplier;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.health.HealthComputationAdapter;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory.Type;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.FloatingPointRandomChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.optimize.HealthComputation;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;

public class LayeredOrganOptimizer extends AudioPopulationOptimizer<AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar>> {
	public static final double defaultMinFeedback = 0.01;
	public static final double defaultMaxFeedback = 0.5;

	private HealthComputation<Scalar> health;

	public LayeredOrganOptimizer(AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar> f,
								 GenomeBreeder breeder, Supplier<Genome> generator, int totalCycles) {
		super(null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					LayeredOrganPopulation<Double, Scalar, Double, Scalar> pop = new LayeredOrganPopulation<>(children);
					pop.init(f, pop.getGenomes().get(0), ((AudioHealthComputation) getHealthComputation()).getMonitor());
					return pop;
				});
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int cycles) {
		int dim = 3;

		// Random genetic material generators
		ChromosomeFactory<Scalar> xfactory = new FloatingPointRandomChromosomeFactory(); // GENERATORS
		ChromosomeFactory<Scalar> yfactory = new FloatingPointRandomChromosomeFactory(); // DELAY
		ChromosomeFactory<Scalar> zfactory = new FloatingPointRandomChromosomeFactory(); // ROUTING
		ChromosomeFactory<Scalar> afactory = new FloatingPointRandomChromosomeFactory(); // PERIODIC
		ChromosomeFactory<Scalar> bfactory = new FloatingPointRandomChromosomeFactory(); // EXPONENTIAL
		xfactory.setChromosomeSize(dim, 2); // GENERATORS
		yfactory.setChromosomeSize(dim, 2); // DELAY
		zfactory.setChromosomeSize(dim, dim);      // ROUTING
		afactory.setChromosomeSize(dim, 3); // PERIODIC
		bfactory.setChromosomeSize(dim, 2); // EXPONENTIAL

		GenomeFromChromosomes generator = new GenomeFromChromosomes(xfactory, yfactory, zfactory, afactory); //, bfactory);

		TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));
		TieredCellAdjustmentFactory<Scalar, Scalar> tcb = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.EXPONENTIAL), tca);
		AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar> factory = new AdjustmentLayerOrganSystemFactory(tca, SimpleOrganFactory.getDefault(desirables));

		DefaultGenomeBreeder breeder = new DefaultGenomeBreeder(
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // GENERATORS
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
				Breeders.perturbationBreeder(0.005, ScaleFactor::new)); //,   // PERIODIC
				// Breeders.perturbationBreeder(0.0001, ScaleFactor::new)); // EXPONENTIAL

		return new LayeredOrganOptimizer(factory, breeder, generator, cycles);
	}

	/**
	 * Build a {@link LayeredOrganOptimizer} and initialize and run it.
	 *
	 * @see  LayeredOrganOptimizer#build(DesirablesProvider, int)
	 * @see  LayeredOrganOptimizer#init
	 * @see  LayeredOrganOptimizer#run()
	 */
	public static void main(String args[]) throws FileNotFoundException {
		DefaultDesirablesProvider provider = new DefaultDesirablesProvider<>(116, WesternScales.major(WesternChromatic.G3, 1));
		provider.getSamples().add(new File("audio-health/src/main/resources/health-test-in.wav"));

		LayeredOrganOptimizer opt = build(provider, 25);
		opt.init();
		opt.run();
	}
}
