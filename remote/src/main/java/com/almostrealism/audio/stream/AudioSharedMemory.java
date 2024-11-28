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

package com.almostrealism.audio.stream;

import io.almostrealism.code.ComputeContext;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.SharedMemoryOutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.RAM;
import org.almostrealism.time.Temporal;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioSharedMemory {
	static long pos = 0;
	static int bufferFrames = BufferedOutputScheduler.defaultBufferFrames;
	static int sampleRate = 44100;
	static double currentFrequency = 220;

	public static void main(String args[]) throws InterruptedException, IOException {
		int size = bufferFrames * 16;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		SharedMemoryOutputLine line = new SharedMemoryOutputLine(createDestination(size));
		BufferedOutputScheduler scheduler = BufferedOutputScheduler.create(executor, line,
				bufferFrames, AudioSharedMemory::renderingProcess);

		AudioServer server = new AudioServer(7799);
		server.addStream("next", new BufferedOutputControl(scheduler));

		server.start();
		System.out.println("Server started");

		scheduler.start();
		System.out.println("Scheduler started");

		while (true) {
			Thread.sleep(2000);
			currentFrequency = currentFrequency == 220 ? 440 : 220;
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
}
