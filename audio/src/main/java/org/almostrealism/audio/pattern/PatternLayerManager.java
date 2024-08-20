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

import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.arrange.AutomationManager;
import org.almostrealism.audio.arrange.ChannelSection;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioContext;
import org.almostrealism.audio.notes.PatternNoteAudio;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.mem.Heap;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.io.DistributionMetric;
import org.almostrealism.io.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PatternLayerManager implements CodeFeatures {
	public static boolean enableWarnings = SystemUtils.isEnabled("AR_PATTERN_WARNINGS").orElse(true);
	public static boolean enableLogging = SystemUtils.isEnabled("AR_PATTERN_LOGGING").orElse(false);

	public static DistributionMetric sizes = AudioScene.console.distribution("patternSizes");

	private int channel;
	private double duration;
	private double scale;

	private boolean melodic;
	private int scaleTraversalDepth;
	private double minLayerScale;
	private ScaleTraversalStrategy scaleTraversalStrategy;

	private Supplier<List<NoteAudioChoice>> percChoices;
	private Supplier<List<NoteAudioChoice>> melodicChoices;
	private SimpleChromosome layerChoiceChromosome;
	private SimpleChromosome envelopeAutomationChromosome;

	private ParameterFunction factorySelection; // TODO  rename
	private ParameterizedPositionFunction activeSelection;
	private PatternElementFactory elementFactory;

	private List<PatternLayer> roots;
	private List<ParameterSet> layerParams;

	private PackedCollection<?> destination;

	public PatternLayerManager(List<NoteAudioChoice> choices,
							   SimpleChromosome layerChoiceChromosome,
							   SimpleChromosome envelopeAutomationChromosome,
							   int channel, double measures, boolean melodic) {
		this(NoteAudioChoice.choices(choices, false), NoteAudioChoice.choices(choices, true),
				layerChoiceChromosome, envelopeAutomationChromosome, channel, measures, melodic);
	}

	public PatternLayerManager(Supplier<List<NoteAudioChoice>> percChoices,
							   Supplier<List<NoteAudioChoice>> melodicChoices,
							   SimpleChromosome layerChoiceChromosome,
							   SimpleChromosome envelopeAutomationChromosome,
							   int channel, double measures, boolean melodic) {
		this.channel = channel;
		this.duration = measures;
		this.scale = 1.0;
		this.scaleTraversalStrategy = ScaleTraversalStrategy.CHORD;
		this.scaleTraversalDepth = 1;
		this.minLayerScale = 0.0625;
		setMelodic(melodic);

		this.percChoices = percChoices;
		this.melodicChoices = melodicChoices;
		this.layerChoiceChromosome = layerChoiceChromosome;
		this.envelopeAutomationChromosome = envelopeAutomationChromosome;
		this.roots = new ArrayList<>();
		this.layerParams = new ArrayList<>();
		init();
	}

	public void init() {
		factorySelection = ParameterFunction.random();
		activeSelection = ParameterizedPositionFunction.random();
		elementFactory = new PatternElementFactory();
	}

	public PackedCollection<?> getDestination() { return destination; }

	public void updateDestination(AudioSceneContext context) {
		if (context.getChannels().contains(channel)) {
			destination = context.getDestination();
		}
	}

	public List<NoteAudioChoice> getChoices() {
		return melodic ? melodicChoices.get() : percChoices.get();
	}

	public Stream<NoteAudioChoice> choices() {
		return getChoices().stream()
				.filter(c -> c.getChannels() == null || c.getChannels().contains(channel))
				.filter(c -> scaleTraversalDepth <= c.getMaxScaleTraversalDepth());
	}

	public int getChannel() { return channel; }
	public void setChannel(int channel) { this.channel = channel; }

	public void setDuration(double measures) { duration = measures; }
	public double getDuration() { return duration; }

	public int getScaleTraversalDepth() { return scaleTraversalDepth; }
	public void setScaleTraversalDepth(int scaleTraversalDepth) { this.scaleTraversalDepth = scaleTraversalDepth; }

	public double getMinLayerScale() { return minLayerScale; }
	public void setMinLayerScale(double minLayerScale) { this.minLayerScale = minLayerScale; }

	public void setMelodic(boolean melodic) {
		this.melodic = melodic;
	}

	public boolean isMelodic() { return melodic; }

	public ScaleTraversalStrategy getScaleTraversalStrategy() {
		return scaleTraversalStrategy;
	}

	public void setScaleTraversalStrategy(ScaleTraversalStrategy scaleTraversalStrategy) {
		this.scaleTraversalStrategy = scaleTraversalStrategy;
	}

	public PatternElementFactory getElementFactory() {
		return elementFactory;
	}

	public void setElementFactory(PatternElementFactory elementFactory) {
		this.elementFactory = elementFactory;
	}

	public PatternLayerSeeds getSeeds(ParameterSet params) {
		List<PatternLayerSeeds> options = choices()
				.filter(NoteAudioChoice::isSeed)
				.map(choice -> choice.seeds(params))
				.collect(Collectors.toList());

		if (options.isEmpty()) return null;

		double c = factorySelection.apply(params);
		if (c < 0) c = c + 1.0;
		return options.get((int) (options.size() * c));
	}

	public List<PatternElement> getTailElements() {
		return roots.stream()
				.map(PatternLayer::getTail)
				.map(PatternLayer::getElements)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public Map<NoteAudioChoice, List<PatternElement>> getAllElementsByChoice(double start, double end) {
		Map<NoteAudioChoice, List<PatternElement>> result = new HashMap<>();
		roots.forEach(l -> l.putAllElementsByChoice(result, start, end));
		return result;
	}

	public List<PatternElement> getAllElements(double start, double end) {
		return roots.stream()
				.map(l -> l.getAllElements(start, end))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public Settings getSettings() {
		Settings settings = new Settings();
		settings.setChannel(channel);
		settings.setDuration(duration);
		settings.setScaleTraversalStrategy(scaleTraversalStrategy);
		settings.setScaleTraversalDepth(scaleTraversalDepth);
		settings.setMinLayerScale(minLayerScale);
		settings.setMelodic(melodic);
		settings.setFactorySelection(factorySelection);
		settings.setActiveSelection(activeSelection);
		settings.setElementFactory(elementFactory);
		settings.getLayers().addAll(layerParams);
		return settings;
	}

	public void setSettings(Settings settings) {
		channel = settings.getChannel();
		duration = settings.getDuration();
		scaleTraversalStrategy = settings.getScaleTraversalStrategy();
		scaleTraversalDepth = settings.getScaleTraversalDepth();
		minLayerScale = settings.getMinLayerScale();
		melodic = settings.isMelodic();

		if (settings.getFactorySelection() != null)
			factorySelection = settings.getFactorySelection();

		if (settings.getActiveSelection() != null)
			activeSelection = settings.getActiveSelection();

		if (settings.getElementFactory() != null)
			elementFactory = settings.getElementFactory();

		clear(true);
		settings.getLayers().forEach(this::addLayer);
	}

	protected void decrement() { scale *= 2; }
	protected void increment() {
		scale /= 2;
	}

	public int rootCount() { return roots.size(); }

	public int depth() {
		if (rootCount() <= 0) return 0;
		return roots.stream()
				.map(PatternLayer::depth)
				.max(Integer::compareTo).orElse(0);
	}

	public int getLayerCount() {
		return layerChoiceChromosome.length();
	}

	public void setLayerCount(int count) {
		if (count < 0) throw new IllegalArgumentException(count + " is not a valid number of layers");
		if (count == getLayerCount()) return;

		if (getLayerCount() < count) {
			while (getLayerCount() < count) addLayer(new ParameterSet());
		} else {
			while (getLayerCount() > count) removeLayer(true);
		}
	}

	public void addLayer(ParameterSet params) {
		envelopeAutomationChromosome.addGene();

		SimpleGene g = layerChoiceChromosome.addGene();
		g.set(0, params.getX());
		g.set(1, params.getY());
		g.set(2, params.getZ());
		layer(params);
	}

	public void layer(Gene<PackedCollection<?>> gene) {
		ParameterSet params = new ParameterSet();
		params.setX(gene.valueAt(0).getResultant(c(1.0)).get().evaluate().toDouble(0));
		params.setY(gene.valueAt(1).getResultant(c(1.0)).get().evaluate().toDouble(0));
		params.setZ(gene.valueAt(2).getResultant(c(1.0)).get().evaluate().toDouble(0));
		layer(params);
	}

	protected void layer(ParameterSet params) {
		Gene<PackedCollection<?>> automationGene = envelopeAutomationChromosome.valueAt(depth());
		PackedCollection<?> automationParams =
				PackedCollection.factory().apply(AutomationManager.GENE_LENGTH)
								.fill(pos -> automationGene.valueAt(pos[0]).getResultant(c(1.0)).evaluate().toDouble());

		if (rootCount() <= 0) {
			PatternLayerSeeds seeds = getSeeds(params);

			if (seeds != null) {
				seeds.generator(getElementFactory(), 0, duration,
							scaleTraversalStrategy, scaleTraversalDepth, minLayerScale)
						.forEach(roots::add);

				scale = seeds.getScale(duration, minLayerScale);
			}

			if (rootCount() <= 0) {
				roots.add(new PatternLayer());
			}

			roots.forEach(layer -> layer.setAutomationParameters(automationParams));
		} else {
			if (enableLogging) {
				System.out.println();
				log(roots.size() +
						" roots (scale = " + scale + ", duration = " + duration + ")");
			}

			roots.forEach(layer -> {
				NoteAudioChoice choice = scale >= minLayerScale ? choose(scale, params) : null;
				PatternLayer next;

				if (choice != null) {
					next = choice.apply(getElementFactory(), layer.getAllElements(0, 2 * duration), scale,
								scaleTraversalStrategy, scaleTraversalDepth, params);
					next.trim(2 * duration);
				} else {
					next = new PatternLayer();
				}

				next.setAutomationParameters(automationParams);

				if (enableLogging) {
					log(layer.getAllElements(0, duration).size() +
										" elements --> " + next.getElements().size() + " elements");
				}

				layer.getTail().setChild(next);
			});
		}

		layerParams.add(params);
		increment();
	}

	public void removeLayer(boolean removeGene) {
		if (removeGene) {
			envelopeAutomationChromosome.removeGene(envelopeAutomationChromosome.length() - 1);
			layerChoiceChromosome.removeGene(layerChoiceChromosome.length() - 1);
		}

		layerParams.remove(layerParams.size() - 1);
		decrement();

		if (depth() <= 0) return;
		if (depth() <= 1) {
			roots.clear();
			return;
		}

		roots.forEach(layer -> layer.getLastParent().setChild(null));
	}

	public void clear(boolean removeGenes) {
		if (removeGenes) {
			while (getLayerCount() > 0) removeLayer(true);
		} else {
			while (depth() > 0) removeLayer(false);
		}
	}

	public void refresh() {
		clear(false);
		if (layerParams.size() != depth())
			throw new IllegalStateException("Layer count mismatch (" + layerParams.size() + " != " + layerChoiceChromosome.length() + ")");

		IntStream.range(0, layerChoiceChromosome.length()).forEach(i -> layer(layerChoiceChromosome.valueAt(i)));
	}

	public NoteAudioChoice choose(double scale, ParameterSet params) {
		List<NoteAudioChoice> options = choices()
				.filter(c -> scale >= c.getMinScale())
				.filter(c -> scale <= c.getMaxScale())
				.collect(Collectors.toList());

		if (options.isEmpty()) return null;

		double c = factorySelection.apply(params);
		if (c < 0) c = c + 1.0;
		return options.get((int) (options.size() * c));
	}

	public void sum(Supplier<AudioSceneContext> context) {
		Map<NoteAudioChoice, List<PatternElement>> elements = getAllElementsByChoice(0.0, duration);
		if (elements.isEmpty()) {
			if (!roots.isEmpty() && enableWarnings)
				warn("No pattern elements (channel " + channel + ")");
			return;
		}

		AudioSceneContext ctx = context.get();

		// TODO  What about when duration is longer than measures?
		// TODO  This results in count being 0, and nothing being output
		int count = (int) (ctx.getMeasures() / duration);
		if (ctx.getMeasures() / duration - count > 0.0001) {
			warn("Pattern duration does not divide measures; there will be gaps");
		}

		IntStream.range(0, count).forEach(i -> {
			ChannelSection section = ctx.getSection(i * duration);

			if (section == null) {
				warn("No ChannelSection at measure " + i);
			} else {
				double active = activeSelection.apply(layerParams.get(layerParams.size() - 1), section.getPosition()) + ctx.getActivityBias();
				if (active < 0) return;
			}

			double offset = i * duration;
			elements.keySet().forEach(choice -> {
				NoteAudioContext audioContext =
						new NoteAudioContext(
							choice.getValidNotes(),
							this::nextNotePosition);

				elements.get(choice).stream()
						.map(e -> e.getNoteDestinations(melodic, offset, ctx, audioContext))
						.flatMap(List::stream)
						.forEach(note -> {
							if (note.getOffset() >= destination.getShape().length(0)) return;

							Function<PackedCollection<?>, PackedCollection<?>> process = audio -> {
								int frames = Math.min(audio.getShape().getCount(),
										destination.getShape().length(0) - note.getOffset());
								sizes.addEntry(frames);

								TraversalPolicy shape = shape(frames);
								return PatternNoteAudio.sum.sum(destination.range(shape, note.getOffset()), audio.range(shape));
							};

							Heap.stage(() ->
									process.apply(traverse(1, note.getProducer()).get().evaluate()));
						});
			});
		});
	}

	public double nextNotePosition(double position) {
		return getAllElements(position, duration).stream()
				.map(PatternElement::getPositions)
				.flatMap(List::stream)
				.filter(p -> p > position)
				.mapToDouble(p -> p)
				.min().orElse(duration);
	}

	public static String layerHeader() {
		int count = 128;
		int divide = count / 4;

		StringBuffer buf = new StringBuffer();

		i: for (int i = 0; i < count; i++) {
			if (i % (divide / 2) == 0) {
				if (i % divide == 0) {
					buf.append("|");
				} else {
					buf.append(" ");
				}
			}

			buf.append(" ");
		}

		buf.append("|");
		return buf.toString();
	}

	public static String layerString(PatternLayer layer) {
		return layerString(layer.getElements());
	}

	public static String layerString(List<PatternElement> elements) {
		int count = 128;
		int divide = count / 8;
		double scale = 1.0 / count;

		StringBuffer buf = new StringBuffer();

		i: for (int i = 0; i < count; i++) {
			if (i % divide == 0) buf.append("|");
			for (PatternElement e : elements) {
				if (e.isPresent(i * scale, (i + 1) * scale)) {
					String s = e.getNote().toString();
					if (s.contains("/")) s = s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf("/") + 2);
					buf.append(s);
					continue i;
				}
			}
			buf.append(" ");
		}

		buf.append("|");
		return buf.toString();
	}

	public static class Settings {
		private int channel;
		private double duration;
		private ScaleTraversalStrategy scaleTraversalStrategy;
		private int scaleTraversalDepth;
		private double minLayerScale;
		private boolean melodic;

		private ParameterFunction factorySelection;
		private ParameterizedPositionFunction activeSelection;
		private PatternElementFactory elementFactory;

		private List<ParameterSet> layers = new ArrayList<>();

		public int getChannel() { return channel; }
		public void setChannel(int channel) { this.channel = channel; }

		public double getDuration() { return duration; }
		public void setDuration(double duration) { this.duration = duration; }

		public ScaleTraversalStrategy getScaleTraversalStrategy() { return scaleTraversalStrategy; }
		public void setScaleTraversalStrategy(ScaleTraversalStrategy scaleTraversalStrategy) { this.scaleTraversalStrategy = scaleTraversalStrategy; }

		public int getScaleTraversalDepth() { return scaleTraversalDepth; }
		public void setScaleTraversalDepth(int scaleTraversalDepth) { this.scaleTraversalDepth = scaleTraversalDepth; }

		public double getMinLayerScale() { return minLayerScale; }
		public void setMinLayerScale(double minLayerScale) { this.minLayerScale = minLayerScale; }

		public boolean isMelodic() { return melodic; }
		public void setMelodic(boolean melodic) { this.melodic = melodic; }

		public ParameterFunction getFactorySelection() { return factorySelection; }
		public void setFactorySelection(ParameterFunction factorySelection) { this.factorySelection = factorySelection; }

		public ParameterizedPositionFunction getActiveSelection() { return activeSelection; }
		public void setActiveSelection(ParameterizedPositionFunction activeSelection) { this.activeSelection = activeSelection; }

		public PatternElementFactory getElementFactory() { return elementFactory; }
		public void setElementFactory(PatternElementFactory elementFactory) { this.elementFactory = elementFactory; }

		public List<ParameterSet> getLayers() { return layers; }
		public void setLayers(List<ParameterSet> layers) { this.layers = layers; }
	}
}
