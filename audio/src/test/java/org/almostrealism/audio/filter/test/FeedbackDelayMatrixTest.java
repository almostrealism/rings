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

package org.almostrealism.audio.filter.test;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.TemporalFactor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class FeedbackDelayMatrixTest extends AudioPassFilterTest {
	private static double gain = 0.2;

	@Test
	public void reverb() throws IOException {
		WavFile f = WavFile.openWavFile(new File("Library/Snare Perc DD.wav"));
		FDN verb = new FDN((int) f.getSampleRate());
		runFilter("reverb", f, verb);
	}

	private static class FDN implements TemporalFactor<PackedCollection<?>>, CodeFeatures {
		private int sampleRate;
		private int delayLength1 = 200;
		private int delayLength2 = 350;
		private double[] delayLine1 = new double[delayLength1];
		private double[] delayLine2 = new double[delayLength2];
		private int index1 = 0, index2 = 0;

		float[][] matrix = {{0.5f, 0.3f}, {0.3f, 0.5f}};

		private Evaluable<PackedCollection<?>> input;
		private PackedCollection<?> output;

		public FDN(int sampleRate) {
			this.sampleRate = sampleRate;
			this.output = new PackedCollection<>(1);
		}

		@Override
		public Producer<PackedCollection<?>> getResultant(Producer<PackedCollection<?>> value) {
			this.input = value.get();
			return p(output);
		}

		@Override
		public Supplier<Runnable> tick() {
			return () -> () -> {
				double inputSample = gain * input.evaluate().toDouble(0);

				// Retrieve delayed samples
				double delayedSample1 = delayLine1[index1];
				double delayedSample2 = delayLine2[index2];

				// Calculate new samples using feedback matrix
				double newSample1 = matrix[0][0] * delayedSample1 + matrix[0][1] * delayedSample2 + inputSample;
				double newSample2 = matrix[1][0] * delayedSample1 + matrix[1][1] * delayedSample2 + inputSample;

				// Update delay lines
				delayLine1[index1] = newSample1;
				delayLine2[index2] = newSample2;

				// Increment indexes and loop if necessary
				index1 = (index1 + 1) % delayLength1;
				index2 = (index2 + 1) % delayLength2;

				// Output sample (may include further processing or filtering)
				output.set(0, newSample1 + newSample2);
			};
		}
	}
}
