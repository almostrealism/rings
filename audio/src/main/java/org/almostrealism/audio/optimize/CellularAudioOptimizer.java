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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.WaveSet;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.grains.GrainGenerationSettings;
import org.almostrealism.audio.grains.GranularSynthesizer;
import org.almostrealism.audio.health.AudioHealthComputation;
import org.almostrealism.audio.health.SilenceDurationHealthComputation;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.algebra.Pair;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.PatternFactoryChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.sequence.GridSequencer;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.PackedCollectionHeap;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.cl.CLComputeContext;
import org.almostrealism.hardware.cl.HardwareOperator;
import org.almostrealism.hardware.jni.NativeComputeContext;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.GenomeFromChromosomes;
import org.almostrealism.heredity.RandomChromosomeFactory;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.optimize.PopulationOptimizer;

public class CellularAudioOptimizer extends AudioPopulationOptimizer<Cells> {
	public static final int verbosity = 0;
	public static final boolean enableSourcesJson = true;
	public static final boolean enableStems = false;

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

	private AudioScenePopulation<PackedCollection<?>> population;

	public CellularAudioOptimizer(AudioScene<?> scene,
								  Supplier<GenomeBreeder<PackedCollection<?>>> breeder, Supplier<Supplier<Genome<PackedCollection<?>>>> generator,
								  int totalCycles) {
		super(null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					if (population == null) {
						population = new AudioScenePopulation<>(scene, children);
						AudioHealthComputation hc = (AudioHealthComputation) getHealthComputation();
						population.init(population.getGenomes().get(0), hc.getMeasures(), hc.getOutput());
					} else {
						population.setGenomes(children);
					}

					return population;
				});
	}

	public static CellularAudioOptimizer build(AudioScene<?> scene, int cycles) {
		return build(AudioSceneGenome.generator(scene), scene, cycles);
	}

	public static CellularAudioOptimizer build(Supplier<Supplier<Genome<PackedCollection<?>>>> generator, AudioScene<?> scene, int cycles) {
		return new CellularAudioOptimizer(scene, scene::getBreeder, generator, cycles);
	}

	/**
	 * Build a {@link CellularAudioOptimizer} and initialize and run it.
	 *
	 * @see  CellularAudioOptimizer#build(AudioScene, int)
	 * @see  CellularAudioOptimizer#init
	 * @see  CellularAudioOptimizer#run()
	 */
	public static void main(String args[]) throws IOException {
		CLComputeContext.enableFastQueue = false;
		StableDurationHealthComputation.enableTimeout = true;
		AudioScene.enableMainFilterUp = true;
		AudioScene.enableEfxFilters = true;
		AudioScene.enableEfx = true;
		AudioScene.enableWetInAdjustment = true;
		AudioScene.enableMasterFilterDown = true;
		AudioScene.disableClean = false;
		AudioScene.enableSourcesOnly = false;
		SilenceDurationHealthComputation.enableSilenceCheck = false;
		AudioPopulationOptimizer.enableIsolatedContext = false;

		PopulationOptimizer.enableVerbose = verbosity > 0;
		Hardware.enableVerbose = verbosity > 0;
		WaveOutput.enableVerbose = verbosity > 1;
		PopulationOptimizer.enableDisplayGenomes = verbosity > 2;
		NativeComputeContext.enableVerbose = verbosity > 2;
		SilenceDurationHealthComputation.enableVerbose = verbosity > 2;
		HardwareOperator.enableLog = verbosity > 2;
		HardwareOperator.enableVerboseLog = verbosity > 3;

		// PopulationOptimizer.THREADS = verbosity < 1 ? 2 : 1;
		PopulationOptimizer.enableBreeding = verbosity < 3;

		AdjustableDelayCell.defaultPurgeFrequency = 1.0;
		// HealthCallable.setComputeRequirements(ComputeRequirement.C);
		// HealthCallable.setComputeRequirements(ComputeRequirement.PROFILING);
		// Hardware.getLocalHardware().setMaximumOperationDepth(7);

		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(20000 * OutputLine.sampleRate), PackedCollectionHeap::destroy);

		AudioScene<?> scene = createScene();
		CellularAudioOptimizer opt = build(scene, PopulationOptimizer.enableBreeding ? 25 : 1);
		opt.init();
		opt.run();
	}

	public static AudioScene<?> createScene() throws IOException {
		double bpm = 120.0; // 116.0;
		int sourceCount = 5;
		AudioScene<?> scene = new AudioScene<>(null, bpm, sourceCount, 3, OutputLine.sampleRate);

		Set<Integer> choices = IntStream.range(0, sourceCount).mapToObj(i -> i).collect(Collectors.toSet());

		GranularSynthesizer synth = new GranularSynthesizer();
		synth.setGain(3.0);
		synth.addFile("Library/organ.wav");
		synth.addGrain(new GrainGenerationSettings());
		synth.addGrain(new GrainGenerationSettings());

		WaveSet synthNotes = new WaveSet(synth);
		synthNotes.setRoot(WesternChromatic.C3);
		synthNotes.setNotes(WesternScales.major(WesternChromatic.C3, 1));

		GridSequencer sequencer = new GridSequencer();
		sequencer.setStepCount(8);
		sequencer.initParamSequence();
		// sequencer.getSamples().add(synthNotes);
		sequencer.getSamples().add(new WaveSet(new FileWaveDataProvider("Library/MD_SNARE_09.wav")));
		sequencer.getSamples().add(new WaveSet(new FileWaveDataProvider("Library/MD_SNARE_11.wav")));
		sequencer.getSamples().add(new WaveSet(new FileWaveDataProvider("Library/Snare Perc DD.wav")));

		Waves seqWaves = new Waves("Sequencer", new WaveSet(sequencer));
		seqWaves.getChoices().setChoices(choices);

		Waves group = new Waves("Group");
		group.getChoices().setChoices(choices);
		group.getChildren().add(seqWaves);


		File sources = new File("sources.json");
		Waves waves = scene.getWaves();

		if (enableSourcesJson && sources.exists()) {
			waves = Waves.load(sources);
			scene.setWaves(waves);
		} else if (enableStems) {
			waves.addSplits(Arrays.asList(new File(STEMS).listFiles()), bpm, Math.pow(10, -6), choices, 1.0, 2.0, 4.0);
		} else {
			waves.getChildren().add(group);
			waves.getChoices().setChoices(choices);
		}

		scene.getPatternManager().getChoices().addAll(createChoices());
		scene.setTuning(new DefaultKeyboardTuning());
		scene.setTotalMeasures(64);
		scene.addSection(0, 32);
		scene.addSection(32, 32);
		scene.addBreak(64);

		double duration1 = 0.5;
		double duration2 = 1.0;

		int channel = 0;
		PatternLayerManager layer = scene.getPatternManager().addPattern(channel, 0.25, false);
		layer.setSeedBias(0.8);
		layer.addLayer(new ParameterSet());

//		layer = scene.getPatternManager().addPattern(channel, duration1, false);
//		layer.setSeedBias(0.8);
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel, duration1, false);
//		layer.setSeedBias(0.8);
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel, duration1, false);
//		layer.setSeedBias(0.8);
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel, duration1, false);
//		layer.setSeedBias(0.8);
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel++, duration1, false);
//		layer.setSeedBias(0.8);
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel, duration2, false);
//		layer.setSeedBias(0.25);
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel++, duration2, false);
//		layer.setSeedBias(0.25);
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel++, 2.0, false);
//		layer.setSeedBias(0.2);
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel++, 8.0, true);
//		layer.setSeedBias(0.2);
//		layer.setChordDepth(3);
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//
//		layer = scene.getPatternManager().addPattern(channel++, 4.0, true);
//		layer.setSeedBias(0.0);
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//		layer.addLayer(new ParameterSet());
//
//		scene.getEfxManager().setWetChannels(List.of(4));

