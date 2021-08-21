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

package org.almostrealism.optimize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.almostrealism.relation.Generated;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.organs.OrganFactory;
import org.almostrealism.io.Console;
import org.almostrealism.organs.Organ;

import org.almostrealism.population.Population;

public class PopulationOptimizer<T, O extends Organ<T>> implements Generated<Supplier<Genome>, PopulationOptimizer> {
	public static Console console = new Console();

	public static boolean enableVerbose = false;

	public static int popSize = 60;
	public static int maxChildren = 50;
	public static double secondaryOffspringPotential = 0.0;
	public static double teriaryOffspringPotential = 0.0;
	public static double quaternaryOffspringPotential = 0.0;
	public static double lowestHealth = 0.0;

	private Population<T, O> population;
	private Function<List<Genome>, Population> children;
	private Supplier<Genome> generator;

	private HealthComputation<T> health;
	private GenomeBreeder breeder;

	public PopulationOptimizer(HealthComputation<T> h,
							   Function<List<Genome>, Population> children,
							   GenomeBreeder breeder, Supplier<Genome> generator) {
		this(null, h, children, breeder, generator);
	}

	public PopulationOptimizer(Population<T, O> p, HealthComputation<T> h,
							   Function<List<Genome>, Population> children,
							   GenomeBreeder breeder, Supplier<Genome> generator) {
		this.population = p;
		this.health = h;
		this.children = children;
		this.breeder = breeder;
		this.generator = generator;
	}

	public void setPopulation(Population<T, O> population) { this.population = population; }

	public Population<T, O> getPopulation() { return this.population; }

	public void setHealthComputation(HealthComputation<T> h) { this.health = h; }

	public HealthComputation<T> getHealthComputation() { return health; }

	public void setChildrenFunction(Function<List<Genome>, Population> pop) { this.children = pop; }

	public Function<List<Genome>, Population> getChildrenFunction() { return children; }

	@Override
	public Supplier<Genome> getGenerator() { return generator; }

	public void iterate() {
		long start = System.currentTimeMillis();

		// Sort the population
		SortedSet<Genome> sorted = orderByHealth(population);

		// Fresh genetic material
		List<Genome> genomes = new ArrayList<>();

		// Mate in order of health
		Iterator<Genome> itr = sorted.iterator();

		Genome g1;
		Genome g2 = itr.next();

		w: for (int i = 0; itr.hasNext(); i++) {
			g1 = g2;
			if (genomes.size() >= maxChildren || itr.hasNext() == false) break w;
			g2 = itr.next();

			// Combine chromosomes to produce new offspring
			breed(genomes, g1, g2);

			if (StrictMath.random() < secondaryOffspringPotential && genomes.size() < maxChildren) {
				// Combine chromosomes to produce a second offspring
				breed(genomes, g1, g2);
			} else {
				continue w;
			}

			if (StrictMath.random() < teriaryOffspringPotential && genomes.size() < maxChildren) {
				// Combine chromosomes to produce a third offspring
				breed(genomes, g1, g2);
			} else {
				continue w;
			}

			if (StrictMath.random() < quaternaryOffspringPotential && genomes.size() < maxChildren) {
				// Combine chromosomes to produce a fourth offspring
				breed(genomes, g1, g2);
			} else {
				continue w;
			}
		}

		int add = popSize - genomes.size();

		console.println("Generating new population with " + genomes.size() + " children");

		this.population = children.apply(genomes);

		if (generator != null && add > 0) {
			console.println("Adding an additional " + add + " members");

			List<Genome> addGenomes = IntStream.range(0, add)
					.mapToObj(i -> generator.get())
					.collect(Collectors.toList());
			Population<T, O> addPop = children.apply(addGenomes);
			this.population.merge(addPop);
		}

		long sec = (System.currentTimeMillis() - start) / 1000;

		if (enableVerbose)
			console.println("Iteration completed after " + sec + " seconds");
	}

	public void breed(List<Genome> genomes, Genome g1, Genome g2) {
		genomes.add(breeder.combine(g1, g2));
	}

