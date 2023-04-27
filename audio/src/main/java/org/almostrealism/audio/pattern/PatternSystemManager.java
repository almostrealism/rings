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

package org.almostrealism.audio.pattern;

import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteSourceProvider;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.Scale;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.ProducerWithOffset;
import org.almostrealism.collect.computations.RootDelegateSegmentsAdd;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.PassThroughProducer;
import org.almostrealism.heredity.ConfigurableGenome;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO  Excluded from the genome (manually configured):
// 	     1. The number of layers
// 	     2. Melodic/Percussive flag
// 	     3. The duration of each layer

public class PatternSystemManager implements NoteSourceProvider, CodeFeatures {
	public static boolean enableWarnings = true;

	private List<PatternFactoryChoice> choices;
	private List<PatternLayerManager> patterns;
	private ConfigurableGenome genome;

	private PackedCollection<?> volume;
	private PackedCollection<?> destination;
	private RootDelegateSegmentsAdd<PackedCollection> sum;
	private Runnable runSum;

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

	private void updateDestination(PackedCollection<?> destination, Supplier<PackedCollection> intermediateDestination) {
		this.destination = destination;
		this.sum = new RootDelegateSegmentsAdd<>(8, destination.traverse(1));
		IntStream.range(0, patterns.size()).forEach(i -> patterns.get(i).updateDestination(intermediateDestination.get()));

		KernelizedEvaluable<PackedCollection<?>> scale = multiply(
				new PassThroughProducer<>(1, 0), new PassThroughProducer<>(1, 1, -1)).get();

		OperationList generate = new OperationList("PatternSystemManager Sum");
		generate.add(() -> sum.get());
		generate.add(() -> () ->
				scale.into(this.destination.traverse(1)).evaluate(this.destination.traverse(1), volume));
		runSum = generate.get();
	}

	@Override
	public List<PatternNoteSource>getSource(String id) {
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

	public void sum(List<Integer> channels, DoubleToIntFunction offsetForPosition,
					int measures, DoubleFunction<Scale<?>> scaleForPosition,
					PackedCollection destination, Supplier<PackedCollection> intermediateDestination) {
		if (this.destination != destination) {
			updateDestination(destination, intermediateDestination);
		}

		List<Integer> patternsForChannel = IntStream.range(0, patterns.size())
				.filter(i -> channels == null || channels.contains(patterns.get(i).getChannel()))
				.boxed().collect(Collectors.toList());

		if (patternsForChannel.isEmpty()) {
			if (enableWarnings) System.out.println("PatternSystemManager: No patterns");
			return;
		}

		sum.getInput().clear();
		patternsForChannel.forEach(i -> {
			patterns.get(i).sum(offsetForPosition, measures, scaleForPosition);
			sum.getInput().add(new ProducerWithOffset<>(v(patterns.get(i).getDestination()), 0));
		});

		if (sum.getInput().size() > sum.getMaxInputs()) {
			System.out.println("PatternSystemManager: Too many patterns (" + sum.getInput().size() + ") for sum");
			return;
		}

		runSum.run();
	}

	public static class Settings {
		private List<PatternLayerManager.Settings> patterns = new ArrayList<>();

		public List<PatternLayerManager.Settings> getPatterns() { return patterns; }
		public void setPatterns(List<PatternLayerManager.Settings> patterns) { this.patterns = patterns; }

		public static Settings defaultSettings(int channels, int patternsPerChannel) {
			Settings settings = new Settings();
			IntStream.range(0, channels).forEach(c -> IntStream.range(0, patternsPerChannel).forEach(p -> {
				PatternLayerManager.Settings pattern = new PatternLayerManager.Settings();
				pattern.setChannel(c);
				pattern.setDuration(c == 0 ? 1 : Math.pow(2.0, c - 1));
				pattern.setChordDepth(c == 3 ? 3 : 1);
				pattern.setMelodic(c > 2);
				pattern.setFactorySelection(ParameterFunction.random());
				if (p == 0 || (c < 3 && p < 4)) {
					pattern.getLayers().add(ParameterSet.random());
					pattern.getLayers().add(ParameterSet.random());
					pattern.getLayers().add(ParameterSet.random());
				}
				settings.getPatterns().add(pattern);
			}));
			return settings;
		}
	}
}