//		scene.getEfxManager().setWetChannels(List.of(0));

		// scene.saveSettings(new File("scene-settings.json"));
		return scene;
	}

	private static List<PatternFactoryChoice> createChoices() throws IOException {
		if (enableSourcesJson) {
			PatternFactoryChoiceList choices = new ObjectMapper()
					.readValue(new File("pattern-factory.json"), PatternFactoryChoiceList.class);
			return choices;
		} else {
			List<PatternFactoryChoice> choices = new ArrayList<>();

			PatternFactoryChoice kick = new PatternFactoryChoice(new PatternElementFactory("Kicks", new PatternNote("Kit/Kick.wav")));
			kick.setSeed(true);
			kick.setMinScale(0.25);
			choices.add(kick);

			PatternFactoryChoice clap = new PatternFactoryChoice(new PatternElementFactory("Clap/Snare", new PatternNote("Kit/Clap.wav")));
			clap.setMaxScale(0.5);
			choices.add(clap);

			PatternFactoryChoice toms = new PatternFactoryChoice(
					new PatternElementFactory("Toms", new PatternNote("Kit/Tom1.wav"),
							new PatternNote("Kit/Tom2.wav")));
			toms.setMaxScale(0.25);
			choices.add(toms);

			PatternFactoryChoice hats = new PatternFactoryChoice(new PatternElementFactory("Hats"));
			hats.setMaxScale(0.25);
			choices.add(hats);

			return choices;
		}
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
