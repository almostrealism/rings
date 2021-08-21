package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.filter.test.AssignableGenomeTest;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory.Type;
import com.almostrealism.audio.optimize.LayeredOrganOptimizer;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import com.almostrealism.audio.optimize.SimpleOrganFactory;
import com.almostrealism.sound.DefaultDesirablesProvider;
import com.almostrealism.tone.WesternChromatic;
import com.almostrealism.tone.WesternScales;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LayeredOrganOptimizerTest extends AssignableGenomeTest {
	protected LayeredOrganOptimizer optimizer() {
		int dim = 2;

		/*
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
		 */

		List<Genome> genomes = new ArrayList<>();
		genomes.add(genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(genome(0.4, 0.6, 0.8, 0.2, false));
		genomes.add(genome(0.8, 0.3, 0.8, 0.2, false));
		genomes.add(genome1());

		DesirablesProvider desirables = new DefaultDesirablesProvider<>(120, WesternScales.major(WesternChromatic.G3, 1));

		TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));
		TieredCellAdjustmentFactory<Scalar, Scalar> tcb = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.EXPONENTIAL), tca);
		AdjustmentLayerOrganSystemFactory<Double, Scalar, Double, Scalar> factory = new AdjustmentLayerOrganSystemFactory(tca, SimpleOrganFactory.getDefault(desirables));

		DefaultGenomeBreeder breeder = new DefaultGenomeBreeder(
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // GENERATORS
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
				Breeders.perturbationBreeder(0.005, ScaleFactor::new));  // PERIODIC

		LayeredOrganOptimizer optimizer = new LayeredOrganOptimizer(null, breeder, null, 3);
		optimizer.setChildrenFunction(g -> {
			LayeredOrganPopulation<Double, Scalar, Double, Scalar> pop = new LayeredOrganPopulation<>(genomes);
			pop.init(factory, pop.getGenomes().get(0), ((AudioHealthComputation) optimizer.getHealthComputation()).getMonitor());
			return pop;
		});

		return optimizer;
	}

	@Test
	public void optimize() throws FileNotFoundException {
		LayeredOrganOptimizer optimizer = optimizer();
		optimizer.init();
		optimizer.run();
	}
}
