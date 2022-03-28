/*
 * Copyright 2022 Michael Murray
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
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.health.AudioHealthComputation;
import com.almostrealism.audio.health.SilenceDurationHealthComputation;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.sound.DefaultDesirablesProvider;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBankHeap;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.filter.AdjustableDelayCell;
import org.almostrealism.breeding.Breeders;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.cl.CLComputeContext;
import org.almostrealism.hardware.cl.HardwareOperator;
import org.almostrealism.hardware.jni.NativeComputeContext;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.RandomChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;

public class CellularAudioOptimizer extends AudioPopulationOptimizer<Cells> {
	public static final int verbosity = 2;

	public static String LIBRARY = "Library";
	public static String STEMS = "Stems";

	static {
		String env = System.getenv("AR_RINGS_LIBRARY");
		if (env != null) LIBRARY = env;

		String arg = System.getProperty("AR_RINGS_LIBRARY");
		if (arg != null) LIBRARY = arg;
		
		env = System.getenv("AR_RINGS_STEMS");
		if (env != null) STEMS = env;

		arg = System.getProperty("AR_RINGS_STEMS");
		if (arg != null) STEMS = arg;
	}

	private LayeredOrganPopulation<Scalar, Scalar> population;

	public CellularAudioOptimizer(Supplier<GeneticTemporalFactory<Scalar, Scalar, Cells>> f,
								  Supplier<GenomeBreeder<Scalar>> breeder, Supplier<Supplier<Genome<Scalar>>> generator,
								  int sampleRate, int sources, int delayLayers, int totalCycles) {
		super(null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					if (population == null) {
						population = new LayeredOrganPopulation<>(children, sources, delayLayers, sampleRate);
						AudioHealthComputation hc = (AudioHealthComputation) getHealthComputation();
						population.init(f.get(), population.getGenomes().get(0), hc.getMeasures(), hc.getOutput());
					} else {
						population.setGenomes(children);
					}

					return population;
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
			RandomChromosomeFactory masterFilterDown = new RandomChromosomeFactory(); // MASTER FILTER DOWN

			generators.setChromosomeSize(sources, 0); // GENERATORS

			volume.setChromosomeSize(sources, 6);     // VOLUME
			Pair periodicVolumeDurationRange = new Pair(
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMin),
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMax));
			Pair overallVolumeDurationRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallVolumeDurationMin),
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallVolumeDurationMax));
			Pair overallVolumeExponentRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallVolumeExponentMin),
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallVolumeExponentMax));
			Pair overallVolumeInitialRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentInitial(config.minVolume),
					DefaultAudioGenome.factorForAdjustmentInitial(config.maxVolume));
			Pair overallVolumeOffsetRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMin),
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMax));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 0, periodicVolumeDurationRange));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 1, overallVolumeDurationRange));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 2, overallVolumeExponentRange));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 3, overallVolumeInitialRange));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 4, new Pair(-1.0, -1.0)));
			IntStream.range(0, sources).forEach(i -> volume.setRange(i, 5, overallVolumeOffsetRange));

			filterUp.setChromosomeSize(sources, 6); // MAIN FILTER UP
			Pair periodicFilterUpDurationRange = new Pair(
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicFilterUpDurationMin),
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicFilterUpDurationMax));
			Pair overallFilterUpDurationRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallFilterUpDurationMin),
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallFilterUpDurationMax));
			Pair overallFilterUpExponentRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallFilterUpExponentMin),
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallFilterUpExponentMax));
			Pair overallFilterUpOffsetRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallFilterUpOffsetMin),
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallFilterUpOffsetMax));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 0, periodicFilterUpDurationRange));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 1, overallFilterUpDurationRange));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 2, overallFilterUpExponentRange));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 3, new Pair(0.0, 0.0)));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 4, new Pair(1.0, 1.0)));
			IntStream.range(0, sources).forEach(i -> filterUp.setRange(i, 5, overallFilterUpOffsetRange));

			wetIn.setChromosomeSize(sources, 6);		 // WET IN
			Pair periodicWetInDurationRange = new Pair(
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicWetInDurationMin),
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicWetInDurationMax));
			Pair overallWetInDurationRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallWetInDurationMin),
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallWetInDurationMax));
			Pair overallWetInExponentRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallWetInExponentMin),
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallWetInExponentMax));
			Pair overallWetInOffsetRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallWetInOffsetMin),
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallWetInOffsetMax));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 0, periodicWetInDurationRange));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 1, overallWetInDurationRange));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 2, overallWetInExponentRange));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 3, new Pair(0.0, 0.0)));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 4, new Pair(1.0, 1.0)));
			IntStream.range(0, sources).forEach(i -> wetIn.setRange(i, 5, overallWetInOffsetRange));

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

			masterFilterDown.setChromosomeSize(sources, 6);     // VOLUME
			Pair periodicMasterFilterDownDurationRange = new Pair(
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicMasterFilterDownDurationMin),
					DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicMasterFilterDownDurationMax));
			Pair overallMasterFilterDownDurationRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallMasterFilterDownDurationMin),
					DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallMasterFilterDownDurationMax));
			Pair overallMasterFilterDownExponentRange = new Pair(
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallMasterFilterDownExponentMin),
					DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallMasterFilterDownExponentMax));
			Pair overallMasterFilterDownInitialRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentInitial(1.0),
					DefaultAudioGenome.factorForAdjustmentInitial(1.0));
			Pair overallMasterFilterDownOffsetRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallMasterFilterDownOffsetMin),
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallMasterFilterDownOffsetMax));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 0, periodicMasterFilterDownDurationRange));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 1, overallMasterFilterDownDurationRange));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 2, overallMasterFilterDownExponentRange));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 3, overallMasterFilterDownInitialRange));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 4, new Pair(-1.0, -1.0)));
			IntStream.range(0, sources).forEach(i -> masterFilterDown.setRange(i, 5, overallMasterFilterDownOffsetRange));

			return Genome.fromChromosomes(generators, volume, filterUp, wetIn, processors, transmission, wetOut, filters, masterFilterDown);
		};
	}

	public static CellularAudioOptimizer build(DesirablesProvider desirables, int cycles) {
		return build(desirables, 6, 3, cycles);
	}

	public static CellularAudioOptimizer build(DesirablesProvider desirables, int sources, int delayLayers, int cycles) {
		return build(generator(sources, delayLayers), desirables, sources, delayLayers, cycles);
	}

	public static CellularAudioOptimizer build(Supplier<Supplier<Genome<Scalar>>> generator, DesirablesProvider desirables,
											   int sources, int delayLayers, int cycles) {
		return new CellularAudioOptimizer(() -> new GeneticTemporalFactoryFromDesirables().from(desirables), () -> {
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
					Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // FILTERS
					Breeders.averageBreeder());  									   // MASTER FILTER DOWN
		}, generator, OutputLine.sampleRate, sources, delayLayers, cycles);
	}

	/**
	 * Build a {@link CellularAudioOptimizer} and initialize and run it.
	 *
	 * @see  CellularAudioOptimizer#build(DesirablesProvider, int)
	 * @see  CellularAudioOptimizer#init
	 * @see  CellularAudioOptimizer#run()
	 */
	public static void main(String args[]) throws IOException {
		CLComputeContext.enableFastQueue = true;
		StableDurationHealthComputation.enableTimeout = true;
		GeneticTemporalFactoryFromDesirables.enableMainFilterUp = true;
		GeneticTemporalFactoryFromDesirables.enableEfxFilters = true;
		GeneticTemporalFactoryFromDesirables.enableEfx = true;
		GeneticTemporalFactoryFromDesirables.enableWetInAdjustment = true;
		GeneticTemporalFactoryFromDesirables.enableMasterFilterDown = false;
		GeneticTemporalFactoryFromDesirables.disableClean = false;
		GeneticTemporalFactoryFromDesirables.enableSourcesOnly = false;
		SilenceDurationHealthComputation.enableSilenceCheck = false;
		AudioPopulationOptimizer.enableIsolatedContext = false;

		PopulationOptimizer.enableVerbose = verbosity > 0;
		Hardware.enableVerbose = verbosity > 0;
		WaveOutput.enableVerbose = verbosity > 1;
		PopulationOptimizer.enableDisplayGenomes = verbosity > 2;
		NativeComputeContext.enableVerbose = verbosity > 2;
		SilenceDurationHealthComputation.enableVerbose = verbosity > 2;
		HardwareOperator.enableVerboseLog = verbosity > 3;

		// PopulationOptimizer.THREADS = verbosity < 1 ? 2 : 1;
		PopulationOptimizer.enableBreeding = false; // verbosity < 3;

		AdjustableDelayCell.defaultPurgeFrequency = 1.0;
		// HealthCallable.setComputeRequirements(ComputeRequirement.C);
		// HealthCallable.setComputeRequirements(ComputeRequirement.PROFILING);
		// Hardware.getLocalHardware().setMaximumOperationDepth(7);

		WavFile.setHeap(() -> new ScalarBankHeap(600 * OutputLine.sampleRate), ScalarBankHeap::destroy);

		DefaultDesirablesProvider provider = new DefaultDesirablesProvider<>(116);

		File sources = new File("sources.json");
		if (sources.exists()) {
			provider.setWaves(Waves.load(sources));
		} else {
			provider.getWaves().addSplits(Arrays.asList(new File(STEMS).listFiles()), 116.0, Math.pow(10, -6), 1.0, 2.0, 4.0);
		}

		// GeneticTemporalFactoryFromDesirables.sourceOverride = new Waves();
		// GeneticTemporalFactoryFromDesirables.sourceOverride.addFiles(new File("Library/MD_SNARE_09.wav"));

		CellularAudioOptimizer opt = build(provider, PopulationOptimizer.enableBreeding ? 25 : 1);
		opt.init();
		opt.run();
	}

	public static class GeneratorConfiguration {
		public double repeatSpeedUpDurationMin, repeatSpeedUpDurationMax;

		public double minVolume, maxVolume;
		public double periodicVolumeDurationMin, periodicVolumeDurationMax;
		public double overallVolumeDurationMin, overallVolumeDurationMax;
		public double overallVolumeExponentMin, overallVolumeExponentMax;
		public double overallVolumeOffsetMin, overallVolumeOffsetMax;

		public double periodicFilterUpDurationMin, periodicFilterUpDurationMax;
		public double overallFilterUpDurationMin, overallFilterUpDurationMax;
		public double overallFilterUpExponentMin, overallFilterUpExponentMax;
		public double overallFilterUpOffsetMin, overallFilterUpOffsetMax;

		public double minTransmission, maxTransmission;
		public double minDelay, maxDelay;

		public double periodicSpeedUpDurationMin, periodicSpeedUpDurationMax;
		public double periodicSpeedUpPercentageMin, periodicSpeedUpPercentageMax;
		public double periodicSlowDownDurationMin, periodicSlowDownDurationMax;
		public double periodicSlowDownPercentageMin, periodicSlowDownPercentageMax;
		public double overallSpeedUpDurationMin, overallSpeedUpDurationMax;
		public double overallSpeedUpExponentMin, overallSpeedUpExponentMax;

		public double periodicWetInDurationMin, periodicWetInDurationMax;
		public double overallWetInDurationMin, overallWetInDurationMax;
		public double overallWetInExponentMin, overallWetInExponentMax;
		public double overallWetInOffsetMin, overallWetInOffsetMax;

		public double minWetOut, maxWetOut;
		public double minHighPass, maxHighPass;
		public double minLowPass, maxLowPass;

		public double periodicMasterFilterDownDurationMin, periodicMasterFilterDownDurationMax;
		public double overallMasterFilterDownDurationMin, overallMasterFilterDownDurationMax;
		public double overallMasterFilterDownExponentMin, overallMasterFilterDownExponentMax;
		public double overallMasterFilterDownOffsetMin, overallMasterFilterDownOffsetMax;

		public double offsetChoices[];
		public double repeatChoices[];

		public GeneratorConfiguration() { this(1); }

		public GeneratorConfiguration(int scale) {
			repeatSpeedUpDurationMin = 1;
			repeatSpeedUpDurationMax = 90;

			minVolume = 0.5 / scale;
			maxVolume = 1 / scale;
			periodicVolumeDurationMin = 0.5;
			periodicVolumeDurationMax = 180;
//			overallVolumeDurationMin = 60;
//			overallVolumeDurationMax = 240;
			overallVolumeDurationMin = 4;
			overallVolumeDurationMax = 10;
			overallVolumeExponentMin = 1;
			overallVolumeExponentMax = 1;
			overallVolumeOffsetMin = 18;
			overallVolumeOffsetMax = 22;

			periodicFilterUpDurationMin = 0.5;
			periodicFilterUpDurationMax = 180;
			overallFilterUpDurationMin = 30;
			overallFilterUpDurationMax = 180;
			overallFilterUpExponentMin = 0.5;
			overallFilterUpExponentMax = 3.5;
			overallFilterUpOffsetMin = 15;
			overallFilterUpOffsetMax = 20;

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

			periodicWetInDurationMin = 0.5;
			periodicWetInDurationMax = 180;
//			overallWetInDurationMin = 30;
//			overallWetInDurationMax = 120;
			overallWetInDurationMin = 6;
			overallWetInDurationMax = 20;
			overallWetInExponentMin = 0.5;
			overallWetInExponentMax = 2.5;
			overallWetInOffsetMin = 15;
			overallWetInOffsetMax = 25;

			minWetOut = 0.8;
			maxWetOut = 1.0;
			minHighPass = 0;
			maxHighPass = 20000;
			minLowPass = 0;
			maxLowPass = 20000;

			periodicMasterFilterDownDurationMin = 0.5;
			periodicMasterFilterDownDurationMax = 90;
			overallMasterFilterDownDurationMin = 5;
			overallMasterFilterDownDurationMax = 90;
			overallMasterFilterDownExponentMin = 0.5;
			overallMasterFilterDownExponentMax = 3.5;
			overallMasterFilterDownOffsetMin = 15;
			overallMasterFilterDownOffsetMax = 15;

			offsetChoices = IntStream.range(0, 7)
					.mapToDouble(i -> Math.pow(2, -i))
					.toArray();
			offsetChoices[0] = 0.0;

			repeatChoices = IntStream.range(0, 9)
					.map(i -> i - 2)
					.mapToDouble(i -> Math.pow(2, i))
					.map(DefaultAudioGenome::factorForRepeat)
					.toArray();
		}
	}
}
