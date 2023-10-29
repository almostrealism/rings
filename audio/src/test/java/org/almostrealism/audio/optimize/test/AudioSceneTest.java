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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.generative.NoOpGenerationProvider;
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.pattern.test.PatternFactoryTest;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Gene;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.ArrayListGene;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
		AudioScene pattern = pattern(2, 2, true);
		pattern.assignGenome(pattern.getGenome().random());

		OperationList setup = new OperationList();
		setup.add(pattern.getTimeManager().setup());

		CellList cells = pattern.getPatternChannel(0, setup);
		cells.addSetup(() -> setup);
		cells.o(i -> new File("results/pattern-test.wav")).sec(20).get().run();
	}

	protected Cells cells(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> meter) {
		return cells(scene, measures, meter, enableFilter);
	}

	protected Cells cells(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output, boolean filter) {
		scene.assignGenome(scene.getGenome().random());
		return scene.getCells(measures, null, output);
	}

	public Cells randomOrgan(AudioScene<?> scene, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output) {
		scene.assignGenome(scene.getGenome().random());
		return scene.getCells(measures, null, output);
	}

	@Test
	public void withOutput() {
		AudioScene.enableMainFilterUp = false;
		AudioScene.enableEfxFilters = false;
		AudioScene.enableEfx = false;

		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/genetic-factory-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), out, false);
		organ.sec(6).get().run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

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
