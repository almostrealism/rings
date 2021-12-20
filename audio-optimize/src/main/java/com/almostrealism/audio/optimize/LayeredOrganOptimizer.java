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
import com.almostrealism.audio.health.SilenceDurationHealthComputation;
import com.almostrealism.audio.optimize.DefaultCellAdjustmentFactory.Type;
import com.almostrealism.sound.DefaultDesirablesProvider;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBankHeap;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.cl.HardwareOperator;
import org.almostrealism.hardware.jni.NativeComputeContext;
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
	public static final int verbosity = 0;

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
			ChromosomeFactory<Scalar> generators = SimpleOrganGenome.generatorFactory(config.offsetChoices, config.repeatChoices);   // GENERATORS
			RandomChromosomeFactory volume = new RandomChromosomeFactory();       // VOLUME
			RandomChromosomeFactory processors = new RandomChromosomeFactory();   // DELAY
			RandomChromosomeFactory transmission = new RandomChromosomeFactory(); // ROUTING
			RandomChromosomeFactory wet = new RandomChromosomeFactory();		  // WET
			RandomChromosomeFactory filters = new RandomChromosomeFactory();      // FILTERS
			RandomChromosomeFactory afactory = new RandomChromosomeFactory();     // PERIODIC

			generators.setChromosomeSize(dim, 1); // GENERATORS

			volume.setChromosomeSize(dim, 2);     // VOLUME
			Pair volumeRange = new Pair(config.minVolume, config.maxVolume);
			IntStream.range(0, dim).forEach(i -> {
				volume.setRange(i, 0, volumeRange);
				volume.setRange(i, 1, volumeRange);
			});

			processors.setChromosomeSize(dim, 7); // DELAYr
			Pair delayRange = new Pair(SimpleOrganGenome.factorForDelay(config.minDelay),
									SimpleOrganGenome.factorForDelay(config.maxDelay));
			Pair periodicSpeedUpDurationRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSpeedUpDurationMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSpeedUpDurationMax));
			Pair periodicSpeedUpPercentageRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSpeedUpPercentageMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSpeedUpPercentageMax));
			Pair periodicSlowDownDurationRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSlowDownDurationMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSlowDownDurationMax));
			Pair periodicSlowDownPercentageRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSlowDownPercentageMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.periodicSlowDownPercentageMax));
			Pair overallSpeedUpDurationRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.overallSpeedUpDurationMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.overallSpeedUpDurationMax));
			Pair overallSpeedUpExponentRange = new Pair(
					SimpleOrganGenome.factorForSlowDownDuration(config.overallSpeedUpExponentMin),
					SimpleOrganGenome.factorForSlowDownDuration(config.overallSpeedUpExponentMax));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 0, delayRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 1, periodicSpeedUpDurationRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 2, periodicSpeedUpPercentageRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 3, periodicSlowDownDurationRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 4, periodicSlowDownPercentageRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 5, overallSpeedUpDurationRange));
			IntStream.range(0, dim).forEach(i -> processors.setRange(i, 6, overallSpeedUpExponentRange));

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

			return Genome.fromChromosomes(generators, volume, processors, transmission, wet, filters, afactory);
		};
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int cycles) {
		return build(desirables, 6, cycles);
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
		PopulationOptimizer.enableVerbose = verbosity > 0;
		Hardware.enableVerbose = verbosity > 1;
		WaveOutput.enableVerbose = verbosity > 1;
		NativeComputeContext.enableVerbose = verbosity > 2;
		HardwareOperator.enableVerboseLog = verbosity > 2;
		SilenceDurationHealthComputation.enableVerbose = verbosity > 2;

		WavFile.setHeap(() -> new ScalarBankHeap(600 * OutputLine.sampleRate), ScalarBankHeap::destroy);

		DefaultDesirablesProvider provider = new DefaultDesirablesProvider<>(116);

		Stream.of(new File("Library").listFiles()).map(f -> {
			try {
				return WavFile.openWavFile(f).getSampleRate() == OutputLine.sampleRate ? f : null;
			} catch (Exception e) {
				e.printStackTrace();
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

		public double periodicSpeedUpDurationMin, periodicSpeedUpDurationMax;
		public double periodicSpeedUpPercentageMin, periodicSpeedUpPercentageMax;
		public double periodicSlowDownDurationMin, periodicSlowDownDurationMax;
		public double periodicSlowDownPercentageMin, periodicSlowDownPercentageMax;
		public double overallSpeedUpDurationMin, overallSpeedUpDurationMax;
		public double overallSpeedUpExponentMin, overallSpeedUpExponentMax;

		public double minWet, maxWet;
		public double minHighPass, maxHighPass;
		public double minLowPass, maxLowPass;

		public double offsetChoices[];
		public double repeatChoices[];

		public GeneratorConfiguration() { this(1); }

		public GeneratorConfiguration(int scale) {
			minVolume = 0.0;
			maxVolume = 1.0 / scale;
			minTransmission = 0.0;
			maxTransmission = 0.5; // / Math.pow(scale, 1.3);
			minDelay = 0.5;
			maxDelay = 60;

			periodicSpeedUpDurationMin = 0.5;
			periodicSpeedUpDurationMax = 180;
			periodicSpeedUpPercentageMin = 0.0;
			// periodicSpeedUpPercentageMax = 100;
			periodicSpeedUpPercentageMax = 10;

			periodicSlowDownDurationMin = 1;
			periodicSlowDownDurationMax = 180;
			periodicSlowDownPercentageMin = 0.0;
			periodicSlowDownPercentageMax = 0.9;
			// periodicSlowDownPercentageMax = 0.0;

			// overallSpeedUpDurationMin = 0.1;
			overallSpeedUpDurationMin = 10;
			overallSpeedUpDurationMax = 180;

			overallSpeedUpExponentMin = 1;
			// overallSpeedUpExponentMax = 10;
			overallSpeedUpExponentMax = 1;

			minWet = 0.8;
			maxWet = 1.0;
			minHighPass = 0;
			maxHighPass = 20000;
			minLowPass = 0;
			maxLowPass = 20000;

			offsetChoices = IntStream.range(0, 7)
					.mapToDouble(i -> Math.pow(2, -i))
					.toArray();
			offsetChoices[0] = 0.0;

//			repeatChoices = IntStream.range(0, 13)
//				.map(i -> i - 6)
//				.mapToDouble(i -> Math.pow(2, i))
//				.map(SimpleOrganGenome::factorForRepeat)
//				.toArray();

			repeatChoices = IntStream.range(0, 9)
					.map(i -> i - 2)
					.mapToDouble(i -> Math.pow(2, i))
					.map(SimpleOrganGenome::factorForRepeat)
					.toArray();
		}
	}
}
