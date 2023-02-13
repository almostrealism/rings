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

package org.almostrealism.audio.optimize;

import org.almostrealism.algebra.Pair;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeFromChromosomes;
import org.almostrealism.heredity.RandomChromosomeFactory;

import java.util.Optional;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/*
 * TODO  This class is just a temporary solution to the problem that
 * TODO  not everything has been migrated to ConfigurableGenome.
 */
@Deprecated
public class AudioSceneGenome implements Genome<PackedCollection<?>> {
	private Genome<PackedCollection<?>> genome;
	private Genome<PackedCollection<?>> legacyGenome;

	public AudioSceneGenome() { }

	public AudioSceneGenome(Genome<PackedCollection<?>> genome, Genome<PackedCollection<?>> legacyGenome) {
		this.genome = genome;
		this.legacyGenome = legacyGenome;
	}

	public Genome<PackedCollection<?>> getGenome() {
		return genome;
	}

	public void setGenome(Genome<PackedCollection<?>> genome) {
		this.genome = genome;
	}

	public Genome<PackedCollection<?>> getLegacyGenome() {
		return legacyGenome;
	}

	public void setLegacyGenome(Genome<PackedCollection<?>> legacyGenome) {
		this.legacyGenome = legacyGenome;
	}

	@Override
	public Genome getHeadSubset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Chromosome getLastChromosome() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int count() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Chromosome<PackedCollection<?>> valueAt(int pos) {
		throw new UnsupportedOperationException();
	}

	public String signature() {
		return Optional.ofNullable(genome).map(Genome::signature).orElse("") + ":" +
				Optional.ofNullable(legacyGenome).map(Genome::signature).orElse("");
	}

	public static Supplier<Supplier<Genome<PackedCollection<?>>>> generator(AudioScene<?> scene) {
		return generator(scene, new GeneratorConfiguration(scene.getSourceCount()));
	}

	public static Supplier<Supplier<Genome<PackedCollection<?>>>> generator(AudioScene<?> scene, GeneratorConfiguration config) {
		int sources = scene.getSourceCount();
		int delayLayers = scene.getDelayLayerCount();

		Supplier<GenomeFromChromosomes> oldGenome = () -> {
			// Random genetic material generators
			ChromosomeFactory<PackedCollection<?>> generators = DefaultAudioGenome.generatorFactory(config.minChoice, config.maxChoice,
					config.offsetChoices, config.repeatChoices,
					config.repeatSpeedUpDurationMin, config.repeatSpeedUpDurationMax);   // GENERATORS
			RandomChromosomeFactory parameters = new RandomChromosomeFactory();   // PARAMETERS
			RandomChromosomeFactory volume = new RandomChromosomeFactory();       // VOLUME
			RandomChromosomeFactory filterUp = new RandomChromosomeFactory();     // MAIN FILTER UP
			RandomChromosomeFactory wetIn = new RandomChromosomeFactory();		  // WET IN
			RandomChromosomeFactory processors = new RandomChromosomeFactory();   // DELAY
			RandomChromosomeFactory transmission = new RandomChromosomeFactory(); // ROUTING
			RandomChromosomeFactory wetOut = new RandomChromosomeFactory();		  // WET OUT
			RandomChromosomeFactory filters = new RandomChromosomeFactory();      // FILTERS
			RandomChromosomeFactory masterFilterDown = new RandomChromosomeFactory(); // MASTER FILTER DOWN

			generators.setChromosomeSize(sources, 0); // GENERATORS

			parameters.setChromosomeSize(sources, 3);
			IntStream.range(0, sources).forEach(i -> {
				parameters.setRange(i, 0, new Pair(config.minX.applyAsDouble(i), config.maxX.applyAsDouble(i)));
				parameters.setRange(i, 1, new Pair(config.minY.applyAsDouble(i), config.maxY.applyAsDouble(i)));
				parameters.setRange(i, 2, new Pair(config.minZ.applyAsDouble(i), config.maxZ.applyAsDouble(i)));
			});

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
			Pair overallVolumeOffsetRange = new Pair(
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMin),
					DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMax));

			IntStream.range(0, sources).forEach(i -> {
				volume.setRange(i, 0, periodicVolumeDurationRange);
				volume.setRange(i, 1, overallVolumeDurationRange);
				volume.setRange(i, 2, overallVolumeExponentRange);
				volume.setRange(i, 3, new Pair(
						DefaultAudioGenome.factorForAdjustmentInitial(config.minVolume.applyAsDouble(i)),
						DefaultAudioGenome.factorForAdjustmentInitial(config.maxVolume.applyAsDouble(i))));
				volume.setRange(i, 4, new Pair(-1.0, -1.0));
				volume.setRange(i, 5, overallVolumeOffsetRange);
			});

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

