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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.generative.NoOpGenerationProvider;
import org.almostrealism.audio.health.AudioHealthComputation;
import org.almostrealism.audio.health.SilenceDurationHealthComputation;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.PatternFactoryChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.pattern.PatternSystemManager;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.cl.CLComputeContext;
import org.almostrealism.hardware.cl.HardwareOperator;
import org.almostrealism.hardware.jni.NativeComputeContext;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.optimize.PopulationOptimizer;

public class AudioSceneOptimizer extends AudioPopulationOptimizer<Cells> {
	public static final int verbosity = 0;

	public static final boolean enableSourcesJson = true;
	public static final int singleChannel = -1;

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

	public AudioSceneOptimizer(AudioScene<?> scene,
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

	public static AudioSceneOptimizer build(AudioScene<?> scene, int cycles) {
		return build(() -> scene.getGenome()::random, scene, cycles);
	}

	public static AudioSceneOptimizer build(Supplier<Supplier<Genome<PackedCollection<?>>>> generator, AudioScene<?> scene, int cycles) {
		return new AudioSceneOptimizer(scene, scene::getBreeder, generator, cycles);
	}

	/**
	 * Build a {@link AudioSceneOptimizer} and initialize and run it.
	 *
	 * @see  AudioSceneOptimizer#build(AudioScene, int)
	 * @see  AudioSceneOptimizer#init
	 * @see  AudioSceneOptimizer#run()
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
		PopulationOptimizer.enableBreeding = true; // verbosity < 3;

		AdjustableDelayCell.defaultPurgeFrequency = 1.0;
		// HealthCallable.setComputeRequirements(ComputeRequirement.C);
		// HealthCallable.setComputeRequirements(ComputeRequirement.PROFILING);
		// Hardware.getLocalHardware().setMaximumOperationDepth(7);

		// WaveData.setCollectionHeap(() -> new PackedCollectionHeap(25000 * OutputLine.sampleRate), PackedCollectionHeap::destroy);

		AudioScene<?> scene = createScene();
		AudioSceneOptimizer opt = build(scene, PopulationOptimizer.enableBreeding ? 10 : 1);
		opt.init();
		opt.run();
	}

	public static AudioScene<?> createScene() throws IOException {
		double bpm = 120.0;
		int sourceCount = AudioScene.DEFAULT_SOURCE_COUNT;
		int delayLayers = AudioScene.DEFAULT_DELAY_LAYERS;
		AudioScene<?> scene = new AudioScene<>(null, bpm, sourceCount, delayLayers,
										OutputLine.sampleRate, new NoOpGenerationProvider());

		scene.getPatternManager().getChoices().addAll(createChoices());
		scene.setTuning(new DefaultKeyboardTuning());
		scene.setLibraryRoot(new FileWaveDataProviderNode(new File(LIBRARY)));

		AudioScene.Settings settings = AudioScene.Settings.defaultSettings(sourceCount,
				AudioScene.DEFAULT_PATTERNS_PER_CHANNEL,
				AudioScene.DEFAULT_ACTIVE_PATTERNS,
				AudioScene.DEFAULT_LAYERS,
				AudioScene.DEFAULT_DURATION);

		if (singleChannel >= 0) {
			PatternSystemManager.enableWarnings = false;
			// PatternLayerManager.enableLogging = true;
			// DefaultChannelSectionFactory.enableFilter = false;

			// settings.setWetChannels(Collections.emptyList());

			settings.getPatternSystem().setPatterns(
					settings
							.getPatternSystem()
							.getPatterns()
							.stream()
							.filter(p -> p.getChannel() == singleChannel)
							.collect(Collectors.toList()));
		}

		scene.setSettings(settings);
		return scene;
	}

	private static List<PatternFactoryChoice> createChoices() throws IOException {
		if (enableSourcesJson) {
			PatternFactoryChoiceList choices = new ObjectMapper()
					.readValue(new File("pattern-factory.json"), PatternFactoryChoiceList.class);
			return choices;
		} else {
			List<PatternFactoryChoice> choices = new ArrayList<>();

			PatternFactoryChoice kick = new PatternFactoryChoice(new PatternElementFactory("Kicks", PatternNote.create("Kit/Kick.wav")));
			kick.setSeed(true);
			kick.setMinScale(0.25);
			choices.add(kick);

			PatternFactoryChoice clap = new PatternFactoryChoice(new PatternElementFactory("Clap/Snare", PatternNote.create("Kit/Clap.wav")));
			clap.setMaxScale(0.5);
			choices.add(clap);

			PatternFactoryChoice toms = new PatternFactoryChoice(
					new PatternElementFactory("Toms", PatternNote.create("Kit/Tom1.wav"),
							PatternNote.create("Kit/Tom2.wav")));
			toms.setMaxScale(0.25);
			choices.add(toms);

			PatternFactoryChoice hats = new PatternFactoryChoice(new PatternElementFactory("Hats"));
			hats.setMaxScale(0.25);
			choices.add(hats);

			return choices;
		}
	}
}