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
import org.almostrealism.hardware.AcceleratedComputationOperation;
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
	public static final int verbosity = 1;

	public LayeredOrganOptimizer(Supplier<AdjustmentLayerOrganSystemFactory<Scalar, Scalar, Double, Scalar>> f,
								 Supplier<GenomeBreeder<Scalar>> breeder, Supplier<Supplier<Genome<Scalar>>> generator,
								 int sampleRate, int sources, int delayLayers, int totalCycles) {
		super(null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					LayeredOrganPopulation<Scalar, Scalar, Double, Scalar> pop = new LayeredOrganPopulation(children, sources, delayLayers, sampleRate);
					AudioHealthComputation hc = (AudioHealthComputation) getHealthComputation();
					pop.init(f.get(), pop.getGenomes().get(0), hc.getMeasures(), hc.getOutput());
					return pop;
				});
	}

	public static Supplier<Supplier<Genome<Scalar>>> generator(int sources, int delayLayers) {
		return generator(sources, delayLayers, new GeneratorConfiguration(sources));
	}

	public static Supplier<Supplier<Genome<Scalar>>> generator(int sources, int delayLayers, GeneratorConfiguration config) {
		return () -> {
			// Random genetic material generators
			ChromosomeFactory<Scalar> generators = DefaultAudioGenome.generatorFactory(config.offsetChoices, config.repeatChoices,
													config.repeatSpeedUpDurationMin, config.repeatSpeedUpDurationMax);   // GENERATORS
			RandomChromosomeFactory volume = new RandomChromosomeFactory();       // VOLUME
			RandomChromosomeFactory filterUp = new RandomChromosomeFactory();     // MAIN FILTER UP
			RandomChromosomeFactory wetIn = new RandomChromosomeFactory();		  // WET IN
			RandomChromosomeFactory processors = new RandomChromosomeFactory();   // DELAY
			RandomChromosomeFactory transmission = new RandomChromosomeFactory(); // ROUTING
			RandomChromosomeFactory wetOut = new RandomChromosomeFactory();		  // WET OUT
			RandomChromosomeFactory filters = new RandomChromosomeFactory();      // FILTERS

			generators.setChromosomeSize(sources, 0); // GENERATORS

			volume.setChromosomeSize(sources, 2);     // VOLUME
			Pair volumeRange = new Pair(config.minVolume, config.maxVolume);
			IntStream.range(0, sources).forEach(i -> {
				volume.setRange(i, 0, volumeRange);
				volume.setRange(i, 1, volumeRange);
			});

			filterUp.setChromosomeSize(sources, 3); // MAIN FILTER UP
			Pair periodicFilterUpDurationRange = new Pair(
					DefaultAudioGenome.factorForPeriodicFilterUpDuration(config.periodicFilterUpDurationMin),
					DefaultAudioGenome.factorForPeriodicFilterUpDuration(config.periodicFilterUpDurationMax));
			Pair overallFilterUpDurationRange = new Pair(
					DefaultAudioGenome.factorForPolyFilterUpDuration(config.overallFilterUpDurationMin),
					DefaultAudioGenome.factorForPolyFilterUpDuration(config.overallFilterUpDurationMax));
			Pair overallFilterUpExponentRange = new Pair(
					DefaultAudioGenome.factorForPolyFilterUpExponent(config.overallFilterUpExponentMin),
					DefaultAudioGenome.factorForPolyFilterUpExponent(config.overallFilterUpExponentMax));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 0, periodicFilterUpDurationRange));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 1, overallFilterUpDurationRange));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 2, overallFilterUpExponentRange));

			wetIn.setChromosomeSize(sources, delayLayers);		 // WET IN
			Pair wetInRange = new Pair(config.minWetIn, config.maxWetIn);
			IntStream.range(0, sources).forEach(i ->
					IntStream.range(0, delayLayers).forEach(j -> wetIn.setRange(i, j, wetInRange)));

			processors.setChromosomeSize(delayLayers, 7); // DELAY
			Pair delayRange = new Pair(DefaultAudioGenome.factorForDelay(config.minDelay),
									DefaultAudioGenome.factorForDelay(config.maxDelay));
			Pair periodicSpeedUpDurationRange = new Pair(
					DefaultAudioGenome.factorForSpeedUpDuration(config.periodicSpeedUpDurationMin),
					DefaultAudioGenome.factorForSpeedUpDuration(config.periodicSpeedUpDurationMax));
			Pair periodicSpeedUpPercentageRange = new Pair(
					DefaultAudioGenome.factorForSpeedUpPercentage(config.periodicSpeedUpPercentageMin),
					DefaultAudioGenome.factorForSpeedUpPercentage(config.periodicSpeedUpPercentageMax));
			Pair periodicSlowDownDurationRange = new Pair(
					DefaultAudioGenome.factorForSlowDownDuration(config.periodicSlowDownDurationMin),
					DefaultAudioGenome.factorForSlowDownDuration(config.periodicSlowDownDurationMax));
			Pair periodicSlowDownPercentageRange = new Pair(
					DefaultAudioGenome.factorForSlowDownPercentage(config.periodicSlowDownPercentageMin),
					DefaultAudioGenome.factorForSlowDownPercentage(config.periodicSlowDownPercentageMax));
			Pair overallSpeedUpDurationRange = new Pair(
					DefaultAudioGenome.factorForPolySpeedUpDuration(config.overallSpeedUpDurationMin),
					DefaultAudioGenome.factorForPolySpeedUpDuration(config.overallSpeedUpDurationMax));
			Pair overallSpeedUpExponentRange = new Pair(
					DefaultAudioGenome.factorForPolySpeedUpExponent(config.overallSpeedUpExponentMin),
					DefaultAudioGenome.factorForPolySpeedUpExponent(config.overallSpeedUpExponentMax));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 0, delayRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 1, periodicSpeedUpDurationRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 2, periodicSpeedUpPercentageRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 3, periodicSlowDownDurationRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 4, periodicSlowDownPercentageRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 5, overallSpeedUpDurationRange));
			IntStream.range(0, delayLayers).forEach(i -> processors.setRange(i, 6, overallSpeedUpExponentRange));

			transmission.setChromosomeSize(delayLayers, delayLayers);    // ROUTING
			Pair transmissionRange = new Pair(config.minTransmission, config.maxTransmission);
			IntStream.range(0, delayLayers).forEach(i -> IntStream.range(0, delayLayers)
					.forEach(j -> transmission.setRange(i, j, transmissionRange)));

			wetOut.setChromosomeSize(1, delayLayers);		 // WET OUT
			Pair wetOutRange = new Pair(config.minWetOut, config.maxWetOut);
			IntStream.range(0, delayLayers).forEach(i -> wetOut.setRange(0, i, wetOutRange));

			filters.setChromosomeSize(sources, 2);    // FILTERS
			Pair hpRange = new Pair(DefaultAudioGenome.factorForFilterFrequency(config.minHighPass),
					DefaultAudioGenome.factorForFilterFrequency(config.maxHighPass));
			Pair lpRange = new Pair(DefaultAudioGenome.factorForFilterFrequency(config.minLowPass),
					DefaultAudioGenome.factorForFilterFrequency(config.maxLowPass));
			IntStream.range(0, sources).forEach(i -> {
				filters.setRange(i, 0, hpRange);
				filters.setRange(i, 1, lpRange);
			});

			return Genome.fromChromosomes(generators, volume, filterUp, wetIn, processors, transmission, wetOut, filters);
		};
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int cycles) {
		return build(desirables, 8, 4, cycles);
	}

	public static LayeredOrganOptimizer build(DesirablesProvider desirables, int sources, int delayLayers, int cycles) {
		return build(generator(sources, delayLayers), desirables, sources, delayLayers, cycles);
	}

	public static LayeredOrganOptimizer build(Supplier<Supplier<Genome<Scalar>>> generator, DesirablesProvider desirables,
											  int sources, int delayLayers, int cycles) {
		return new LayeredOrganOptimizer(() -> {
			TieredCellAdjustmentFactory<Scalar, Scalar> tca = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.PERIODIC));
			TieredCellAdjustmentFactory<Scalar, Scalar> tcb = new TieredCellAdjustmentFactory<>(new DefaultCellAdjustmentFactory(Type.EXPONENTIAL), tca);
			return new AdjustmentLayerOrganSystemFactory(tca, new GeneticTemporalFactoryFromDesirables().from(desirables));
		}, () -> {
			return new DefaultGenomeBreeder(
					Breeders.of(Breeders.randomChoiceBreeder(),
							Breeders.randomChoiceBreeder(),
							Breeders.randomChoiceBreeder(),
							Breeders.averageBreeder()), 							   // GENERATORS
					Breeders.averageBreeder(),  									   // VOLUME
					Breeders.averageBreeder(),  									   // MAIN FILTER UP
					Breeders.averageBreeder(),  									   // WET IN
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
					Breeders.averageBreeder(),  									   // WET OUT
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new)); // FILTERS
		}, generator, OutputLine.sampleRate, sources, delayLayers, cycles);
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
		Hardware.enableVerbose = verbosity > 0;
		WaveOutput.enableVerbose = verbosity > 1;
		PopulationOptimizer.enableDisplayGenomes = verbosity > 1;
		NativeComputeContext.enableVerbose = verbosity > 2;
		HardwareOperator.enableVerboseLog = verbosity > 2;
		SilenceDurationHealthComputation.enableVerbose = verbosity > 2;

		WavFile.setHeap(() -> new ScalarBankHeap(600 * OutputLine.sampleRate), ScalarBankHeap::destroy);

		DefaultDesirablesProvider provider = new DefaultDesirablesProvider<>(116);

		Stream.of(new File("Library").listFiles()).map(f -> {
			try {
				if (".DS_Store".equals(f.getName())) return null;
				return WavFile.openWavFile(f).getSampleRate() == OutputLine.sampleRate ? f : null;
			} catch (Exception e) {
				System.out.println("Error loading " + f.getName());
				e.printStackTrace();
				return null;
			}
		}).filter(Objects::nonNull).forEach(provider.getSamples()::add);

		LayeredOrganOptimizer opt = build(provider, PopulationOptimizer.enableBreeding ? 25 : 1);
		opt.init();
		opt.run();
	}

	public static class GeneratorConfiguration {
		public double repeatSpeedUpDurationMin, repeatSpeedUpDurationMax;

		public double minVolume, maxVolume;
		public double periodicFilterUpDurationMin, periodicFilterUpDurationMax;
		public double overallFilterUpDurationMin, overallFilterUpDurationMax;
		public double overallFilterUpExponentMin, overallFilterUpExponentMax;

		public double minTransmission, maxTransmission;
		public double minDelay, maxDelay;

		public double periodicSpeedUpDurationMin, periodicSpeedUpDurationMax;
		public double periodicSpeedUpPercentageMin, periodicSpeedUpPercentageMax;
		public double periodicSlowDownDurationMin, periodicSlowDownDurationMax;
		public double periodicSlowDownPercentageMin, periodicSlowDownPercentageMax;
		public double overallSpeedUpDurationMin, overallSpeedUpDurationMax;
		public double overallSpeedUpExponentMin, overallSpeedUpExponentMax;

		public double minWetIn, maxWetIn;
		public double minWetOut, maxWetOut;
		public double minHighPass, maxHighPass;
		public double minLowPass, maxLowPass;

		public double offsetChoices[];
		public double repeatChoices[];

		public GeneratorConfiguration() { this(1); }

		public GeneratorConfiguration(int scale) {
			repeatSpeedUpDurationMin = 1;
			repeatSpeedUpDurationMax = 90;

			minVolume = 0.0;
			maxVolume = 1.0 / scale;

			periodicFilterUpDurationMin = 0.5;
			periodicFilterUpDurationMax = 180;
			overallFilterUpDurationMin = 10;
			overallFilterUpDurationMax = 180;
			overallFilterUpExponentMin = 0.5;
			overallFilterUpExponentMax = 3.5;

			minTransmission = 0.0;
			maxTransmission = 0.5;
			minDelay = 0.5;
			maxDelay = 60;

			periodicSpeedUpDurationMin = 0.5;
			periodicSpeedUpDurationMax = 180;
			periodicSpeedUpPercentageMin = 0.0;
			periodicSpeedUpPercentageMax = 10;

			periodicSlowDownDurationMin = 1;
			periodicSlowDownDurationMax = 180;
			periodicSlowDownPercentageMin = 0.0;
			periodicSlowDownPercentageMax = 0.9;

			overallSpeedUpDurationMin = 10;
			overallSpeedUpDurationMax = 180;
			overallSpeedUpExponentMin = 1;
			overallSpeedUpExponentMax = 1;

			minWetIn = 0.5;
			maxWetIn = 1.0;
			minWetOut = 0.8;
			maxWetOut = 1.0;
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
					.map(DefaultAudioGenome::factorForRepeat)
					.toArray();
		}
	}
}
