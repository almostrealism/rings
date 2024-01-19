/*
 * Copyright 2024 Michael Murray
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.almostrealism.code.OperationProfile;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.arrange.MixdownManager;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.filter.AudioSumProvider;
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
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.pattern.PatternSystemManager;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.hardware.AcceleratedOperation;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.HardwareOperator;
import org.almostrealism.hardware.cl.CLMemoryProvider;
import org.almostrealism.hardware.jni.NativeCompiler;
import org.almostrealism.hardware.jni.NativeComputeContext;
import org.almostrealism.hardware.mem.Heap;
import org.almostrealism.hardware.mem.MemoryDataArgumentMap;
import org.almostrealism.hardware.metal.MetalMemoryProvider;
import org.almostrealism.hardware.metal.MetalProgram;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.optimize.PopulationOptimizer;
import org.almostrealism.time.TemporalRunner;

public class AudioSceneOptimizer extends AudioPopulationOptimizer<Cells> {
	public static final int verbosity = 0;
	public static boolean enableVerbose = false;

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

	private AudioScenePopulation population;

	public AudioSceneOptimizer(AudioScene<?> scene,
							   Supplier<GenomeBreeder<PackedCollection<?>>> breeder,
							   Supplier<Supplier<Genome<PackedCollection<?>>>> generator,
							   int totalCycles) {
		super(scene.getChannelCount() + 1, null, breeder, generator, "Population.xml", totalCycles);
		setChildrenFunction(
				children -> {
					if (children.isEmpty()) throw new IllegalArgumentException();

					if (population == null) {
						population = new AudioScenePopulation(scene, children);
						AudioHealthComputation hc = (AudioHealthComputation) getHealthComputation();

						if (enableVerbose) log("Initializing AudioScenePopulation");
						population.init(population.getGenomes().get(0), hc.getMeasures(), hc.getStems(), hc.getOutput());
						if (enableVerbose) log("AudioScenePopulation initialized (getCells duration = " + AudioScene.console.timing("getCells").getTotal() + ")");
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
		HardwareOperator.profile = new OperationProfile("HardwareOperator");
		MemoryDataArgumentMap.profile = new OperationProfile("MemoryDataArgumentMap");

		NativeComputeContext.enableLargeScopeMonitoring = false;
		TemporalRunner.enableOptimization = false;
		TemporalRunner.enableIsolation = false;

		StableDurationHealthComputation.enableTimeout = false;
		MixdownManager.enableReverb = true;
		AudioScene.enableMainFilterUp = true;
		AudioScene.enableEfxFilters = true;
		AudioScene.enableEfx = true;
		AudioScene.enableWetInAdjustment = true;
		AudioScene.enableMasterFilterDown = true;
		AudioScene.disableClean = false;
		AudioScene.enableSourcesOnly = false;
		PatternElementFactory.enableEnvelope = true;
		PatternLayerManager.enableVolumeAdjustment = true;
		SilenceDurationHealthComputation.enableSilenceCheck = false;
		AudioPopulationOptimizer.enableIsolatedContext = false;
		AudioPopulationOptimizer.enableStemOutput = true;
		PopulationOptimizer.popSize = 65;

		// Verbosity level 1
		NativeCompiler.enableLargeInstructionSetMonitoring = verbosity > 0;
		CLMemoryProvider.enableLargeAllocationLogging = verbosity > 0;
		MetalMemoryProvider.enableLargeAllocationLogging = verbosity > 0;
		PatternNote.enableVerbose = verbosity > 0;

		// Verbosity level 2
		WaveOutput.enableVerbose = verbosity > 1;
		AudioSceneOptimizer.enableVerbose = verbosity > 1;
		PopulationOptimizer.enableVerbose = verbosity > 1;
		MetalProgram.enableLargeProgramMonitoring = verbosity > 1;

		// Verbosity level 3
		PatternSystemManager.enableVerbose = verbosity > 2;
		SilenceDurationHealthComputation.enableVerbose = verbosity > 2;
		PopulationOptimizer.enableDisplayGenomes = verbosity > 2;
		NativeComputeContext.enableVerbose = verbosity > 2;
		Hardware.enableVerbose = verbosity > 2;
		HardwareOperator.enableLog = verbosity > 2;
		NativeCompiler.enableInstructionSetMonitoring = verbosity > 2;

		// Verbosity level 4
		HardwareOperator.enableVerboseLog = verbosity > 3;

		// PopulationOptimizer.THREADS = verbosity < 1 ? 2 : 1;
		PopulationOptimizer.enableBreeding = verbosity < 1;

		AdjustableDelayCell.defaultPurgeFrequency = 1.0;
		// HealthCallable.setComputeRequirements(ComputeRequirement.PROFILING);
		// Hardware.getLocalHardware().setMaximumOperationDepth(7);

		Heap heap = new Heap(12 * 1024 * 1024);

		heap.use(() -> {
			try {
				AudioScene<?> scene = createScene();
				AudioSceneOptimizer opt = build(scene, PopulationOptimizer.enableBreeding ? 10 : 1);
				opt.init();
				opt.run();

				HardwareOperator.profile.print();

				if (WavCellChromosome.timing.getTotal() > 60)
					WavCellChromosome.timing.print();

				if (enableVerbose)
					PatternLayerManager.sizes.print();

				if (AudioSumProvider.timing.getTotal() > 120)
					AudioSumProvider.timing.print();

				if (MemoryDataArgumentMap.profile.getMetric().getTotal() > 10)
					MemoryDataArgumentMap.profile.print();

				AcceleratedOperation.printTimes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
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
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			PatternFactoryChoiceList choices = mapper
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
