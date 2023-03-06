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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.RoutingChoices;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.test.AdjustableDelayCellTest;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.generative.NoOpGenerationProvider;
import org.almostrealism.audio.optimize.AudioSceneGenome;
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.pattern.test.PatternFactoryTest;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.PackedCollectionHeap;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.mem.MemoryDataArgumentMap;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.audio.optimize.CellularAudioOptimizer;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class AudioSceneTest implements CellFeatures {
	public static final boolean enableDelay = true;
	public static final boolean enableFilter = true;

	public static final double delayParam = 0.35;
	public static final double delay = 60 * ((1 / (1 - Math.pow(delayParam, 3))) - 1);
	public static final double speedUpDuration1 = 10.0;
	public static final double speedUpPercentage1 = 0.0;
	public static final double slowDownDuration1 = 10.0;
	public static final double slowDownPercentage1 = 0.0;
	public static final double polySpeedUpDuration1 = 3;
	public static final double polySpeedUpExponent1 = 1.5;
	public static final double speedUpDuration2 = 10.0;
	public static final double speedUpPercentage2 = 0.0;
	public static final double slowDownDuration2 = 10.0;
	public static final double slowDownPercentage2 = 0.0;
	public static final double polySpeedUpDuration2 = 2;
	public static final double polySpeedUpExponent2 = 1.1;

	public static final double feedbackParam = 0.1;

	public static final String sampleFile1 = "Library/Snare Perc DD.wav";
	public static final String sampleFile2 = "Library/GT_HAT_31.wav";

	public AudioScene<?> pattern(int sources, int delayLayers) {
		return pattern(sources, delayLayers, false);
	}

	protected AudioScene<?> pattern(int sources, int delayLayers, boolean sections) {
		AudioScene<?> scene = new AudioScene<>(null, 120, sources, delayLayers, OutputLine.sampleRate, new NoOpGenerationProvider());
		scene.setTotalMeasures(16);
		scene.getPatternManager().getChoices().addAll(PatternFactoryTest.createChoices());
		scene.setTuning(new DefaultKeyboardTuning());

		PatternLayerManager layer = scene.getPatternManager().addPattern(0, 1.0, false);
		layer.addLayer(new ParameterSet());
		layer.addLayer(new ParameterSet());
		layer.addLayer(new ParameterSet());
		
		if (sections) {
			scene.addSection(0, 16);
		}

		return scene;
	}

	@Test
	public void pattern() {
		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(600 * OutputLine.sampleRate), PackedCollectionHeap::destroy);

		AudioScene pattern = pattern(2, 2, true);
		pattern.assignGenome(genome(pattern, false));

		OperationList setup = new OperationList();
		setup.add(pattern.getLegacyGenome().setup());
		setup.add(pattern.getTimeManager().setup());

		CellList cells = pattern.getPatternChannel(0, setup);
		cells.addSetup(() -> setup);
		cells.o(i -> new File("results/pattern-test.wav")).sec(20).get().run();
	}

	protected Genome<PackedCollection<?>> genome(AudioScene<?> scene, boolean filter) {
		Genome<PackedCollection<?>> g = AudioSceneGenome.generator(scene).get().get();
		if (g != null) return g;

		ArrayListChromosome<PackedCollection<?>> generators = new ArrayListChromosome();
		generators.add(g(0.2, 0.5,
				DefaultAudioGenome.factorForRepeat(1),
				DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));
		generators.add(g(0.5, 0.0,
				DefaultAudioGenome.factorForRepeat(1),
				DefaultAudioGenome.factorForRepeatSpeedUpDuration(180)));

		ArrayListChromosome<PackedCollection<?>> volume = new ArrayListChromosome();
		volume.add(g(0.7, 1.0));
		volume.add(g(2.5, 1.0));

		ArrayListChromosome<PackedCollection<?>> mainFilterUp = new ArrayListChromosome<>();
		IntStream.range(0, 2).mapToObj(i ->
				g(DefaultAudioGenome.factorForPeriodicAdjustmentDuration(10),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(180),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(1.0))).forEach(mainFilterUp::add);

		ArrayListChromosome<PackedCollection<?>> mainFilterDown = new ArrayListChromosome<>();
		IntStream.range(0, 2).mapToObj(i ->
				g(DefaultAudioGenome.factorForPeriodicAdjustmentDuration(360),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(360),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(1.0))).forEach(mainFilterDown::add);

		ArrayListChromosome<PackedCollection<?>> wetIn = new ArrayListChromosome<>();
		IntStream.range(0, 2).mapToObj(i ->
				g(DefaultAudioGenome.factorForPeriodicAdjustmentDuration(10),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(10),
						DefaultAudioGenome.factorForPolyAdjustmentDuration(1.0))).forEach(wetIn::add);

		ArrayListChromosome<Double> processors = new ArrayListChromosome();
		processors.add(new ArrayListGene<>(delayParam,
				DefaultAudioGenome.factorForSpeedUpDuration(speedUpDuration1),
				DefaultAudioGenome.factorForSpeedUpPercentage(speedUpPercentage1),
				DefaultAudioGenome.factorForSlowDownDuration(slowDownDuration1),
				DefaultAudioGenome.factorForSlowDownPercentage(slowDownPercentage1),
				DefaultAudioGenome.factorForPolySpeedUpDuration(polySpeedUpDuration1),
				DefaultAudioGenome.factorForPolySpeedUpExponent(polySpeedUpExponent1)));
		processors.add(new ArrayListGene<>(delayParam,
				DefaultAudioGenome.factorForSpeedUpDuration(speedUpDuration2),
				DefaultAudioGenome.factorForSpeedUpPercentage(speedUpPercentage2),
				DefaultAudioGenome.factorForSlowDownDuration(slowDownDuration2),
				DefaultAudioGenome.factorForSlowDownPercentage(slowDownPercentage2),
				DefaultAudioGenome.factorForPolySpeedUpDuration(polySpeedUpDuration2),
				DefaultAudioGenome.factorForPolySpeedUpExponent(polySpeedUpExponent2)));

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();

		if (enableDelay) {
			transmission.add(new ArrayListGene<>(0.0, feedbackParam));
			transmission.add(new ArrayListGene<>(feedbackParam, 0.0));
		} else {
			transmission.add(new ArrayListGene<>(0.0, 0.0));
			transmission.add(new ArrayListGene<>(0.0, 0.0));
		}

		ArrayListChromosome<Double> filters = new ArrayListChromosome();

		if (filter) {
			filters.add(new ArrayListGene<>(0.15, 1.0));
			filters.add(new ArrayListGene<>(0.15, 1.0));
		} else {
			filters.add(new ArrayListGene<>(0.0, 1.0));
			filters.add(new ArrayListGene<>(0.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(c(g(0.0, 0.0, 0.0), g(0.0, 0.0, 0.0)));
		genome.add(volume);
		genome.add(mainFilterUp);
		genome.add(wetIn);
		genome.add(processors);
		genome.add(transmission);
		genome.add(c(g(0.0, 0.5)));
		genome.add(filters);
		genome.add(mainFilterDown);

		return genome;
	}

	protected Cells cells(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> meter) {
		return cells(scene, measures, meter, enableFilter);
	}

	protected Cells cells(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output, boolean filter) {
		scene.assignGenome(genome(scene, filter));
		return scene.getCells(measures, output);
	}

	public Cells randomOrgan(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output) {
		AudioSceneGenome.GeneratorConfiguration conf = new AudioSceneGenome.GeneratorConfiguration();
		conf.minDelay = delay;
		conf.maxDelay = delay;
		conf.minTransmission = feedbackParam;
		conf.maxTransmission = feedbackParam;
		conf.minHighPass = 0;
		conf.maxHighPass = 0;
		conf.minLowPass = 20000;
		conf.maxLowPass = 20000;

		Genome g = AudioSceneGenome.generator(scene, conf).get().get();

		scene.assignGenome(new AudioSceneGenome(null, g));
		System.out.println("0, 0, 2 = " + scene.getLegacyGenome().valueAt(DefaultAudioGenome.GENERATORS, 0).valueAt(2).getResultant(c(1.0)).get().evaluate());
		System.out.println("0, 1, 2 = " + scene.getLegacyGenome().valueAt(DefaultAudioGenome.GENERATORS, 1).valueAt(2).getResultant(c(1.0)).get().evaluate());

		return scene.getCells(measures, output);
	}

	@Test
	public void withOutput() {
		AudioScene.enableMainFilterUp = false;
		AudioScene.enableEfxFilters = false;
		AudioScene.enableEfx = false;

		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(600 * OutputLine.sampleRate), PackedCollectionHeap::destroy);
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/genetic-factory-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), out, false);
		organ.sec(6).get().run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	public void comparison(boolean twice) {
		AudioScene<?> scene = pattern(2, 2);

		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/genetic-factory-test-a.wav")).get(0);
		Cells organ = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), out);
		organ.reset();

		Genome<PackedCollection<?>> genome = ((AudioSceneGenome) genome(scene, enableFilter)).getLegacyGenome();

		List<TemporalFactor<PackedCollection<?>>> temporals = new ArrayList<>();

		Function<Gene<PackedCollection<?>>, IntFunction<Cell<PackedCollection<?>>>> generator = g -> channel -> {
			TemporalFactor<PackedCollection<?>> tf = (TemporalFactor<PackedCollection<?>>) g.valueAt(2);
			temporals.add(tf);

			Producer<PackedCollection<?>> duration = tf.getResultant(c(scene.getTempo().l(1)));

			return scene.getWaves().getChoiceCell(channel,
					(Producer) g.valueAt(0).getResultant(Ops.ops().c(1.0)),
					c(0.0), c(0.0), c(0.0),
					(Producer) g.valueAt(1).getResultant(duration), (Producer) duration);
		};

		Supplier<Runnable> genomeSetup = genome instanceof Setup ? ((Setup) genome).setup() : () -> () -> { };

