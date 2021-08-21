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
import com.almostrealism.audio.health.HealthComputationAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.heredity.ChromosomeBreeder;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.FloatingPointRandomChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.organs.SimpleOrgan;

public class SimpleOrganOptimizer extends AudioPopulationOptimizer<SimpleOrgan<Scalar>> {
	private static double defaultMinFeedback = 0.01;
	private static double defaultMaxFeedback = 0.5;
	
	public SimpleOrganOptimizer(SimpleOrganFactory<Scalar, DesirablesProvider> f,
								ChromosomeBreeder<Scalar> xb, ChromosomeBreeder<Scalar> yb,
								Supplier<Genome> generator) {
		super(SimpleOrganPopulation::new, new DefaultGenomeBreeder(xb, yb), generator, "Population.xml");
	}

	public static void main(String args[]) throws FileNotFoundException {
		if (args.length > 0 && args[0].equals("help")) {
			PopulationOptimizer.console.println("Usage:");
			PopulationOptimizer.console.println("SimpleOrganOptimizer [total iterations] [population size] [minimum cell feedback] [maximum cell feedback]");
		}
		
		int tot = 100;
		int dim = 4;
		
		double min = defaultMinFeedback;
		double max = defaultMaxFeedback;
		
		if (args.length > 0) tot = Integer.parseInt(args[0]);
		if (args.length > 1) { PopulationOptimizer.popSize = Integer.parseInt(args[1]); PopulationOptimizer.maxChildren = PopulationOptimizer.popSize; }
		if (args.length > 2) min = Double.parseDouble(args[2]);
		if (args.length > 3) max = Double.parseDouble(args[3]);
		
		// Random genetic material generators
		FloatingPointRandomChromosomeFactory xfactory = new FloatingPointRandomChromosomeFactory();
		FloatingPointRandomChromosomeFactory yfactory = new FloatingPointRandomChromosomeFactory();
		xfactory.setChromosomeSize(dim, 2);
		yfactory.setChromosomeSize(dim, dim);
		
		// Population of organs
		GenomeFromChromosomes generator = new GenomeFromChromosomes(xfactory, yfactory);

		// Create and run the optimizer
		SimpleOrganOptimizer opt = new SimpleOrganOptimizer(SimpleOrganFactory.getDefault(null),
													Breeders.perturbationBreeder(0.0005, ScaleFactor::new),
													Breeders.perturbationBreeder(0.0005, ScaleFactor::new),
													generator);
		opt.init();
		opt.run();
	}
}
