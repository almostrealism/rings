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

package org.almostrealism.audio.util;

import io.almostrealism.code.ComputeContext;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.SharedMemoryOutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.RAM;
import org.almostrealism.time.Temporal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioSharedMemory {
	static boolean useOutputLine = true;
	static long pos = 0;
	static int bufferFrames = BufferedOutputScheduler.defaultBufferFrames;
	static int sampleRate = 44100;
	static double currentFrequency = 220;

	public static void main(String args[]) throws InterruptedException {
		if (useOutputLine) {
			outputLine();
		} else {
			direct();
		}
	}

	protected static PackedCollection<?> createDestination(int numSamples) {
		String shared = "/Users/michael/Library/Containers/com.almostrealism.Rings.ringsAUfx/Data/rings_shm";

		ComputeContext<?> ctx = Hardware.getLocalHardware().getComputeContext();
		PackedCollection<?> dest = ctx.getDataContext().sharedMemory(len -> shared, () -> new PackedCollection<>(numSamples));

		System.out.println("Buffer ptr: " + ((RAM) dest.getMem()).getContentPointer());
		return dest;
	}

	protected static void generateSineWave(float dest[], int numSamples) {
		generateSineWave(dest, numSamples, currentFrequency);
	}

	protected static void generateSineWave(float dest[], int numSamples, double frequency) {
		for (int i = 0; i < numSamples; i++) {
			double time = Math.toIntExact(pos++ % Integer.MAX_VALUE) / (double) sampleRate;
			dest[i] = (float) Math.sin(2 * Math.PI * frequency * time);
		}
	}

	protected static Temporal renderingProcess(Producer<PackedCollection<?>> buffer) {
		float[] data = new float[bufferFrames];

		return () -> () -> {
			Evaluable<PackedCollection<?>> out = buffer.get();

			return () -> {
				generateSineWave(data, bufferFrames);
				out.evaluate().setMem(data);
			};
		};
	}

	protected static void outputLine() throws InterruptedException {
		int size = bufferFrames * 32;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		SharedMemoryOutputLine line = new SharedMemoryOutputLine(createDestination(size));
		BufferedOutputScheduler scheduler = BufferedOutputScheduler.create(executor, line,
										bufferFrames, AudioSharedMemory::renderingProcess);
		scheduler.start();

		while (true) {
			Thread.sleep(200);
			currentFrequency = currentFrequency == 220 ? 440 : 220;
		}
	}

	protected static void direct() throws InterruptedException {
		double duration = 4.0;
		int numSamples = (int) (sampleRate * duration);

		double frequencyA = 220.0;
		double frequencyB = 440.0;
		float[] sineWaveDataA = new float[numSamples];
		float[] sineWaveDataB = new float[numSamples];
		for (int i = 0; i < numSamples; i++) {
			double time = i / (double) sampleRate;
			sineWaveDataA[i] = (float) Math.sin(2 * Math.PI * frequencyA * time);
			sineWaveDataB[i] = (float) Math.sin(2 * Math.PI * frequencyB * time);
		}

		PackedCollection<?> dest = createDestination(numSamples);

		boolean test = false;
		boolean updates = true;

		int count = 0;

		while (true) {
			Thread.sleep(3000);
			boolean alt = count++ % 2 == 0;
			float data[] = alt ? sineWaveDataA : sineWaveDataB;

			if (test) {
				dest.fill(0.001);
			} else if (updates) {
				dest.setMem(data);
				System.out.println("Assigned buffer contents: " + alt);

				if (Math.abs(dest.toDouble(100) - data[100]) > 0.0001) {
					System.out.println("Error: " + dest.toDouble(100) + " != " + sineWaveDataA[100]);
				}
			}

			System.out.println(dest.toArrayString(0, 10));
		}
	}
}
