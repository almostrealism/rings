/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.almostrealism.relation.Tree;
import io.almostrealism.cycle.Setup;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.arrange.EfxManager;
import org.almostrealism.audio.arrange.GlobalTimeManager;
import org.almostrealism.audio.arrange.MixdownManager;
import org.almostrealism.audio.arrange.RiseManager;
import org.almostrealism.audio.arrange.SceneSectionManager;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.data.PathResource;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.generative.GenerationManager;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.NoOpGenerationProvider;
import org.almostrealism.audio.pattern.ChordProgressionManager;
import org.almostrealism.audio.pattern.PatternSystemManager;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.CombinedGenome;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.ParameterGenome;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.space.Animation;
import io.almostrealism.uml.ModelEntity;
import org.almostrealism.time.Frequency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ModelEntity
public class AudioScene<T extends ShadableSurface> implements Setup, CellFeatures {
	public static final int DEFAULT_SOURCE_COUNT = 6;
	public static final int DEFAULT_DELAY_LAYERS = 3;
	public static final int DEFAULT_PATTERNS_PER_CHANNEL = 6;
	public static final IntUnaryOperator DEFAULT_ACTIVE_PATTERNS;
	public static final IntUnaryOperator DEFAULT_LAYERS;
	public static final IntUnaryOperator DEFAULT_DURATION;
	public static final IntPredicate DEFAULT_REPEAT_CHANNELS = c -> c != 5;

	static {
		DEFAULT_ACTIVE_PATTERNS = c ->
				switch (c) {
					case 0 -> 5; // 4;
					case 1 -> 5; // 4;
					case 2 -> 2; // 1;
					case 3 -> 2; // 2;
					case 4 -> 2; // 1;
					case 5 -> 2; // 1;
					default -> throw new IllegalArgumentException("Unexpected value: " + c);
				};

		DEFAULT_LAYERS = c ->
				switch (c) {
					case 0 -> 5;
					case 1 -> 5;
					case 2 -> 6; // 4;
					case 3 -> 6; // 5;
					case 4 -> 5; // 4;
					case 5 -> 1;
					default -> throw new IllegalArgumentException("Unexpected value: " + c);
				};

		DEFAULT_DURATION = c ->
				switch (c) {
					case 0 -> 1;
					case 1 -> 4;
					case 2 -> 16;
					case 3 -> 16;
					case 4 -> 8;
					case 5 -> 16;
					default -> (int) Math.pow(2.0, c - 1);
				};
	}

	public static final int mixdownDuration = 140;

	public static boolean enableMainFilterUp = true;
	public static boolean enableEfxFilters = true;
	public static boolean enableEfx = true;
	public static boolean enableWetInAdjustment = true;
	public static boolean enableMasterFilterDown = true;

	public static boolean enableMixdown = false;
	public static boolean enableSourcesOnly = false;
	public static boolean disableClean = false;

	private int sampleRate;
	private double bpm;
	private int channelCount;
	private int delayLayerCount;
	private int measureSize = 4;
	private int totalMeasures = 1;

	private Animation<T> scene;

	private GlobalTimeManager time;
	private KeyboardTuning tuning;
	private ChordProgressionManager progression;

	private Tree<? extends Supplier<FileWaveDataProvider>> library;
	private PatternSystemManager patterns;
	private PackedCollection<?> patternDestination;
	private List<String> channelNames;

	private SceneSectionManager sections;
	private EfxManager efx;
	private RiseManager riser;
	private MixdownManager mixdown;

	private GenerationManager generation;

	private CombinedGenome genome;
	
	private OperationList setup;

	private List<Consumer<Frequency>> tempoListeners;
	private List<DoubleConsumer> durationListeners;

	public AudioScene(Animation<T> scene, double bpm, int channels, int delayLayers,
					  int sampleRate) {
		this(scene, bpm, channels, delayLayers, sampleRate, new NoOpGenerationProvider());
	}

