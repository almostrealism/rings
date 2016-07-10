/*
 * Copyright 2016 Michael Murray
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

package com.almostrealism.receptor.synth;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.almostrealism.feedgrow.audio.AudioCellAdapter;
import com.almostrealism.feedgrow.audio.AudioProteinCache;

public class SampleFactory {
	public static Sample createSample(String file) throws UnsupportedAudioFileException, IOException {
		AudioInputStream input = AudioSystem.getAudioInputStream(new File(file));
		
		byte data[] = new byte[100000];
		
		for (int i = 0; input.available() > 0; i++) {
			input.read(data);
		}
		
		return new Sample(data);
	}
	
	public static Sample createSample(double freq, int ms) {
		int samples = (int) ((ms * AudioProteinCache.sampleRate) / 1000);
		long[] output = new long[samples];

		double period = (double) AudioProteinCache.sampleRate / freq;
		for (int i = 0; i < output.length; i++) {
			double angle = 2.0 * Math.PI * i / period;
			output[i] = (int) (Math.sin(angle) * AudioCellAdapter.depth);
		}

		return new Sample(output);
	}
}
