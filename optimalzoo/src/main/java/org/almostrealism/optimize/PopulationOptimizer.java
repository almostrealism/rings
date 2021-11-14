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
import java.util.stream.IntStream;

import io.almostrealism.relation.Generated;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.io.Console;

import org.almostrealism.population.Population;
import org.almostrealism.time.Temporal;
import org.almostrealism.util.CodeFeatures;

public class PopulationOptimizer<G, T, O extends Temporal> implements Generated<Supplier<Genome<G>>, PopulationOptimizer>, CodeFeatures {
	public static Console console = new Console();

	public static boolean enableVerbose = false;

	public static int popSize = 60;
	public static int maxChildren = popSize + 5;
	public static double secondaryOffspringPotential = 0.25;
	public static double tertiaryOffspringPotential = 0.25;
	public static double quaternaryOffspringPotential = 0.25;
	public static double lowestHealth = 0.0;

	private Population<G, T, O> population;
	private Function<List<Genome<G>>, Population> children;

	private Supplier<Supplier<Genome<G>>> generatorSupplier;
	private Supplier<Genome<G>> generator;

	private Supplier<HealthComputation<O>> healthSupplier;
	private HealthComputation<O> health;

	private Supplier<GenomeBreeder<G>> breeder;

	public PopulationOptimizer(Supplier<HealthComputation<O>> h,
							   Function<List<Genome<G>>, Population> children,
							   Supplier<GenomeBreeder<G>> breeder, Supplier<Supplier<Genome<G>>> generator) {
		this(null, h, children, breeder, generator);
	}

	public PopulationOptimizer(Population<G, T, O> p, Supplier<HealthComputation<O>> h,
							   Function<List<Genome<G>>, Population> children,
							   Supplier<GenomeBreeder<G>> breeder, Supplier<Supplier<Genome<G>>> generator) {
		this.population = p;
		this.healthSupplier = h;
		this.children = children;
		this.breeder = breeder;
		this.generatorSupplier = generator;
	}

	public void setPopulation(Population<G, T, O> population) { this.population = population; }

	public Population<G, T, O> getPopulation() { return this.population; }

	public void resetHealth() { health = null; }

	public HealthComputation<?> getHealthComputation() {
		if (health == null) health = healthSupplier.get();
		return health;
	}

	public void setChildrenFunction(Function<List<Genome<G>>, Population> pop) { this.children = pop; }

	public Function<List<Genome<G>>, Population> getChildrenFunction() { return children; }

	public void resetGenerator() {
		generator = null;
	}

	@Override
	public Supplier<Genome<G>> getGenerator() {
		if (generator == null && generatorSupplier != null)
			generator = generatorSupplier.get();
		return generator;
	}

	public void iterate() {
		long start = System.currentTimeMillis();

		// Sort the population
		SortedSet<Genome> sorted = orderByHealth(population);

		// Fresh genetic material
		List<Genome<G>> genomes = new ArrayList<>();

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

			if (StrictMath.random() < tertiaryOffspringPotential && genomes.size() < maxChildren) {
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

		this.population.getGenomes().clear();
		this.population.getGenomes().addAll(genomes);

		if (generator != null && add > 0) {
			console.println("Adding an additional " + add + " members");

			IntStream.range(0, add)
					.mapToObj(i -> generator.get())
					.forEach(this.population.getGenomes()::add);
		}

		long sec = (System.currentTimeMillis() - start) / 1000;

		if (enableVerbose)
			console.println("Iteration completed after " + sec + " seconds");
	}

	public void breed(List<Genome<G>> genomes, Genome g1, Genome g2) {
		genomes.add(breeder.get().combine(g1, g2));
	}

	private SortedSet<Genome> orderByHealth(Population<G, T, O> pop) {
		final HashMap<Genome, Double> healthTable = new HashMap<>();

		double highestHealth = 0;
		double totalHealth = 0;

		console.print("Calculating health");
		if (enableVerbose) {
			console.println("...");
		} else {
			console.print(".");
		}

		for (int i = 0; i < pop.size(); i++) {
			double health = -1;

			try {
				if (enableVerbose) {
					console.println();
					console.println("Enabling genome:");
					console.println(String.valueOf(pop.getGenomes().get(i)));
				}

				this.health.setTarget(pop.enableGenome(i));
				health = this.health.computeHealth();

				if (health > highestHealth) highestHealth = health;

				healthTable.put(pop.getGenomes().get(i), health);
				totalHealth += health;
			} finally {
				this.health.reset();
				pop.disableGenome();
			}

			if (enableVerbose) {
				console.println();
				console.println("Health of Organ " + i + " is " + percent(health));
			} else {
				console.print(".");
			}
		}

		if (!enableVerbose) console.println();

		console.println("Average health for this round is " +
				percent(totalHealth / pop.size()) + ", max " +
				percent(highestHealth));
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
}
