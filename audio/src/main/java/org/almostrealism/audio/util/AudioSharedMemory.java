package org.almostrealism.audio.util;

import io.almostrealism.code.ComputeContext;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.RAM;

public class AudioSharedMemory {
	public static void main(String args[]) throws InterruptedException {
		int sampleRate = 44100;
		double duration = 4.0;
		int numSamples = (int) (sampleRate * duration);

		ComputeContext<?> ctx = Hardware.getLocalHardware().getComputeContext();

		double frequencyA = 220.0;
		double frequencyB = 440.0;
		float[] sineWaveDataA = new float[numSamples];
		float[] sineWaveDataB = new float[numSamples];
		for (int i = 0; i < numSamples; i++) {
			double time = i / (double) sampleRate;
			sineWaveDataA[i] = (float) Math.sin(2 * Math.PI * frequencyA * time);
			sineWaveDataB[i] = (float) Math.sin(2 * Math.PI * frequencyB * time);
		}

		String shared = "/Users/michael/Library/Containers/com.almostrealism.Rings.ringsAUfx/Data/rings_shm";
		PackedCollection<?> dest = ctx.getDataContext().sharedMemory(len -> shared, () -> new PackedCollection<>(numSamples));

		System.out.println("Buffer ptr: " + ((RAM) dest.getMem()).getContentPointer());

		boolean test = false;
		boolean updates = true;

		int count = 0;

		while (true) {
			Thread.sleep(3000);
			boolean alt = count++ % 2 == 0;
			float data[] = alt ? sineWaveDataA : sineWaveDataB;

			if (test) {
//				dest.fill(alt ? 1.0 : -1.0);
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