			return Genome.fromChromosomes(generators, parameters, volume, filterUp, wetIn, processors, transmission, wetOut, filters, masterFilterDown);
		};

		return () -> {
			GenomeFromChromosomes old = oldGenome.get();
			return () -> new AudioSceneGenome(scene.getGenome().random(), old.get());
		};
	}

	public static class GeneratorConfiguration {
		public IntToDoubleFunction minChoice, maxChoice;
		public double minChoiceValue, maxChoiceValue;

		public double repeatSpeedUpDurationMin, repeatSpeedUpDurationMax;

		public IntToDoubleFunction minX, maxX;
		public IntToDoubleFunction minY, maxY;
		public IntToDoubleFunction minZ, maxZ;
		public double minXValue, maxXValue;
		public double minYValue, maxYValue;
		public double minZValue, maxZValue;

		public IntToDoubleFunction minVolume, maxVolume;
		public double minVolumeValue, maxVolumeValue;
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
			double offset = 80;
			double duration = 0;

			minChoiceValue = 0.0;
			maxChoiceValue = 1.0;
			repeatSpeedUpDurationMin = 5.0;
			repeatSpeedUpDurationMax = 60.0;

			minVolumeValue = 0.4 / scale;
			maxVolumeValue = 0.8 / scale;
			periodicVolumeDurationMin = 0.5;
			periodicVolumeDurationMax = 180;
//			overallVolumeDurationMin = 60;
//			overallVolumeDurationMax = 240;
			overallVolumeDurationMin = duration + 5.0;
			overallVolumeDurationMax = duration + 30.0;
			overallVolumeExponentMin = 1;
			overallVolumeExponentMax = 1;
			overallVolumeOffsetMin = offset + 10.0;
			overallVolumeOffsetMax = offset + 35.0;

			periodicFilterUpDurationMin = 0.5;
			periodicFilterUpDurationMax = 180;
			overallFilterUpDurationMin = duration + 90.0;
			overallFilterUpDurationMax = duration + 360.0;
			overallFilterUpExponentMin = 0.5;
			overallFilterUpExponentMax = 3.5;
			overallFilterUpOffsetMin = offset;
			overallFilterUpOffsetMax = offset + 35.0;

			minTransmission = 0.3;
			maxTransmission = 1.6;
			minDelay = 4.0;
			maxDelay = 20.0;

			periodicSpeedUpDurationMin = 20.0;
			periodicSpeedUpDurationMax = 180.0;
			periodicSpeedUpPercentageMin = 0.0;
			periodicSpeedUpPercentageMax = 2.0;

			periodicSlowDownDurationMin = 20.0;
			periodicSlowDownDurationMax = 180.0;
			periodicSlowDownPercentageMin = 0.0;
			periodicSlowDownPercentageMax = 0.9;

			overallSpeedUpDurationMin = 10.0;
			overallSpeedUpDurationMax = 60.0;
			overallSpeedUpExponentMin = 1;
			overallSpeedUpExponentMax = 1;

			periodicWetInDurationMin = 0.5;
			periodicWetInDurationMax = 180;
//			overallWetInDurationMin = 30;
//			overallWetInDurationMax = 120;
			overallWetInDurationMin = duration + 5.0;
			overallWetInDurationMax = duration + 60.0;
			overallWetInExponentMin = 0.5;
			overallWetInExponentMax = 2.5;
			overallWetInOffsetMin = offset;
			overallWetInOffsetMax = offset + 30;

			minWetOut = 0.5;
			maxWetOut = 1.8;
			minHighPass = 0.0;
			maxHighPass = 5000.0;
			minLowPass = 15000.0;
			maxLowPass = 20000.0;

			periodicMasterFilterDownDurationMin = 0.5;
			periodicMasterFilterDownDurationMax = 90;
			overallMasterFilterDownDurationMin = duration + 30;
			overallMasterFilterDownDurationMax = duration + 120;
			overallMasterFilterDownExponentMin = 0.5;
			overallMasterFilterDownExponentMax = 3.5;
			overallMasterFilterDownOffsetMin = offset;
			overallMasterFilterDownOffsetMax = offset + 40;

			offsetChoices = IntStream.range(0, 7)
					.mapToDouble(i -> Math.pow(2, -i))
					.toArray();
			offsetChoices[0] = 0.0;

			repeatChoices = IntStream.range(0, 9)
					.map(i -> i - 2)
					.mapToDouble(i -> Math.pow(2, i))
//					.map(DefaultAudioGenome::factorForRepeat)
					.toArray();

			repeatChoices = new double[] { 16 };


			minChoice = i -> minChoiceValue;
			maxChoice = i -> maxChoiceValue;
			minX = i -> minXValue;
			maxX = i -> maxXValue;
			minY = i -> minYValue;
			maxY = i -> maxYValue;
			minZ = i -> minZValue;
			maxZ = i -> maxZValue;
			minVolume = i -> minVolumeValue;
			maxVolume = i -> maxVolumeValue;
		}
	}
}
