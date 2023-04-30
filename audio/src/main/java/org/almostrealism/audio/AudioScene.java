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
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.audio.arrange.EfxManager;
import org.almostrealism.audio.arrange.GlobalTimeManager;
import org.almostrealism.audio.arrange.MixdownManager;
import org.almostrealism.audio.arrange.SceneSectionManager;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.generative.GenerationManager;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.NoOpGenerationProvider;
import org.almostrealism.audio.optimize.AudioSceneGenome;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.audio.pattern.ChordProgressionManager;
import org.almostrealism.audio.pattern.PatternSystemManager;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.Breeders;
import org.almostrealism.heredity.CombinedGenome;
import org.almostrealism.heredity.DefaultGenomeBreeder;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.GenomeBreeder;
import org.almostrealism.heredity.ParameterGenome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.space.Animation;
import io.almostrealism.uml.ModelEntity;
import org.almostrealism.time.Frequency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ModelEntity
public class AudioScene<T extends ShadableSurface> implements Setup, CellFeatures {
	public static final int DEFAULT_PATTERNS_PER_CHANNEL = 6;

	public static final int mixdownDuration = 140;

	public static final boolean enablePatternSystem = true;
	public static final boolean enableRepeat = true;
	public static boolean enableMainFilterUp = true;
	public static boolean enableEfxFilters = true;
	public static boolean enableEfx = true;
	public static boolean enableWetInAdjustment = true;
	public static boolean enableMasterFilterDown = true;

	public static boolean enableMixdown = false;
	public static boolean enableSourcesOnly = false;
	public static boolean disableClean = false;

	public static Waves sourceOverride = null;

	private int sampleRate;
	private double bpm;
	private int sourceCount;
	private int delayLayerCount;
	private int measureSize = 4;
	private int totalMeasures = 1;

	private Animation<T> scene;
	private Waves sources;

	private KeyboardTuning tuning;
	private GlobalTimeManager time;
	private SceneSectionManager sections;
	private ChordProgressionManager progression;

	private PatternSystemManager patterns;
	private PackedCollection<?> patternDestination;
	private List<String> channelNames;

	private MixdownManager mixdown;
	private EfxManager efx;

	private GenerationManager generation;

	private CombinedGenome genome;
	private DefaultAudioGenome legacyGenome;
	
	private OperationList setup;

	private List<Consumer<Frequency>> tempoListeners;
	private List<DoubleConsumer> durationListeners;
	private List<Consumer<Waves>> sourcesListener;

	public AudioScene(Animation<T> scene, double bpm, int sources, int delayLayers,
					  int sampleRate) {
		this(scene, bpm, sources, delayLayers, sampleRate, new NoOpGenerationProvider());
	}

	public AudioScene(Animation<T> scene, double bpm, int sources, int delayLayers,
					  int sampleRate, GenerationProvider generation) {
		this.sampleRate = sampleRate;
		this.bpm = bpm;
		this.sourceCount = sources;
		this.delayLayerCount = delayLayers;
		this.scene = scene;

		this.tempoListeners = new ArrayList<>();
		this.durationListeners = new ArrayList<>();
		this.sourcesListener = new ArrayList<>();

		this.time = new GlobalTimeManager(measure -> (int) (measure * getMeasureDuration() * getSampleRate()));

		this.genome = new CombinedGenome(5);
		this.legacyGenome = new DefaultAudioGenome(sources, delayLayers, sampleRate, time.getClock().frame());

		this.tuning = new DefaultKeyboardTuning();
		this.sections = new SceneSectionManager(genome.getGenome(0), sources, this::getMeasureDuration, getSampleRate());
		this.progression = new ChordProgressionManager(genome.getGenome(1), WesternScales.minor(WesternChromatic.G1, 1));
		this.progression.setSize(16);
		this.progression.setDuration(8);

		this.sources = new Waves();
		IntStream.range(0, sourceCount).forEach(this.sources.getChoices().getChoices()::add);

		patterns = new PatternSystemManager(genome.getGenome(2));
		patterns.init();

		this.channelNames = new ArrayList<>();

		addDurationListener(duration -> patternDestination = null);

		this.mixdown = new MixdownManager(genome.getGenome(3), sources, time.getClock(), getSampleRate());
		this.efx = new EfxManager(genome.getGenome(4), sources, this::getBeatDuration, getSampleRate());

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
		patterns.setTuning(tuning);
	}

	public Animation<T> getScene() { return scene; }

