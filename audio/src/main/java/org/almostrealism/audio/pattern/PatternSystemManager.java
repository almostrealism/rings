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

package org.almostrealism.audio.pattern;

import io.almostrealism.relation.DynamicProducer;
import io.almostrealism.relation.Tree;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.filter.AudioSumProvider;
import org.almostrealism.audio.notes.NoteSourceProvider;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.notes.TreeNoteSource;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.PackedCollectionMax;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO  Excluded from the genome (manually configured):
// 	     1. The number of layers
// 	     2. Melodic/Percussive flag
// 	     3. The duration of each layer

public class PatternSystemManager implements NoteSourceProvider, CodeFeatures {
	public static final boolean enableAutoVolume = true;
	public static boolean enableVerbose = false;
	public static boolean enableWarnings = true;

	private static AudioSumProvider sum = new AudioSumProvider();

	private List<PatternFactoryChoice> choices;
	private List<PatternLayerManager> patterns;
	private ConfigurableGenome genome;

	private PackedCollection<?> volume;
	private PackedCollection<?> destination;

	public PatternSystemManager() {
		this(new ArrayList<>());
	}

	public PatternSystemManager(List<PatternFactoryChoice> choices) {
		this(choices, new ConfigurableGenome());
	}

	public PatternSystemManager(ConfigurableGenome genome) {
		this(new ArrayList<>(), genome);
	}

	public PatternSystemManager(List<PatternFactoryChoice> choices, ConfigurableGenome genome) {
		this.choices = choices;
		this.patterns = new ArrayList<>();
		this.genome = genome;
	}

	public void init() {
		volume = new PackedCollection(1);
		volume.setMem(0, 1.0);
	}

	private DynamicProducer<PackedCollection<?>> destination() {
		return new DynamicProducer<>(args -> destination);
	}

	@Override
	public List<PatternNoteSource> getSource(String id) {
		return choices.stream()
				.map(PatternFactoryChoice::getFactory)
				.filter(f -> Objects.equals(id, f.getId()))
				.map(PatternElementFactory::getSources)
				.findFirst().orElse(null);
	}

	public List<PatternFactoryChoice> getChoices() {
		return choices;
	}

	public List<PatternLayerManager> getPatterns() { return patterns; }

	public void setVolume(double volume) {
		this.volume.setMem(0, volume);
	}

	public Settings getSettings() {
		Settings settings = new Settings();
		settings.getPatterns().addAll(patterns.stream().map(PatternLayerManager::getSettings).collect(Collectors.toList()));
		return settings;
	}

	public void setSettings(Settings settings) {
		patterns.clear();
		settings.getPatterns().forEach(s -> addPattern(s.getChannel(), s.getDuration(), s.isMelodic()).setSettings(s));
	}

	public void refreshParameters() {
		patterns.forEach(PatternLayerManager::refresh);
	}

	public void setTuning(KeyboardTuning tuning) {
		getChoices().forEach(c -> c.setTuning(tuning));
	}

	public void setTree(Tree<? extends Supplier<FileWaveDataProvider>> root) {
		setTree(root, null);
	}

	public void setTree(Tree<? extends Supplier<FileWaveDataProvider>> root, DoubleConsumer progress) {
		List<PatternNoteSource> sources = getChoices()
				.stream()
				.flatMap(c -> c.getFactory().getSources().stream())
				.collect(Collectors.toList());

		if (progress != null && !sources.isEmpty())
			progress.accept(0.0);

		IntStream.range(0, sources.size()).forEach(i -> {
			PatternNoteSource s = sources.get(i);

			if (s instanceof TreeNoteSource)
				((TreeNoteSource) s).setTree(root);

			if (progress != null)
				progress.accept((double) (i + 1) / sources.size());
		});
	}

