/*
 * Copyright 2023 Michael Murray
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
import org.almostrealism.audio.generative.NoOpGenerationProvider;
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
import org.almostrealism.audio.pattern.PatternSystemManager;
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
	public static final boolean enableSingleChannel = false;
	public static final boolean enableFullMix = true;

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
					if (children.isEmpty()) throw new IllegalArgumentException();

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
		PopulationOptimizer.enableBreeding = false; // verbosity < 3;

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
		double bpm = 120.0;
		int sourceCount = 5;
		AudioScene<?> scene = new AudioScene<>(null, bpm, sourceCount, 3, OutputLine.sampleRate, new NoOpGenerationProvider());

//		Set<Integer> choices = IntStream.range(0, sourceCount).mapToObj(i -> i).collect(Collectors.toSet());
//		Waves waves = waves(scene, choices, bpm);

		scene.getPatternManager().getChoices().addAll(createChoices());
		scene.setTuning(new DefaultKeyboardTuning());
		scene.setTotalMeasures(64);
		scene.addSection(0, 32);
		scene.addSection(32, 32);
		scene.addBreak(64);

		int channel = 0;
		double duration1 = 0.5;
		double duration2 = 1.0;

		if (enableSingleChannel) {
			PatternLayerManager layer = scene.getPatternManager().addPattern(channel, 2.0, true);
			layer.setSeedBias(-0.1);
			layer.addLayer(new ParameterSet());

			scene.getEfxManager().setWetChannels(List.of(0));

			PatternSystemManager.enableWarnings = false;
			PatternLayerManager.enableWarnings = false;
		} else if (!enableFullMix) {
			PatternLayerManager layer = scene.getPatternManager().addPattern(channel, 0.25, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			// layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			// layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			// Drums
			layer = scene.getPatternManager().addPattern(channel++, duration2, false);
			// layer.setSeedBias(0.25);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration2, false);
			// layer.setSeedBias(0.25);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			// Chords
			layer = scene.getPatternManager().addPattern(channel++, 4.0, true);
			layer.setChordDepth(3);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			// Melody
			layer = scene.getPatternManager().addPattern(channel++, 4.0, true);
			layer.addLayer(new ParameterSet());

			scene.getEfxManager().setWetChannels(List.of(channel - 2, channel - 1));

			PatternSystemManager.enableWarnings = false;
			PatternLayerManager.enableWarnings = false;
		} else {
			AudioScene.Settings settings = AudioScene.Settings.defaultSettings(sourceCount, AudioScene.DEFAULT_PATTERNS_PER_CHANNEL);
//			settings.getPatternSystem().setPatterns(
//					settings
//							.getPatternSystem()
//							.getPatterns()
//							.stream()
//							.filter(p -> p.getChannel() < 4)
//							.collect(Collectors.toList()));

			scene.setSettings(settings);
			/*
			PatternLayerManager layer = scene.getPatternManager().addPattern(channel, 0.25, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration1, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel++, duration1, false);
			layer.setSeedBias(0.8);
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel, duration2, false);
			layer.setSeedBias(0.25);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel++, duration2, false);
			layer.setSeedBias(0.25);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel++, 2.0, false);
			layer.setSeedBias(0.2);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel++, 8.0, true);
			layer.setSeedBias(0.2);
			layer.setChordDepth(3);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			layer = scene.getPatternManager().addPattern(channel++, 4.0, true);
			layer.setSeedBias(0.0);
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());
			layer.addLayer(new ParameterSet());

			 */

			scene.getEfxManager().setWetChannels(List.of(3, 4));
		}

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

	protected static Waves waves(AudioScene scene, Set<Integer> choices, double bpm) throws IOException {
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

		return waves;
	}
}
