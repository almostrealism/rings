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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;

public class DurationAdjustmentTest implements CellFeatures, TestFeatures {
	@Test
	public void dynamicRepeat() {
		int sr = OutputLine.sampleRate;

		TimeCell clock = new TimeCell();

		TraversalPolicy adjustmentShape = shape(10 * sr, 1).traverse(1);

		PackedCollection<?> adjustmentData = c(value(adjustmentShape, 0))
				._greaterThan(c(5.0), c(1.0), c(0.5))
				.get().evaluate(WaveOutput.timeline.getValue().range(adjustmentShape).traverse(1));

		WaveCell adjustment = new WaveCell(adjustmentData, sr, clock.frame());
		Factor<PackedCollection<?>> factor = adjustment.toFactor();

		int count = 32;

		CellList cells = w(c(0.0), factor.getResultant(c(1.0)),
				"Library/Snare Perc DD.wav")
				.addRequirements(clock, (TemporalFactor) factor)
				.o(i -> new File("results/dynamic-repeat.wav"));

		cells.sec(bpm(120).l(count)).get().run();
	}
}