//		List<TemporalFactor<Scalar>> mainFilterUp = new ArrayList<>();

		// Generators
		CellList cells = cells(genome.valueAt(DefaultAudioGenome.GENERATORS).length(),
				i -> generator.apply(genome.valueAt(DefaultAudioGenome.GENERATORS, i)).apply(i));

//		if (enableMainFilterUp) {
//			// Apply dynamic high pass filters
//			cells = cells.map(fc(i -> {
//				TemporalFactor<Scalar> f = (TemporalFactor<Scalar>) genome.valueAt(DefaultAudioGenome.MAIN_FILTER_UP, i, 0);
//				mainFilterUp.add(f);
//				return hp(scalarsMultiply(v(20000), f.getResultant(v(1.0))), v(DefaultAudioGenome.defaultResonance));
//			})).addRequirements(mainFilterUp.toArray(TemporalFactor[]::new));
//		}

		cells = cells
				.addRequirements(temporals.toArray(TemporalFactor[]::new))
				.addSetup(() -> genomeSetup);

		cells = cells.mixdown(140);

		// Volume adjustment
		CellList branch[] = cells.branch(
				fc(i -> genome.valueAt(DefaultAudioGenome.VOLUME, i, 0)),
					fc(i -> genome.valueAt(DefaultAudioGenome.VOLUME, i, 1)
								.andThen(genome.valueAt(DefaultAudioGenome.FX_FILTERS, i, 0))));

		CellList main = branch[0];
		CellList efx = branch[1];

		// Sum the main layer
		// main = main.sum();

		// Create the delay layers
		int delayLayers = genome.valueAt(DefaultAudioGenome.PROCESSORS).length();
		TemporalFactor<Scalar> adjust[] = IntStream.range(0, delayLayers)
					.mapToObj(i -> List.of(genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1),
							genome.valueAt(DefaultAudioGenome.WET_IN, i, 0)))
					.flatMap(List::stream)
					.map(factor -> factor instanceof TemporalFactor ? ((TemporalFactor) factor) : null)
					.filter(Objects::nonNull)
					.toArray(TemporalFactor[]::new);
		CellList delays = IntStream.range(0, delayLayers)
					.mapToObj(i -> new AdjustableDelayCell(OutputLine.sampleRate,
							(Producer) genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 0).getResultant(c(1.0)),
							(Producer) genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1).getResultant(c(1.0))))
					.collect(CellList.collector());

		// Route each line to each delay layer
		efx = efx.m(fi(), delays, i -> delayGene(delayLayers, genome.valueAt(DefaultAudioGenome.WET_IN, i)))
					.addRequirements(adjust)
					// Feedback grid
					.mself(fi(), genome.valueAt(DefaultAudioGenome.TRANSMISSION),
							fc(genome.valueAt(DefaultAudioGenome.WET_OUT, 0)))
					.sum();

		CellList list = efx.o(i -> new File("results/genetic-factory-test-b" + i + ".wav"));

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		Runnable listRun = list.sec(8).get();

		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();

		if (twice) {
			organRun.run();
			((WaveOutput) out.getReceptor()).write().get().run();
		}

		listRun.run();
	}

	@Test
	public void comparisonOnce() {
		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(600 * OutputLine.sampleRate), PackedCollectionHeap::destroy);
		comparison(false);
	}

	@Test
	public void comparisonTwice() { comparison(true); }

	@Test
	public void many() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("organ-factory-many-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), out);

		Runnable run = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();

		IntStream.range(0, 10).forEach(i -> {
			run.run();
			((WaveOutput) out.getReceptor()).write().get().run();
			organ.reset();
		});
	}

	@Test
	public void random() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("factory-rand-test.wav")).get(0);
		Cells organ = randomOrgan(pattern(2, 2), null, out);  // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	private Gene<PackedCollection<?>> delayGene(int delays, Gene<PackedCollection<?>> wet) {
		ArrayListGene<PackedCollection<?>> gene = new ArrayListGene<>();
		gene.add(wet.valueAt(0));
		// gene.add(new IdentityFactor<>());
		IntStream.range(0, delays - 1).forEach(i -> gene.add(p -> c(0.0)));
		return gene;
	}
}
