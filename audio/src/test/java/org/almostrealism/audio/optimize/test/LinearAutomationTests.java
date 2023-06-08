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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.optimize.OptimizeFactorFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.junit.Test;

import java.io.File;

public class LinearAutomationTests implements CellFeatures, OptimizeFactorFeatures {
	@Test
	public void riseFall() {
		int sr = OutputLine.sampleRate;

		TimeCell clock = new TimeCell();

		double direction = 0.9;
		double magnitude = 0.3;
		double exponent = 3.6;

		Producer<PackedCollection<?>> d = c(direction);
		Producer<PackedCollection<?>> m = c(magnitude);
		Producer<PackedCollection<?>> e = c(exponent);

		int seconds = 30;

		Producer<PackedCollection<?>> freq = riseFall(0, 20000, 0.5,
														d, m, e, clock.time(sr), c(seconds));

		CellList cells = w(c(0.0), c(1.0), "Library/Snare Perc DD.wav")
				.addRequirements(clock)
				.map(fc(i -> sf(0.1)))
				.map(fc(i -> lp(freq, c(0.05))))
				.o(i -> new File("results/filter-rise-fall.wav"));

		cells.sec(seconds).get().run();
	}
}
