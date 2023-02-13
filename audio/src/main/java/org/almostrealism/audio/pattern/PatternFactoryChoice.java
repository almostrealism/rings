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

import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.tone.KeyboardTuning;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PatternFactoryChoice {
	private PatternElementFactory factory;
	private double weight;
	private double minScale;
	private double maxScale;
	private int maxChordDepth;

	private boolean seed;
	private int seedUnits;
	private double seedScale;
	private double seedBias;

	@Deprecated
	private ParameterizedPositionFunction seedNoteFunction;

	public PatternFactoryChoice() { this(null); }

	public PatternFactoryChoice(PatternElementFactory factory) {
		this(factory, 1.0);
	}

	public PatternFactoryChoice(PatternElementFactory factory, double weight) {
		this(factory, weight, 0.0, 1.0);
	}

	public PatternFactoryChoice(PatternElementFactory factory, double weight, double minScale, double maxScale) {
		setFactory(factory);
		setWeight(weight);
		setMinScale(minScale);
		setMaxScale(maxScale);
		setMaxChordDepth(1);
		setSeedUnits(4);
		setSeedScale(0.25);
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		seedNoteFunction = ParameterizedPositionFunction.random();
	}

	public PatternElementFactory getFactory() { return factory; }

	public void setFactory(PatternElementFactory factory) { this.factory = factory; }

	// TODO Use this value to determine the likelihood of selection
	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public double getMinScale() { return minScale; }
	public void setMinScale(double minScale) { this.minScale = minScale; }

	public double getMaxScale() { return maxScale; }
	public void setMaxScale(double maxScale) { this.maxScale = maxScale; }

	public int getMaxChordDepth() { return maxChordDepth; }
	public void setMaxChordDepth(int maxChordDepth) { this.maxChordDepth = maxChordDepth; }

	public boolean isSeed() { return seed; }
	public void setSeed(boolean seed) { this.seed = seed; }

	public int getSeedUnits() { return seedUnits; }
	public void setSeedUnits(int seedUnits) { this.seedUnits = seedUnits; }

	public double getSeedScale() { return seedScale; }
	public void setSeedScale(double seedScale) { this.seedScale = seedScale; }

	public double getSeedBias() { return seedBias; }
	public void setSeedBias(double seedBias) { this.seedBias = seedBias; }

	public ParameterizedPositionFunction getSeedNoteFunction() {
		return seedNoteFunction;
	}
	public void setSeedNoteFunction(ParameterizedPositionFunction seedNoteFunction) {
		this.seedNoteFunction = seedNoteFunction;
	}

	public void setTuning(KeyboardTuning tuning) {
		getFactory().setTuning(tuning);
	}

	public PatternLayerSeeds seeds(ParameterSet params) {
		return new PatternLayerSeeds(0, seedScale, 1.0 / seedUnits, seedBias, factory, params);
	}

	public PatternLayer apply(List<PatternElement> elements, double scale, int depth, ParameterSet params) {
		PatternLayer layer = new PatternLayer();
		layer.setChoice(this);
		elements.forEach(e -> layer.getElements().addAll(apply(e, scale, depth, params).getElements()));
		return layer;
	}

	public PatternLayer apply(PatternElement element, double scale, int depth, ParameterSet params) {
		PatternLayer layer = new PatternLayer();
		layer.setChoice(this);

		getFactory().apply(ElementParity.LEFT, element.getPosition(), scale, 0.0, depth, true, params).ifPresent(layer.getElements()::add);
		getFactory().apply(ElementParity.RIGHT, element.getPosition(), scale, 0.0, depth, true, params).ifPresent(layer.getElements()::add);
		return layer;
	}

	public static Supplier<List<PatternFactoryChoice>> choices(List<PatternFactoryChoice> choices, boolean melodic) {
		return () -> choices.stream()
				.filter(c -> c.getFactory().isMelodic() == melodic)
				.collect(Collectors.toList());
	}
}
