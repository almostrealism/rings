/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.filter.test;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.ReceptorCell;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.function.Supplier;

public class AudioCellTest implements CellFeatures {
	@Test
	public void filterFrame() {
		WaveOutput out = new WaveOutput();

		Supplier<Runnable> op =
				w(0, "Library/Snare Perc DD.wav")
						.f(i -> hp(2000, 0.1))
						.map(i -> new ReceptorCell<>(out))
						.iter(10);
		Runnable r = op.get();
		System.out.println(Arrays.toString(out.getData().toArray(0, 5)));

		r.run();
		System.out.println(Arrays.toString(out.getData().toArray(0, 5)));

		Assert.assertTrue(out.getData().toDouble(2) == 0.0);
		Assert.assertFalse(out.getData().toDouble(3) == 0.0);
	}

	@Test
	public void filter() {
		Supplier<Runnable> op =
				w(0, "Library/Snare Perc DD.wav")
						.f(i -> hp(2000, 0.1))
						.om(i -> new File("results/filter-cell-test.wav"))
						.sec(10);
		Runnable r = op.get();
		r.run();
	}

	@Test
	public void repeat() {
		Supplier<Runnable> op =
				w(0, c(1.0), "Library/Snare Perc DD.wav")
						.om(i -> new File("results/repeat-cell.wav"))
						.sec(10);
		Runnable r = op.get();
		r.run();
	}
}
