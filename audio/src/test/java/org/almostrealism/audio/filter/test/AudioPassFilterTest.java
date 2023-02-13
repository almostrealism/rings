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

package org.almostrealism.audio.filter.test;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Provider;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.computations.Loop;
import org.almostrealism.hardware.mem.MemoryDataArgumentMap;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class AudioPassFilterTest implements CellFeatures, TestFeatures {
	@Test
	public void highPass() throws IOException {
		WavFile f = WavFile.openWavFile(new File("Library/Snare Perc DD.wav"));

		double data[][] = new double[f.getNumChannels()][(int) f.getFramesRemaining()];
		f.readFrames(data, (int) f.getFramesRemaining());

		PackedCollection<?> values = WavFile.channel(data, 0);
		PackedCollection<?> out = new PackedCollection<>(values.getMemLength());
		Scalar current = new Scalar();

		AudioPassFilter filter = new AudioPassFilter((int) f.getSampleRate(), c(2000), v(0.1), true);
		Evaluable<PackedCollection<?>> ev = filter.getResultant(p(current)).get();
		Runnable tick = filter.tick().get();

		for (int i = 0; i < values.getMemLength(); i++) {
			current.setValue(values.toDouble(i));
			out.setMem(i, ev.evaluate().toDouble(0));
			tick.run();
		}

		WavFile wav = WavFile.newWavFile(new File("results/filter-test.wav"), 1, out.getMemLength(),
				f.getValidBits(), f.getSampleRate());

		for (int i = 0; i < out.getMemLength(); i++) {
			double value = out.toDouble(i);

			try {
				wav.writeFrames(new double[][]{{value}}, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		wav.close();
	}

	@Test
	public void loopTest() {
		/*
		PackedCollection<?> values = new PackedCollection<>(100);
		values.setMem(1.0, 2.0, 3.0, 4.0, 5.0);

		PackedCollection<?> out = new PackedCollection<>(1);
		CellList c = w(new WaveData(values, OutputLine.sampleRate)).map(new ReceptorCell<>());

		OperationList op = new OperationList();
		op.add(c.setup());

		OperationList tick = new OperationList();
		tick.add(c.tick());
		 */

		// MemoryDataArgumentMap.enableArgumentAggregation = false;

		PackedCollection<?> in = new PackedCollection<>(1);
		in.setMem(1.0);

		PolymorphicAudioData data = new PolymorphicAudioData();
		Scalar out = new Scalar();

		Loop l = new Loop(new TestAudioPassFilterComputation(data, p(in), () -> new Provider<>(out)), 3);
		l.get().run();
		System.out.println(out.toDouble(0));
	}
}