	public ParameterGenome getGenome() { return genome.getParameters(); }

	@Deprecated
	public DefaultAudioGenome getLegacyGenome() { return legacyGenome; }

	public GenomeBreeder<PackedCollection<?>> getBreeder() {
		GenomeBreeder<PackedCollection<?>> breeder = genome.getBreeder();

		GenomeBreeder<PackedCollection<?>> legacyBreeder = new DefaultGenomeBreeder(
				Breeders.of(Breeders.randomChoiceBreeder(),
						Breeders.randomChoiceBreeder(),
						Breeders.randomChoiceBreeder(),
						Breeders.averageBreeder()), 							   // GENERATORS
				Breeders.averageBreeder(),										   // PARAMETERS
				Breeders.averageBreeder(),  									   // VOLUME
				Breeders.averageBreeder(),  									   // MAIN FILTER UP
				Breeders.averageBreeder(),  									   // WET IN
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // DELAY
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // ROUTING
				Breeders.averageBreeder(),  									   // WET OUT
				Breeders.perturbationBreeder(0.0005, ScaleFactor::new),  // FILTERS
				Breeders.averageBreeder());  									   // MASTER FILTER DOWN

		return (g1, g2) -> {
			AudioSceneGenome asg1 = (AudioSceneGenome) g1;
			AudioSceneGenome asg2 = (AudioSceneGenome) g2;
			return new AudioSceneGenome(breeder.combine(asg1.getGenome(), asg2.getGenome()),
					legacyBreeder.combine(asg1.getLegacyGenome(), asg2.getLegacyGenome()));
		};
	}

