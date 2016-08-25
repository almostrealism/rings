/*
 * Copyright 2016 Michael Murray
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

package com.almostrealism.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.almostrealism.breeding.Breeder;
import org.almostrealism.breeding.FloatingPointAverageBreeder;
import org.almostrealism.breeding.LongFactorAverageBreeder;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.DefaultRandomChromosomeFactory;
import org.almostrealism.heredity.FloatingPointRandomChromosomeFactory;
import org.almostrealism.io.Console;
import org.almostrealism.optimize.AverageHealthComputationSet;
import org.almostrealism.optimize.HealthComputation;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.population.SimpleOrganPopulation;
import org.almostrealism.population.SimpleOrganPopulationGenerator;
import org.almostrealism.protein.ProteinCache;

import com.almostrealism.audio.health.SilenceDurationHealthComputation;
import com.almostrealism.audio.health.StableDurationHealthComputation;

public class SimpleOrganOptimizer<T> {
	public static Console console = new Console();
	
	public static boolean enableVerbose = false;
	
	public static boolean enableSilenceDurationHealthComputation = false;
	
	private static double defaultMinFeedback = 0.15;
	private static double defaultMaxFeedback = 0.475;
	
	public static int popSize = 10;
	public static int maxPop = 25;
	public static double secondaryOffspringPotential = 1.0;
	public static double teriaryOffspringPotential = 0.675;
	public static double lowestHealth = 0.45;
	public static double xmutation = 0.0005;
	public static double ymutation = 0.0005;
	
	private ProteinCache<T> cache;
	
	private SimpleOrganPopulation<T> population;
	private SimpleOrganFactory<T> factory;
	private SimpleOrganPopulationGenerator<T> generator;
	
	private HealthComputation<T> health;
	private Breeder<Double> xbreeder;
	private Breeder<T> ybreeder;
	
	public SimpleOrganOptimizer(ProteinCache<T> cache, SimpleOrganPopulation<T> p, SimpleOrganFactory<T> f, HealthComputation<T> h, Breeder<Double> xb, Breeder<T> yb) {
		this.cache = cache;
		this.population = p;
		this.factory = f;
		this.health = h;
		this.xbreeder = xb;
		this.ybreeder = yb;
	}
	
	public SimpleOrganPopulation<T> getPopulation() { return this.population; }
	
	public void setHealthComputation(HealthComputation<T> h) { this.health = h; }
	
	public void setGenerator(SimpleOrganPopulationGenerator<T> generator) { this.generator = generator; }
	
	public void iterate() {
		long start = System.currentTimeMillis();
		
		// Sort the population
		SortedSet<SimpleOrgan<T>> sorted = orderByHealth(population);
		
		// Fresh genetic material
		List<Chromosome<Double>> xSomes = new ArrayList<Chromosome<Double>>();
		List<Chromosome<T>> ySomes = new ArrayList<Chromosome<T>>();
		
		// Mate in order of health
		Iterator<SimpleOrgan<T>> itr = sorted.iterator();
		
		w: for (int i = 0; itr.hasNext() && xSomes.size() < maxPop; i++) {
			SimpleOrgan<T> o1 = itr.next();
			if (itr.hasNext() == false) break w;
			SimpleOrgan<T> o2 = itr.next();
			
			Chromosome<Double> x1 = population.getXChromosome(o1);
			Chromosome<Double> x2 = population.getXChromosome(o2);
			Chromosome<T> y1 = population.getYChromosome(o1);
			Chromosome<T> y2 = population.getYChromosome(o2);
			
			// Combine chromosomes to produce new offspring
			xSomes.add(xbreeder.combine(x1, x2));
			ySomes.add(ybreeder.combine(y1, y2));
			
			if (StrictMath.random() < secondaryOffspringPotential) {
				// Combine chromosomes to produce a second offspring
				xSomes.add(xbreeder.combine(x1, x2));
				ySomes.add(ybreeder.combine(y1, y2));
			} else {
				continue w;
			}
			
			if (StrictMath.random() < teriaryOffspringPotential) {
				// Combine chromosomes to produce a third offspring
				xSomes.add(xbreeder.combine(x1, x2));
				ySomes.add(ybreeder.combine(y1, y2));
			} else {
				continue w;
			}
		} 
		
		int add = popSize - xSomes.size();
		
		console.println("Generating new population with " + xSomes.size() + " members");
		
		this.population = new SimpleOrganPopulation<T>(xSomes, ySomes);
		this.population.init(factory);
		
		if (generator != null && add > 0) {
			console.println("Adding an additional " + add + " members");
			SimpleOrganPopulation<T> addPop = generator.generatePopulation(add, 1.0, 1.0);
			addPop.init(factory);
			this.population.merge(addPop);
		}
		
		this.population.setProteinCache(cache);
		
		long sec = (System.currentTimeMillis() - start) / 1000;
		
		if (enableVerbose)
			console.println("Iteration completed after " + sec + " seconds");
	}
	
	private SortedSet<SimpleOrgan<T>> orderByHealth(SimpleOrganPopulation<T> pop) {
		final HashMap<SimpleOrgan<T>, Double> healthTable = new HashMap<SimpleOrgan<T>, Double>();
		
		double totalHealth = 0;
		
		console.print("Calculating health");
		if (enableVerbose) console.println();
		
		for (int i = 0; i < pop.size(); i++) {
			SimpleOrgan<T> o = pop.getOrgan(i);
			double health = this.health.computeHealth(o);
			
			healthTable.put(o, health);
			totalHealth += health;
			
			if (enableVerbose) {
				console.println("Health of Organ " + i + " is " + health);
			} else {
				console.print(".");
			}
		}
		
		if (!enableVerbose) console.println();
		
		console.println("Average health for this round is " + percent(totalHealth / pop.size()));
		
		TreeSet<SimpleOrgan<T>> sorted = new TreeSet<SimpleOrgan<T>>(new Comparator<SimpleOrgan<T>>() {
			public int compare(SimpleOrgan<T> o1, SimpleOrgan<T> o2) {
				double h1 = healthTable.get(o1);
				double h2 = healthTable.get(o2);
				
				int i = (int) ((h1 - h2) * 10000000);
				
				if (i == 0) {
					if (h1 > h2) {
						return 1;
					} else {
						return -1;
					}
				}
				
				return i;
			}
		});
		
		for (int i = 0; i < pop.size(); i++) {
			SimpleOrgan<T> o = pop.getOrgan(i);
			if (healthTable.get(o) >= lowestHealth) sorted.add(o);
		}
		
		return sorted;
	}
	
	public Console getConsole() { return console; }
	
	public static String percent(double d) {
		int cents = (int) (d * 100);
		int decimal = (int) ((d * 1000) - cents * 10);
		if (decimal < 0) decimal = -decimal;
		return cents + "." + decimal + "%";
	}
	
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
		if (args.length > 1) popSize = Integer.parseInt(args[1]);
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
			pop = new SimpleOrganPopulation<Long>();
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
		AverageHealthComputationSet<Long> health = new AverageHealthComputationSet<Long>();
		health.add(new StableDurationHealthComputation(cache));
		
		if (enableSilenceDurationHealthComputation)
			health.add(new SilenceDurationHealthComputation(cache, 3));
		
		FloatingPointAverageBreeder xbreed = new FloatingPointAverageBreeder();
		LongFactorAverageBreeder ybreed = new LongFactorAverageBreeder();
		
		xbreed.setMutationAmount(xmutation);
		ybreed.setMutationAmount(ymutation);
		
		// Create and run the optimizer
		SimpleOrganOptimizer<Long> opt = new SimpleOrganOptimizer<Long>(cache, pop, SimpleOrganFactory.defaultFactory, health,
																		new FloatingPointAverageBreeder(),
																		new LongFactorAverageBreeder());
		opt.setGenerator(generator);
		
		for (int i = 0; i < tot; i++) {
			opt.iterate();
			
			if (i != 0 && i % 10 == 0) {
				opt.getPopulation().store(new FileOutputStream("Population.xml"));
				console.println("Wrote Population.xml");
			}
		}
		
		opt.getPopulation().store(new FileOutputStream("Population.xml"));
		console.println("Wrote Population.xml");
	}
}
