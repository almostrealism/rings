/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.audio.test;

import com.almostrealism.tone.DefaultKeyboardTuning;
import com.almostrealism.tone.WesternChromatic;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.PolymorphicAudioCell;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.computations.DefaultEnvelopeComputation;
import org.almostrealism.audio.filter.BasicDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.IdentityFactor;
import org.almostrealism.graph.CellPair;
import org.almostrealism.graph.MultiCell;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class PolymorphicAudioCellTest implements TestFeatures {
	private static final int DURATION_FRAMES = 10 * OutputLine.sampleRate;

	protected Receptor<Scalar> loggingReceptor() {
		return protein -> () -> () -> System.out.println(protein.get().evaluate());
	}

	protected Cell<Scalar> loggingCell() { return new ReceptorCell<>(loggingReceptor()); }

	protected PolymorphicAudioCell cell() {
		SineWaveCell cell = new SineWaveCell();
		cell.setFreq(new DefaultKeyboardTuning().getTone(WesternChromatic.G3).asHertz());
		cell.setNoteLength(600);
		cell.setAmplitude(0.1);
		cell.setEnvelope(DefaultEnvelopeComputation::new);

		return new PolymorphicAudioCell(v(0.5), Collections.singletonList(data -> cell));
	}

	@Test
	public void sineWave() {
		PolymorphicAudioCell cell = cell();
		cell.setReceptor(loggingReceptor());
		Runnable push = cell.push(v(0.0)).get();
		IntStream.range(0, 100).forEach(i -> push.run());
		// TODO  Add assertions
	}

	@Test
	public void withOutput() {
		WaveOutput output = new WaveOutput(new File("health/polymorphic-cell-test.wav"));

		PolymorphicAudioCell cell = cell();
		cell.setReceptor(output);

		Runnable push = cell.push(v(0.0)).get();
		Runnable tick = cell.tick().get();
		IntStream.range(0, DURATION_FRAMES).forEach(i -> {
			push.run();
			tick.run();
			if ((i + 1) % 1000 == 0) System.out.println("PolymorphicAudioCellTest: " + (i + 1) + " iterations");
		});

		System.out.println("PolymorphicAudioCellTest: Writing WAV...");
		output.write().get().run();
		System.out.println("PolymorphicAudioCellTest: Done");
	}

	protected Gene<Scalar> identityGene() {
		return new Gene<>() {
			@Override public Factor<Scalar> valueAt(int index) { return new IdentityFactor<>(); }
			@Override public int length() { return 1; }
		};
	}

	protected void loggingCellPair(Cell<Scalar> input) {
		List<Cell<Scalar>> cells = new ArrayList<>();
		cells.add(loggingCell());

		MultiCell<Scalar> m = new MultiCell<>(cells, identityGene());
		m.setName("LoggingMultiCell");
		new CellPair<>(input, m, null, new IdentityFactor<>()).init();
	}

	@Test
	public void withCellPair() {
		PolymorphicAudioCell cell = cell();
		loggingCellPair(cell);

		Runnable push = cell.push(null).get();
		IntStream.range(0, 100).forEach(i -> push.run());
	}

	@Test
	public void withBasicDelayCell() {
		BasicDelayCell delay = new BasicDelayCell(1);
		delay.setReceptor(loggingReceptor());

		PolymorphicAudioCell cell = cell();
		cell.setReceptor(delay);

		Runnable push = cell.push(null).get();
		Runnable tick = delay.tick().get();

		IntStream.range(0, 200).forEach(i -> {
			push.run();
			tick.run();
		});
	}
}