	public void assignGenome(Genome<PackedCollection<?>> genome) {
		AudioSceneGenome g = (AudioSceneGenome) genome;
		this.genome.assignTo(g.getGenome());
		if (g.getLegacyGenome() != null && this.legacyGenome != null)
			this.legacyGenome.assignTo(g.getLegacyGenome());
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

	public void addSourcesListener(Consumer<Waves> listener) { this.sourcesListener.add(listener); }
	public void removeSourcesListener(Consumer<Waves> listener) { this.sourcesListener.remove(listener); }

	public int getSourceCount() { return sourceCount; }
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

	public Settings getSettings() {
		Settings settings = new Settings();
		settings.setBpm(getBPM());
		settings.setMeasureSize(getMeasureSize());
		settings.setTotalMeasures(getTotalMeasures());
		settings.getBreaks().addAll(time.getResets());
		settings.getSections().addAll(sections.getSections()
				.stream().map(s -> new Settings.Section(s.getPosition(), s.getLength())).collect(Collectors.toList()));
		settings.setChordProgression(progression.getSettings());
		settings.setPatternSystem(patterns.getSettings());
		settings.setChannelNames(channelNames);
		settings.setWetChannels(getEfxManager().getWetChannels());
		settings.setGeneration(generation.getSettings());
		return settings;
	}

	public void setSettings(Settings settings) {
		setBPM(settings.getBpm());
		setMeasureSize(settings.getMeasureSize());
		setTotalMeasures(settings.getTotalMeasures());

		time.getResets().clear();
		settings.getBreaks().forEach(time::addReset);
		settings.getSections().forEach(s -> sections.addSection(s.getPosition(), s.getLength()));
		progression.setSettings(settings.getChordProgression());
		patterns.setSettings(settings.getPatternSystem());

		channelNames.clear();
		if (settings.getChannelNames() != null) channelNames.addAll(settings.getChannelNames());

		getEfxManager().getWetChannels().clear();
		if (settings.getWetChannels() != null) getEfxManager().getWetChannels().addAll(settings.getWetChannels());

		generation.setSettings(settings.getGeneration());
	}

	public void setWaves(Waves waves) {
		this.sources = waves;
		sourcesListener.forEach(l -> l.accept(sources));
	}

	// This is needed because AudioScene doesn't manage save
	// and restore itself. Once it does, this can be removed.
	@Deprecated
	public void triggerSourcesChange() {
		sourcesListener.forEach(l -> l.accept(sources));
	}

	protected void triggerDurationChange() {
		durationListeners.forEach(l -> l.accept(getTotalDuration()));
	}

	public Waves getWaves() { return sources; }

	public WaveData getPatternDestination() { return new WaveData(patternDestination, getSampleRate()); }

	@Override
	public Supplier<Runnable> setup() { return setup; }

	public Cells getCells(List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output) {
		CellList cells;

		setup = new OperationList("AudioScene Setup");
		if (legacyGenome != null) setup.add(legacyGenome.setup());
		setup.add(mixdown.setup());
		setup.add(time.setup());

		if (enablePatternSystem) {
			cells = getPatternCells(measures, output, setup);
		} else {
			cells = getWavesCells(measures, output);
		}

		return cells.addRequirement(time::tick);
	}

	public CellList getPatternCells(List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output, OperationList setup) {
		CellList cells = all(sourceCount, i -> efx.apply(i, getPatternChannel(i, setup)));
		return mixdown.cells(legacyGenome, cells, measures, output);
	}

	public CellList getPatternChannel(int channel, OperationList setup) {
		PackedCollection<?> audio = WaveData.allocateCollection(getTotalSamples());

		OperationList patternSetup = new OperationList("PatternChannel Setup");
		patternSetup.add(() -> () -> patterns.setTuning(tuning));
		patternSetup.add(sections.setup());
		patternSetup.add(getPatternSetup(List.of(channel)));
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
		return w(c(getTotalDuration()), new WaveData(audio.traverseEach(), getSampleRate()));
	}

	public Supplier<Runnable> getPatternSetup() { return getPatternSetup(null); }

	public Supplier<Runnable> getPatternSetup(List<Integer> channels) {
		return () -> () -> {
			refreshPatternDestination();
			patterns.sum(channels, pos -> (int) (pos * getMeasureSamples()),
					getTotalMeasures(), getChordProgression()::forPosition,
					patternDestination, () -> WaveData.allocateCollection(getTotalSamples()));
		};
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
		if (file.exists()) {
			setSettings(new ObjectMapper().readValue(file, AudioScene.Settings.class));
		} else {
			setSettings(Settings.defaultSettings(getSourceCount(), DEFAULT_PATTERNS_PER_CHANNEL));
		}
	}

	private CellList getWavesCells(List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output) {
		sources.setBpm(getBPM());

		BiFunction<Gene<PackedCollection<?>>, Gene<PackedCollection<?>>, IntFunction<Cell<PackedCollection<?>>>> generator = (g, p) -> channel -> {
			Producer<PackedCollection<?>> duration = g.valueAt(2).getResultant(c(getTempo().l(1)));

			Producer<PackedCollection<?>> x = p.valueAt(0).getResultant(c(1.0));
			Producer<PackedCollection<?>> y = p.valueAt(1).getResultant(c(1.0));
			Producer<PackedCollection<?>> z = p.valueAt(2).getResultant(c(1.0));

			if (sourceOverride == null) {
				return getWaves().getChoiceCell(channel,
						toScalar(g.valueAt(0).getResultant(Ops.ops().c(1.0))),
						toScalar(x), toScalar(y), toScalar(z), toScalar(g.valueAt(1).getResultant(duration)),
						enableRepeat ? toScalar(duration) : null);
			} else {
				return sourceOverride.getChoiceCell(channel, toScalar(g.valueAt(0).getResultant(Ops.ops().c(1.0))),
						v(0.0), v(0.0), v(0.0), v(0.0), null);
			}
		};

		// Generators
		CellList cells = cells(legacyGenome.valueAt(DefaultAudioGenome.GENERATORS).length(),
				i -> generator.apply(legacyGenome.valueAt(DefaultAudioGenome.GENERATORS, i),
									legacyGenome.valueAt(DefaultAudioGenome.PARAMETERS, i))
										.apply(i));

		return mixdown.cells(legacyGenome, cells, measures, output);
	}

	public static class Settings {
		private double bpm = 120;
		private int measureSize = 4;
		private int totalMeasures = 64;
		private List<Integer> breaks = new ArrayList<>();
		private List<Section> sections = new ArrayList<>();

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

		public static Settings defaultSettings(int channels, int patternsPerChannel) {
			Settings settings = new Settings();
			settings.getSections().add(new Section(0, 16));
			settings.getSections().add(new Section(16, 16));
			settings.getSections().add(new Section(32, 16));
			settings.getSections().add(new Section(48, 16));
			settings.setChordProgression(ChordProgressionManager.Settings.defaultSettings());
			settings.setPatternSystem(PatternSystemManager.Settings.defaultSettings(channels, patternsPerChannel));
			settings.setChannelNames(List.of("Kick", "Drums", "Bass", "Harmony", "Lead"));
			settings.setWetChannels(List.of(3, 4));
			return settings;
		}
	}
}