	public AudioScene(Animation<T> scene, double bpm, int channels, int delayLayers,
					  int sampleRate, GenerationProvider generation) {
		this.sampleRate = sampleRate;
		this.bpm = bpm;
		this.channelCount = channels;
		this.delayLayerCount = delayLayers;
		this.scene = scene;

		this.tempoListeners = new ArrayList<>();
		this.durationListeners = new ArrayList<>();

		this.time = new GlobalTimeManager(measure -> (int) (measure * getMeasureDuration() * getSampleRate()));

		this.genome = new CombinedGenome(5);

		this.tuning = new DefaultKeyboardTuning();
		this.sections = new SceneSectionManager(genome.getGenome(0), channels, this::getTempo, this::getMeasureDuration, getSampleRate());
		this.progression = new ChordProgressionManager(genome.getGenome(1), WesternScales.minor(WesternChromatic.G1, 1));
		this.progression.setSize(16);
		this.progression.setDuration(8);

		patterns = new PatternSystemManager(genome.getGenome(2));
		patterns.init();

		this.channelNames = new ArrayList<>();

		addDurationListener(duration -> patternDestination = null);

		this.mixdown = new MixdownManager(genome.getGenome(3), channels, delayLayers,
										time.getClock(), getSampleRate());
		this.efx = new EfxManager(genome.getGenome(4), channels, this::getBeatDuration, getSampleRate());

		this.generation = new GenerationManager(patterns, generation);
	}

	public void setBPM(double bpm) {
		this.bpm = bpm;
		tempoListeners.forEach(l -> l.accept(Frequency.forBPM(bpm)));
		triggerDurationChange();
	}

	public double getBPM() { return this.bpm; }

