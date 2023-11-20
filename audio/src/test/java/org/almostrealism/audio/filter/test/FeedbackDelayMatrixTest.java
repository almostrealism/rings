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

import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.TemporalFactor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class FeedbackDelayMatrixTest extends AudioPassFilterTest {
	private static double gain = 0.01;

	@Test
	public void reverb() throws IOException {
		WavFile f = WavFile.openWavFile(new File("Library/Snare Perc DD.wav"));
		FDN verb = new FDN((int) f.getSampleRate());
		runFilter("reverb", f, verb, true, (int) (f.getSampleRate() * 6));
	}

	private static class FDN implements TemporalFactor<PackedCollection<?>>, CodeFeatures {
		private int maxDelayFrames = 10000;
		private int size = 12;
		private int sampleRate;

		// TODO  Remove
		private double[][] matrix = {{0.5, 0.3}, {0.3, 0.5}};
		// TODO ---


		private Producer<PackedCollection<?>> input;
		private PackedCollection<?> delayIn, delayOut;
		private PackedCollection<?> delayBuffer;
		private PackedCollection<?> bufferLengths;
		private PackedCollection<?> bufferIndices;
		private PackedCollection<?> feedback;
		private PackedCollection<?> output;

		public FDN(int sampleRate) {
			this.sampleRate = sampleRate;
//			this.matrix = randomHouseholderMatrix(size, 0.5);
			this.matrix = householderMatrix(_normalize(c(1).repeat(size)), 0.5 / size);

			this.delayIn = new PackedCollection<>(size);
			this.delayOut = new PackedCollection<>(size);
			this.delayBuffer = new PackedCollection<>(shape(size, maxDelayFrames));
			this.bufferLengths = new PackedCollection<>(size);
			this.bufferIndices = new PackedCollection<>(size);
			this.feedback = new PackedCollection<>(shape(size, size));
			this.output = new PackedCollection<>(1);

			this.feedback.fill(pos -> matrix[pos[0]][pos[1]]);
			this.bufferLengths.fill(pos -> Math.floor(0.2 * maxDelayFrames + 0.8 * Math.random() * maxDelayFrames));
			this.bufferIndices.fill(pos -> 0.0);
		}

		@Override
		public Producer<PackedCollection<?>> getResultant(Producer<PackedCollection<?>> value) {
			this.input = value;
			return p(output);
		}

		@Override
		public Supplier<Runnable> tick() {
			Supplier<Runnable> matmul = a(p(delayOut), matmul(p(feedback), p(delayIn)).add(c(input, 0).mul(gain).repeat(size)));

			OperationList tick = new OperationList("FDN Tick");
			tick.add(a(cp(delayIn).each(),
					c(p(delayBuffer), shape(delayBuffer), integers(0, size), traverseEach(p(bufferIndices)))));
			tick.add(matmul);
			tick.add(a(c(p(delayBuffer), shape(delayBuffer), integers(0, size), traverseEach(p(bufferIndices))),
					traverseEach(p(delayOut))));
			tick.add(a(p(bufferIndices), add(p(bufferIndices), c(1).repeat(size)).mod(p(bufferLengths))));
			tick.add(a(p(output), sum(p(delayOut))));
			return tick;
		}

		public double[][] randomHouseholderMatrix(int size, double scale) {
			return householderMatrix(rand(size), scale);
		}

		public double[][] householderMatrix(Producer<PackedCollection<?>> v, double scale) {
			return householderMatrix(_normalize(v).evaluate().toArray(), scale);
		}

		public double[][] householderMatrix(double[] v, double scale) {
			int size = v.length;
			double[][] householder = new double[size][size];

			// Compute outer product of v
			double[][] outerProduct = new double[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					outerProduct[i][j] = 2 * v[i] * v[j];
				}
			}

			// Subtract the outer product from identity matrix
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					householder[i][j] = scale * ((i == j ? 1.0 : 0.0) - outerProduct[i][j]);
				}
			}

			return householder;
		}

		// Method to calculate transpose of a matrix
		public static double[][] transpose(double[][] matrix) {
			int rows = matrix.length;
			int columns = matrix[0].length;
			double[][] transpose = new double[columns][rows];

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					transpose[j][i] = matrix[i][j];
				}
			}

			return transpose;
		}

		// Method to multiply two matrices
		public static double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
			int r1 = firstMatrix.length;
			int c1 = firstMatrix[0].length;
			int c2 = secondMatrix[0].length;
			double[][] product = new double[r1][c2];

			for (int i = 0; i < r1; i++) {
				for (int j = 0; j < c2; j++) {
					for (int k = 0; k < c1; k++) {
						product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
					}
				}
			}

			return product;
		}
	}
}
