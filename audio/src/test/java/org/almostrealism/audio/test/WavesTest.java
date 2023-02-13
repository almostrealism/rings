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

package org.almostrealism.audio.test;

import org.almostrealism.algebra.ScalarBankHeap;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollectionHeap;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class WavesTest implements CellFeatures, TestFeatures {
	@Test
	public void splits() {
		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(600 * OutputLine.sampleRate), PackedCollectionHeap::destroy);

		Waves waves = new Waves();
		waves.getChoices().getChoices().add(0);
		waves.addSplits(
				List.of(new File("/Users/michael/AlmostRealism/ringsdesktop/Stems/001 Kicks 1.7_1.wav"),
						new File("/Users/michael/AlmostRealism/ringsdesktop/Stems/002 Percussion 1.6_1.wav"),
						new File("/Users/michael/AlmostRealism/ringsdesktop/Stems/006 Orchestra 1.5_1.wav")),
				bpm(116.0), Math.pow(10, -6), Set.of(0), 1.0);
		CellList cells = cells(3, i ->
				waves.getChoiceCell(0, v(choose(i)), v(0.0), v(0.0), v(0.0),
									v(0.0), v(bpm(116.0).l(1))));
		cells = cells.sum().o(i -> new File("results/waves-splits-test-" + i + ".wav"));
		cells.sec(10).get().run();
	}

	private double choose(int i) {
		if (i == 0) {
			return 0.0;
		} else if (i == 1) {
			return 0.5;
 		} else if (i == 2) {
			return 0.99;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	public void choice() throws IOException {
		Waves waves = Waves.load(new File("sources.json"));
		System.out.println(waves.getSegmentChoice(0, 0.4914, 0.0, 0.0, 0.0).getSourceText());
	}
}