	public PatternLayerManager addPattern(int channel, double measures, boolean melodic) {
		PatternLayerManager pattern = new PatternLayerManager(choices,
								genome.addSimpleChromosome(3),
								channel, measures, melodic);
		patterns.add(pattern);
		return pattern;
	}

	public void clear() {
		patterns.clear();
	}

	public Supplier<Runnable> sum(Supplier<AudioSceneContext> context) {
		OperationList op = new OperationList("PatternSystemManager Sum");

		op.add(() -> () -> this.destination = context.get().getDestination());
		op.add(() -> () ->
				IntStream.range(0, patterns.size()).forEach(i ->
						patterns.get(i).updateDestination(context.get())));

		op.add(() -> () -> {
			AudioSceneContext ctx = context.get();

			List<Integer> patternsForChannel = IntStream.range(0, patterns.size())
					.filter(i -> ctx.getChannels() == null || ctx.getChannels().contains(patterns.get(i).getChannel()))
					.boxed().collect(Collectors.toList());

			if (patternsForChannel.isEmpty()) {
				if (enableWarnings) System.out.println("PatternSystemManager: No patterns");
				return;
			}

			patternsForChannel.stream().forEach(i -> {
				patterns.get(i).sum(context);
			});

//			patternsForChannel.stream().map(i -> {
//				patterns.get(i).sum(context);
//				return p(patterns.get(i).getDestination());
//			}).forEach(note -> {
//				PackedCollection<?> audio = traverse(1, note).get().evaluate();
//				int frames = Math.min(audio.getShape().getCount(),
//						this.destination.getShape().length(0));
//
//				TraversalPolicy shape = shape(frames);
//				if (enableVerbose) log("Rendering " + frames + " frames");
//				sum.sum(this.destination.range(shape), audio.range(shape));
//				if (enableVerbose) log("Rendered " + frames + " frames");
//			});

			if (enableVerbose)
				log("Rendered patterns for channel(s) " + Arrays.toString(ctx.getChannels().toArray()));
		});

		if (enableAutoVolume) {
			CollectionProducer<PackedCollection<?>> max = new PackedCollectionMax(destination());
			CollectionProducer<PackedCollection<?>> auto = max._greaterThan(c(0.0), c(0.8).divide(max), c(1.0));
			op.add(a(1, p(volume), auto));
		}

		op.add(() -> () -> {
			sum.adjustVolume(destination, volume);
		});

		return op;
	}

	public static class Settings {
		private List<PatternLayerManager.Settings> patterns = new ArrayList<>();

		public List<PatternLayerManager.Settings> getPatterns() { return patterns; }
		public void setPatterns(List<PatternLayerManager.Settings> patterns) { this.patterns = patterns; }

		public static Settings defaultSettings(int channels, int patternsPerChannel,
											   IntUnaryOperator activePatterns,
											   IntUnaryOperator layersPerPattern,
											   IntUnaryOperator duration) {
			Settings settings = new Settings();
			IntStream.range(0, channels).forEach(c -> IntStream.range(0, patternsPerChannel).forEach(p -> {
				PatternLayerManager.Settings pattern = new PatternLayerManager.Settings();
				pattern.setChannel(c);
				pattern.setDuration(duration.applyAsInt(c));
				pattern.setMelodic(c > 1 && c != 5);
				pattern.setScaleTraversalStrategy((c == 2 || c == 4) ?
						ScaleTraversalStrategy.SEQUENCE :
						ScaleTraversalStrategy.CHORD);
				pattern.setScaleTraversalDepth(pattern.isMelodic() ? 3 : 1);
				pattern.setFactorySelection(ParameterFunction.random());
				pattern.setActiveSelection(ParameterizedPositionFunction.random());

				if (p < activePatterns.applyAsInt(c)) {
					IntStream.range(0, layersPerPattern.applyAsInt(c)).forEach(l -> {
						pattern.getLayers().add(ParameterSet.random());
					});
				}

				settings.getPatterns().add(pattern);
			}));
			return settings;
		}
	}
}
