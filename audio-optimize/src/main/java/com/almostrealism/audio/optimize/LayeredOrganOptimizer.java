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
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory.Type;
import com.almostrealism.sound.DefaultDesirablesProvider;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WavFile;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.RandomChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.AdjustmentLayerOrganSystemFactory;
import org.almostrealism.organs.TieredCellAdjustmentFactory;

public class LayeredOrganOptimizer extends AudioPopulationOptimizer<AdjustmentLayerOrganSystem<Scalar, Scalar, Double, Scalar>> {

	public LayeredOrganOptimizer(Supplier<AdjustmentLayerOrganSystemFactory<Scalar, Scalar, Double, Scalar>> f,
								 Supplier<GenomeBreeder<Scalar>> breeder, Supplier<Supplier<Genome<Scalar>>> generator,
								 int sampleRate, int cellCount, int totalCycles) {
		super(null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					LayeredOrganPopulation<Scalar, Scalar, Double, Scalar> pop = new LayeredOrganPopulation(children, cellCount, sampleRate);
					AudioHealthComputation hc = (AudioHealthComputation) getHealthComputation();
					pop.init(f.get(), pop.getGenomes().get(0), hc.getMeasures(), hc.getOutput());
					return pop;
				});
	}

	public static Supplier<Supplier<Genome<Scalar>>> generator(int dim) {
		return generator(dim, new GeneratorConfiguration(dim));
	}

	public static Supplier<Supplier<Genome<Scalar>>> generator(int dim, GeneratorConfiguration config) {
		return () -> {
			// Random genetic material generators
			ChromosomeFactory<Scalar> generators = SimpleOrganGenome.generatorFactory();   // GENERATORS
			RandomChromosomeFactory volume = new RandomChromosomeFactory();       // VOLUME
			RandomChromosomeFactory processors = new RandomChromosomeFactory();   // DELAY
			RandomChromosomeFactory transmission = new RandomChromosomeFactory(); // ROUTING
			RandomChromosomeFactory wet = new RandomChromosomeFactory();		  // WET
			RandomChromosomeFactory filters = new RandomChromosomeFactory();      // FILTERS
			RandomChromosomeFactory afactory = new RandomChromosomeFactory();     // PERIODIC
			RandomChromosomeFactory bfactory = new RandomChromosomeFactory();     // EXPONENTIAL

			generators.setChromosomeSize(dim, 1); // GENERATORS

			volume.setChromosomeSize(dim, 2);     // VOLUME
			Pair volumeRange = new Pair(config.minVolume, config.maxVolume);
			IntStream.range(0, dim).forEach(i -> {
				volume.setRange(i, 0, volumeRange);
				volume.setRange(i, 1, volumeRange);
			});

			processors.setChromosomeSize(dim, 3); // DELAY
			Pair delayRange = new Pair(SimpleOrganGenome.factorForDelay(config.minDelay),
									SimpleOrganGenome.factorForDelay(config.maxDelay));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 0, delayRange));

			transmission.setChromosomeSize(dim, dim);    // ROUTING
			Pair transmissionRange = new Pair(config.minTransmission, config.maxTransmission);
			IntStream.range(0, dim).forEach(i -> IntStream.range(0, dim)
					.forEach(j -> transmission.setRange(i, j, transmissionRange)));

			wet.setChromosomeSize(1, dim);		 // WET
			Pair wetRange = new Pair(config.minWet, config.maxWet);
			IntStream.range(0, dim).forEach(i -> wet.setRange(0, i, wetRange));

			filters.setChromosomeSize(dim, 2);    // FILTERS
			Pair hpRange = new Pair(SimpleOrganGenome.factorForFilterFrequency(config.minHighPass),
					SimpleOrganGenome.factorForFilterFrequency(config.maxHighPass));
			Pair lpRange = new Pair(SimpleOrganGenome.factorForFilterFrequency(config.minLowPass),
					SimpleOrganGenome.factorForFilterFrequency(config.maxLowPass));
			IntStream.range(0, dim).forEach(i -> {
				filters.setRange(i, 0, hpRange);
				filters.setRange(i, 1, lpRange);
			});

			afactory.setChromosomeSize(dim, 3);   // PERIODIC
			bfactory.setChromosomeSize(dim, 2);   // EXPONENTIAL

			return Genome.fromChromosomes(generators, volume, processors, transmission, wet, filters, afactory); //, bfactory);
		};
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int cycles) {
		return build(desirables, 3, cycles);
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int dim, int cycles) {
		return build(generator(dim), desirables, dim, cycles);
	}

	public static LayeredOrganOptimizer build(Supplier<Supplier<Genome<Scalar>>> generator, DesirablesProvider desirables, int dim, int cycles) {
		return new LayeredOrganOptimizer(() -> {
			TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));
			TieredCellAdjustmentFactory<Scalar, Scalar> tcb = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.EXPONENTIAL), tca);
			return new AdjustmentLayerOrganSystemFactory(tca, new GeneticTemporalFactoryFromDesirables().from(desirables));
		}, () -> {
			return new DefaultGenomeBreeder(
					Breeders.randomChoiceBreeder(),  								   // GENERATORS
					Breeders.averageBreeder(),  									   // VOLUME
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
					Breeders.averageBreeder(),  									   // WET
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // FILTERS
					Breeders.perturbationBreeder(0.005, ScaleFactor::new)); //,   // PERIODIC
			// Breeders.perturbationBreeder(0.0001, ScaleFactor::new)); // EXPONENTIAL
		}, generator, OutputLine.sampleRate, dim, cycles);
	}

	/**
	 * Build a {@link LayeredOrganOptimizer} and initialize and run it.
	 *
	 * @see  LayeredOrganOptimizer#build(DesirablesProvider, int)
	 * @see  LayeredOrganOptimizer#init
	 * @see  LayeredOrganOptimizer#run()
	 */
	public static void main(String args[]) throws FileNotFoundException {
		Hardware.enableVerbose = true;
		PopulationOptimizer.enableVerbose = true;

		DefaultDesirablesProvider provider = new DefaultDesirablesProvider<>(116);

		Stream.of(new File("Library").listFiles()).map(f -> {
			try {
				return WavFile.openWavFile(f).getSampleRate() == OutputLine.sampleRate ? f : null;
			} catch (Exception e) {
				return null;
			}
		}).filter(Objects::nonNull).forEach(provider.getSamples()::add);

		LayeredOrganOptimizer opt = build(provider, 25);
		opt.init();
		opt.run();
	}

	public static class GeneratorConfiguration {
		public double minVolume, maxVolume;
		public double minTransmission, maxTransmission;
		public double minDelay, maxDelay;
		public double minWet, maxWet;
		public double minHighPass, maxHighPass;
		public double minLowPass, maxLowPass;

		public GeneratorConfiguration() { this(1); }

		public GeneratorConfiguration(int scale) {
			minVolume = 0.0;
			maxVolume = 1.0 / scale;
			minTransmission = 0.0;
			maxTransmission = 1.0;
			minDelay = 0.5;
			maxDelay = 120;
			minWet = 0.0;
			maxWet = 1.0;
			minHighPass = 0;
			maxHighPass = 0;
			minLowPass = 20000;
			maxLowPass = 20000;
		}
	}
}