	private SortedSet<Genome> orderByHealth(Population<T, O> pop) {
		final HashMap<Genome, Double> healthTable = new HashMap<>();

		double highestHealth = 0;
		double totalHealth = 0;

		console.print("Calculating health.");
		if (enableVerbose) console.println();

		for (int i = 0; i < pop.size(); i++) {
			try {
				System.out.println();
				System.out.println("Enabling genome:");
				System.out.println(pop.getGenomes().get(i));
				O o = pop.enableGenome(i);
				double health = this.health.computeHealth(o);

				if (health > highestHealth) highestHealth = health;

				healthTable.put(pop.getGenomes().get(i), health);
				totalHealth += health;
			} finally {
				this.health.reset();
				pop.disableGenome();
			}

			if (enableVerbose) {
				console.println("Health of Organ " + i + " is " + health);
			} else {
				console.print(".");
			}

			System.gc();
		}

		if (!enableVerbose) console.println();

		console.println("Average health for this round is " +
				percent(totalHealth / pop.size()) + ", max " + highestHealth);
		TreeSet<Genome> sorted = new TreeSet<>((g1, g2) -> {
			double h1 = healthTable.get(g1);
			double h2 = healthTable.get(g2);

			int i = (int) ((h2 - h1) * 10000000);

			if (i == 0) {
				if (h1 > h2) {
					return -1;
				} else {
					return 1;
				}
			}

			return i;
		});

		for (int i = 0; i < pop.size(); i++) {
			Genome g = pop.getGenomes().get(i);
			if (healthTable.get(g) >= lowestHealth) sorted.add(g);
		}

		return sorted;
	}

	public Console getConsole() { return console; }

	public static String percent(double d) {
		int cents = (int) (d * 100);
		int decimal = (int) (((d * 100) - cents) * 100);
		if (decimal < 0) decimal = -decimal;
		String decimalString = String.valueOf(decimal);
		if (decimalString.length() < 2) decimalString = "0" + decimalString;
		return cents + "." + decimalString + "%";
	}

	/*
	public static void main(String args[]) throws FileNotFoundException {
		if (args.length > 0 && args[0].equals("help")) {
			console.println("Usage:");
			console.println("SimpleOrganOptimizer [total iterations] [population size] [minimum cell feedback] [maximum cell feedback]");
		}

		int tot = 1000;
		int dim = 4;

		double min = defaultMinFeedback;
		double max = defaultMaxFeedback;

		if (args.length > 0) tot = Integer.parseInt(args[0]);
		if (args.length > 1) { popSize = Integer.parseInt(args[1]); maxPop = popSize; }
		if (args.length > 2) min = Double.parseDouble(args[2]);
		if (args.length > 3) max = Double.parseDouble(args[3]);

		// Audio protein
		AudioProteinCache.addWait = 0;
		AudioProteinCache cache = new AudioProteinCache();

		// Random genetic material generators
		FloatingPointRandomChromosomeFactory xfactory = new FloatingPointRandomChromosomeFactory();
		DefaultRandomChromosomeFactory yfactory = new DefaultRandomChromosomeFactory(min, max);
		xfactory.setChromosomeSize(dim, dim);
		yfactory.setChromosomeSize(dim, dim);

		// Population of organs
		SimpleOrganPopulationGenerator<Long> generator = new SimpleOrganPopulationGenerator<Long>(xfactory, yfactory);
		SimpleOrganPopulation<Long> pop;

		if (new File("Population.xml").exists()) {
			pop = new SimpleOrganPopulation<>();
			pop.read(new FileInputStream("Population.xml"));
			console.println("Read chromosome data from Population.xml");
		} else {
			pop = generator.generatePopulation(popSize, 1.0, 1.0);
			console.println("Generated initial population");
		}

		pop.init(SimpleOrganFactory.defaultFactory);
		pop.setProteinCache(cache);

		console.println(pop.size() + " organs in population");

		// Health calculation algorithm
		AverageHealthComputationSet<Long> health = new AverageHealthComputationSet<>();
		health.add(new StableDurationHealthComputation(cache));

		if (enableSilenceDurationHealthComputation)
			health.add(new SilenceDurationHealthComputation(cache, 3));

		// Create and run the optimizer
		SimpleOrganOptimizer<Long> opt = new SimpleOrganOptimizer<Long>(cache, pop, SimpleOrganFactory.defaultFactory, health,
														Breeders.randomChoiceBreeder(), Breeders.randomChoiceBreeder());
		opt.setGenerator(generator);

		if (enableWavOutput) {
			health.addListener((hc, o) -> {
				if (hc instanceof StableDurationHealthComputation) {
					((StableDurationHealthComputation) hc)
							.setDebugOutputFile("health/Organ-" + opt.getPopulation().indexOf((SimpleOrgan) o) + ".wav");
				}
			});
		}

		for (int i = 0; i < tot; i++) {
			opt.iterate();

			if (i != 0 && i % 10 == 0) {
				opt.getPopulation().store(new FileOutputStream("Population.xml"));
				console.println("Wrote Population.xml");
			}

			System.gc();
		}

		opt.getPopulation().store(new FileOutputStream("Population.xml"));
		console.println("Wrote Population.xml");
	}
	 */
}