	public Frequency getTempo() { return Frequency.forBPM(bpm); }

	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;
		patterns.setTuning(tuning);
	}

	public KeyboardTuning getTuning() { return tuning; }

	public void setLibraryRoot(Tree<? extends Supplier<FileWaveDataProvider>> tree) {
		setLibraryRoot(tree, null);
	}

	public void setLibraryRoot(Tree<? extends Supplier<FileWaveDataProvider>> tree, DoubleConsumer progress) {
		library = tree;
		patterns.setTree(tree, progress);
	}

	public Animation<T> getScene() { return scene; }

	public ParameterGenome getGenome() { return genome.getParameters(); }

	public GenomeBreeder<PackedCollection<?>> getBreeder() {
		return genome.getBreeder();

//		GenomeBreeder<PackedCollection<?>> legacyBreeder = new DefaultGenomeBreeder(
//				Breeders.of(Breeders.randomChoiceBreeder(),
//						Breeders.randomChoiceBreeder(),
//						Breeders.randomChoiceBreeder(),
//						Breeders.averageBreeder()), 							   // GENERATORS
//				Breeders.averageBreeder(),										   // PARAMETERS
//				Breeders.averageBreeder(),  									   // VOLUME
//				Breeders.averageBreeder(),  									   // MAIN FILTER UP
//				Breeders.averageBreeder(),  									   // WET IN
//				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
//				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
//				Breeders.averageBreeder(),  									   // WET OUT
//				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // FILTERS
//				Breeders.averageBreeder());  									   // MASTER FILTER DOWN
	}

	public void assignGenome(Genome<PackedCollection<?>> genome) {
		this.genome.assignTo(genome);
		this.progression.refreshParameters();
		this.patterns.refreshParameters();
	}

	public void addSection(int position, int length) {
		sections.addSection(position, length);
	}
	public void addBreak(int measure) { time.addReset(measure); }

	public GlobalTimeManager getTimeManager() { return time; }
	public SceneSectionManager getSectionManager() { return sections; }
	public ChordProgressionManager getChordProgression() { return progression; }
	public PatternSystemManager getPatternManager() { return patterns; }
	public EfxManager getEfxManager() { return efx; }
	public GenerationManager getGenerationManager() { return generation; }

	public void addTempoListener(Consumer<Frequency> listener) { this.tempoListeners.add(listener); }
	public void removeTempoListener(Consumer<Frequency> listener) { this.tempoListeners.remove(listener); }

	public void addDurationListener(DoubleConsumer listener) { this.durationListeners.add(listener); }
	public void removeDurationListener(DoubleConsumer listener) { this.durationListeners.remove(listener); }

	public int getChannelCount() { return channelCount; }
	public int getDelayLayerCount() { return delayLayerCount; }

	public List<String> getChannelNames() { return channelNames; }

	public double getBeatDuration() { return getTempo().l(1); }

	public void setMeasureSize(int measureSize) { this.measureSize = measureSize; triggerDurationChange(); }
	public int getMeasureSize() { return measureSize; }
	public double getMeasureDuration() { return getTempo().l(getMeasureSize()); }
	public int getMeasureSamples() { return (int) (getMeasureDuration() * getSampleRate()); }

	public void setTotalMeasures(int measures) { this.totalMeasures = measures; triggerDurationChange(); }
	public int getTotalMeasures() { return totalMeasures; }
	public int getTotalBeats() { return totalMeasures * measureSize; }
	public double getTotalDuration() { return getTempo().l(getTotalBeats()); }
	public int getTotalSamples() { return (int) (getTotalDuration() * getSampleRate()); }

	public int getSampleRate() { return sampleRate; }

	public AudioSceneContext getContext() {
		AudioSceneContext context = new AudioSceneContext();
		context.setMeasures(getTotalMeasures());
		context.setFrameForPosition(pos -> (int) (pos * getMeasureSamples()));
		context.setTimeForDuration(len -> len * getMeasureDuration());
		context.setScaleForPosition(getChordProgression()::forPosition);
		context.setDestination(patternDestination);
		context.setIntermediateDestination(() -> WaveData.allocateCollection(getTotalSamples()));
		return context;
	}

	public Settings getSettings() {
		Settings settings = new Settings();
		settings.setBpm(getBPM());
		settings.setMeasureSize(getMeasureSize());
		settings.setTotalMeasures(getTotalMeasures());
		settings.getBreaks().addAll(time.getResets());
		settings.getSections().addAll(sections.getSections()
				.stream().map(s -> new Settings.Section(s.getPosition(), s.getLength())).collect(Collectors.toList()));

		if (library instanceof PathResource) {
			settings.setLibraryRoot(((PathResource) library).getResourcePath());
		}

		settings.setChordProgression(progression.getSettings());
		settings.setPatternSystem(patterns.getSettings());
		settings.setChannelNames(channelNames);
		settings.setWetChannels(getEfxManager().getWetChannels());
		settings.setGeneration(generation.getSettings());

		return settings;
	}

	public void setSettings(Settings settings) { setSettings(settings, this::createTree, null); }

	public void setSettings(Settings settings,
							Function<String, Tree<? extends Supplier<FileWaveDataProvider>>> libraryProvider,
							DoubleConsumer progress) {
		setBPM(settings.getBpm());
		setMeasureSize(settings.getMeasureSize());
		setTotalMeasures(settings.getTotalMeasures());

		time.getResets().clear();
		settings.getBreaks().forEach(time::addReset);
		settings.getSections().forEach(s -> sections.addSection(s.getPosition(), s.getLength()));

		if (settings.getLibraryRoot() != null) {
			setLibraryRoot(libraryProvider.apply(settings.getLibraryRoot()), progress);
		}

		progression.setSettings(settings.getChordProgression());
		patterns.setSettings(settings.getPatternSystem());

		channelNames.clear();
		if (settings.getChannelNames() != null) channelNames.addAll(settings.getChannelNames());

		getEfxManager().getWetChannels().clear();
		getSectionManager().getWetChannels().clear();
		if (settings.getWetChannels() != null) {
			getEfxManager().getWetChannels().addAll(settings.getWetChannels());
			getSectionManager().getWetChannels().addAll(settings.getWetChannels());
		}

		generation.setSettings(settings.getGeneration());
	}

	protected void triggerDurationChange() {
		durationListeners.forEach(l -> l.accept(getTotalDuration()));
	}

	public WaveData getPatternDestination() { return new WaveData(patternDestination, getSampleRate()); }

	public boolean checkResourceUsed(String canonicalPath) {
		return getPatternManager().getChoices().stream().anyMatch(p -> p.getFactory().checkResourceUsed(canonicalPath));
	}

	@Override
	public Supplier<Runnable> setup() { return setup; }

	public Cells getCells(List<? extends Receptor<PackedCollection<?>>> measures,
						  List<? extends Receptor<PackedCollection<?>>> stems,
						  Receptor<PackedCollection<?>> output) {
		return getCells(measures, stems, output,
				IntStream.range(0, getChannelCount())
						.mapToObj(i -> i).collect(Collectors.toList()));
	}

	public Cells getCells(List<? extends Receptor<PackedCollection<?>>> measures,
						  List<? extends Receptor<PackedCollection<?>>> stems,
						  Receptor<PackedCollection<?>> output,
						  List<Integer> channels) {
		CellList cells;

		setup = new OperationList("AudioScene Setup");
		setup.add(mixdown.setup());
		setup.add(time.setup());

		cells = getPatternCells(measures, stems, output, channels, setup);
		return cells.addRequirement(time::tick);
	}

	public CellList getPatternCells(List<? extends Receptor<PackedCollection<?>>> measures,
									List<? extends Receptor<PackedCollection<?>>> stems,
									Receptor<PackedCollection<?>> output,
									List<Integer> channels,
									OperationList setup) {
		int channelIndex[] = channels.stream().mapToInt(i -> i).toArray();

//		CellList cells = all(channelCount, i -> efx.apply(i, getPatternChannel(i, setup)));
		CellList cells = all(channelIndex.length, i -> efx.apply(channelIndex[i], getPatternChannel(channelIndex[i], setup)));
		return mixdown.cells(cells, measures, stems, output, i -> channelIndex[i]);
	}

	public CellList getPatternChannel(int channel, OperationList setup) {
		PackedCollection<?> audio = new PackedCollection<>(shape(getTotalSamples()), 0);

		OperationList patternSetup = new OperationList("PatternChannel Setup");
		patternSetup.add(() -> () -> patterns.setTuning(tuning));
		patternSetup.add(sections.setup());
		patternSetup.add(getPatternSetup(channel));
		patternSetup.add(() -> () ->
				audio.setMem(0, patternDestination, 0, patternDestination.getMemLength()));

		sections.getChannelSections(channel).stream()
				.map(section -> {
					int pos = section.getPosition() * getMeasureSamples();
					int len = section.getLength() * getMeasureSamples();
					PackedCollection<?> sectionAudio = audio.range(shape(len), pos);
					return section.process(p(sectionAudio), p(sectionAudio));
				})
				.forEach(patternSetup::add);

		setup.add(patternSetup);
		return w(PolymorphicAudioData.supply(PackedCollection.factory()), null, c(getTotalDuration()),
				new WaveData(audio.traverseEach(), getSampleRate()));
	}

	public Supplier<Runnable> getPatternSetup(int channel) {
		Supplier<AudioSceneContext> ctx = () -> {
			AudioSceneContext context = getContext();
			context.setChannels(List.of(channel));
			context.setSections(sections.getChannelSections(channel));
			return context;
		};

		OperationList op = new OperationList("AudioScene Pattern Setup");
		op.add(() -> () -> refreshPatternDestination());
		op.add(patterns.sum(ctx));
		return op;
	}

	private void refreshPatternDestination() {
		if (patternDestination == null) {
			patternDestination = new PackedCollection(getTotalSamples());
		} else {
			patternDestination.clear();
		}
	}

	public void saveSettings(File file) throws IOException {
		new ObjectMapper().writeValue(file, getSettings());
	}

	public void loadSettings(File file) throws IOException {
		loadSettings(file, this::createTree, null);
	}

	public void loadSettings(File file, Function<String, Tree<? extends Supplier<FileWaveDataProvider>>> libraryProvider, DoubleConsumer progress) throws IOException {
		if (file.exists()) {
			setSettings(new ObjectMapper().readValue(file, AudioScene.Settings.class), libraryProvider, progress);
		} else {
			setSettings(Settings.defaultSettings(getChannelCount(),
					DEFAULT_PATTERNS_PER_CHANNEL,
					DEFAULT_ACTIVE_PATTERNS,
					DEFAULT_LAYERS,
					DEFAULT_DURATION), libraryProvider, progress);
		}
	}

	public static class Settings {
		private double bpm = 120;
		private int measureSize = 4;
		private int totalMeasures = 64;
		private List<Integer> breaks = new ArrayList<>();
		private List<Section> sections = new ArrayList<>();
		private String libraryRoot;

		private ChordProgressionManager.Settings chordProgression;
		private PatternSystemManager.Settings patternSystem;
		private List<String> channelNames;
		private List<Integer> wetChannels;

		private GenerationManager.Settings generation;

		public Settings() {
			patternSystem = new PatternSystemManager.Settings();
			generation = new GenerationManager.Settings();
		}

		public double getBpm() { return bpm; }
		public void setBpm(double bpm) { this.bpm = bpm; }

		public int getMeasureSize() { return measureSize; }
		public void setMeasureSize(int measureSize) { this.measureSize = measureSize; }

		public int getTotalMeasures() { return totalMeasures; }
		public void setTotalMeasures(int totalMeasures) { this.totalMeasures = totalMeasures; }

		public List<Integer> getBreaks() { return breaks; }
		public void setBreaks(List<Integer> breaks) { this.breaks = breaks; }

		public List<Section> getSections() { return sections; }
		public void setSections(List<Section> sections) { this.sections = sections; }

		public String getLibraryRoot() { return libraryRoot; }
		public void setLibraryRoot(String libraryRoot) { this.libraryRoot = libraryRoot; }

		public ChordProgressionManager.Settings getChordProgression() { return chordProgression; }
		public void setChordProgression(ChordProgressionManager.Settings chordProgression) { this.chordProgression = chordProgression; }
		
		public PatternSystemManager.Settings getPatternSystem() { return patternSystem; }
		public void setPatternSystem(PatternSystemManager.Settings patternSystem) { this.patternSystem = patternSystem; }

		public List<String> getChannelNames() { return channelNames; }
		public void setChannelNames(List<String> channelNames) { this.channelNames = channelNames; }

		public List<Integer> getWetChannels() { return wetChannels; }
		public void setWetChannels(List<Integer> wetChannels) { this.wetChannels = wetChannels; }

		public GenerationManager.Settings getGeneration() { return generation; }
		public void setGeneration(GenerationManager.Settings generation) { this.generation = generation; }

		public static class Section {
			private int position, length;

			public Section() { }

			public Section(int position, int length) {
				this.position = position;
				this.length = length;
			}

			public int getPosition() { return position; }
			public void setPosition(int position) { this.position = position; }

			public int getLength() { return length; }
			public void setLength(int length) { this.length = length; }
		}

		public static Settings defaultSettings(int channels, int patternsPerChannel,
											   IntUnaryOperator activePatterns,
											   IntUnaryOperator layersPerPattern,
											   IntUnaryOperator duration) {
			Settings settings = new Settings();
			settings.getSections().add(new Section(0, 16));
			settings.getSections().add(new Section(16, 16));
			settings.getSections().add(new Section(32, 8));
			settings.getBreaks().add(40);
			settings.getSections().add(new Section(40, 16));
			settings.getSections().add(new Section(56, 16));
			settings.getSections().add(new Section(72, 8));
			settings.getBreaks().add(80);
			settings.getSections().add(new Section(80, 64));
			settings.setTotalMeasures(144);
			settings.setChordProgression(ChordProgressionManager.Settings.defaultSettings());
			settings.setPatternSystem(PatternSystemManager.Settings
					.defaultSettings(channels, patternsPerChannel, activePatterns, layersPerPattern, duration));
			settings.setChannelNames(List.of("Kick", "Drums", "Bass", "Harmony", "Lead", "Atmosphere"));
			settings.setWetChannels(List.of(3, 4, 5));
			return settings;
		}
	}

	private Tree<? extends Supplier<FileWaveDataProvider>> createTree(String f) {
		return new FileWaveDataProviderNode(new File(f));
	}
}
