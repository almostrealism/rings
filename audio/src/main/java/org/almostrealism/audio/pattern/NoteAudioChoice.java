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

package org.almostrealism.audio.pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NoteAudioChoice implements ConsoleFeatures {
	public static int[] GRANULARITY_DIST;

	@Deprecated
	private PatternElementFactory factory;

	private String id;
	private String name;
	private List<NoteAudioSource> sources;
	private Boolean melodic;

	private double weight;
	private double minScale;
	private double maxScale;
	private int maxScaleTraversalDepth;
	private List<Integer> channels;

	private boolean seed;
	private double bias;

	private ParameterFunction granularitySelection;

	public NoteAudioChoice() { this(null); }

	public NoteAudioChoice(PatternElementFactory factory) {
		this(factory, 1.0);
	}

	public NoteAudioChoice(PatternElementFactory factory, double weight) {
		this(factory, weight, 0.0625, 16.0);
	}

	public NoteAudioChoice(PatternElementFactory factory, double weight, double minScale, double maxScale) {
		setFactory(factory);
		setWeight(weight);
		setMinScale(minScale);
		setMaxScale(maxScale);
		setMaxScaleTraversalDepth(1);
		setSeed(true);
		setBias(-0.5);
		setChannels(new ArrayList<>());
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		granularitySelection = ParameterFunction.random();
	}

	public PatternElementFactory getFactory() { return factory; }

	public void setFactory(PatternElementFactory factory) { this.factory = factory; }

	public void setTuning(KeyboardTuning tuning) {
		getSources().forEach(n -> n.setTuning(tuning));
	}

	public String getId() {
		if (id != null) return id;
		return factory.getId();
	}

	public void setId(String id) {
		this.id = id;
		this.factory.setId(id);
	}

	public String getName() {
		if (name != null) return name;
		return factory.getName();
	}

	public void setName(String name) {
		this.name = name;
		this.factory.setName(name);
	}

	public List<NoteAudioSource> getSources() {
		if (sources != null) return sources;
		return factory.getSources();
	}

	public void setSources(List<NoteAudioSource> sources) {
		this.sources = sources;
		this.factory.setSources(sources);
	}

	public boolean isMelodic() {
		if (melodic != null) return melodic;
		return factory.isMelodic();
	}

	public void setMelodic(boolean melodic) {
		this.melodic = melodic;
		this.factory.setMelodic(melodic);
	}

	// TODO Use this value to determine the likelihood of selection
	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public double getMinScale() { return minScale; }
	public void setMinScale(double minScale) { this.minScale = minScale; }

	public double getMaxScale() { return maxScale; }
	public void setMaxScale(double maxScale) { this.maxScale = maxScale; }

	public int getMaxScaleTraversalDepth() { return maxScaleTraversalDepth; }
	public void setMaxScaleTraversalDepth(int maxScaleTraversalDepth) { this.maxScaleTraversalDepth = maxScaleTraversalDepth; }

	public List<Integer> getChannels() { return channels; }
	public void setChannels(List<Integer> channels) { this.channels = channels; }

	public boolean isSeed() { return seed; }
	public void setSeed(boolean seed) { this.seed = seed; }

	public ParameterFunction getGranularitySelection() { return granularitySelection; }
	public void setGranularitySelection(ParameterFunction granularitySelection) {
		this.granularitySelection = granularitySelection;
	}

	public double getBias() { return bias; }
	public void setBias(double bias) {
		this.bias = bias;
	}

	public boolean checkResourceUsed(String canonicalPath) {
		return getSources().stream().anyMatch(s -> s.checkResourceUsed(canonicalPath));
	}

	@JsonIgnore
	public List<NoteAudioProvider> getAllNotes() {
		return getSources().stream()
				.map(NoteAudioSource::getNotes)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@JsonIgnore
	public List<NoteAudioProvider> getValidNotes() {
		return getAllNotes().stream().filter(NoteAudioProvider::isValid).collect(Collectors.toList());
	}

	public PatternLayerSeeds seeds(ParameterSet params) {
		double granularity = granularitySelection.power(2, 3, -3).apply(params);

		if (GRANULARITY_DIST != null) {
			int i;
			double g = granularity;
			for (i = 0; g < 1.0; i++) {
				g *= 2;
			}

			GRANULARITY_DIST[i]++;
		}

		return new PatternLayerSeeds(0, granularity, getMinScale(), getMaxScale(), bias, this, params);
	}

	public PatternLayer apply(PatternElementFactory factory, List<PatternElement> elements, double scale, ScaleTraversalStrategy scaleTraversalStrategy, int depth, ParameterSet params) {
		PatternLayer layer = new PatternLayer();
		layer.setChoice(this);

		elements.forEach(e -> layer.getElements().addAll(apply(factory, e, scale, scaleTraversalStrategy, depth, params).getElements()));
		return layer;
	}

	public PatternLayer apply(PatternElementFactory factory, PatternElement element, double scale, ScaleTraversalStrategy scaleTraversalStrategy, int depth, ParameterSet params) {
		PatternLayer layer = new PatternLayer();
		layer.setChoice(this);

		factory.apply(ElementParity.LEFT, element.getPosition(), scale, getBias(),
					scaleTraversalStrategy, depth, true, params)
				.ifPresent(layer.getElements()::add);

		factory.apply(ElementParity.RIGHT, element.getPosition(), scale, getBias(),
					scaleTraversalStrategy, depth, true, params)
				.ifPresent(layer.getElements()::add);

		return layer;
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}

	public static NoteAudioChoice fromSource(String name, NoteAudioSource source,
											 int channel, int maxScaleTraversalDepth,
											 boolean melodic) {
		NoteAudioChoice c = new NoteAudioChoice();
		c.setName(name);
		c.setSources(new ArrayList<>());
		c.getSources().add(source);
		c.setMelodic(melodic);
		c.setMaxScaleTraversalDepth(maxScaleTraversalDepth);
		c.getChannels().add(channel);
		return c;
	}

	public static Supplier<List<NoteAudioChoice>> choices(List<NoteAudioChoice> choices, boolean melodic) {
		return () -> choices.stream()
				.filter(c -> c.isMelodic() || !melodic)
				.collect(Collectors.toList());
	}
}
