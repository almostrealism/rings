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

package org.almostrealism.audio.synth;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.audio.OutputLine;

public class SampleFactory {
	public static Sample createSample(String file) throws UnsupportedAudioFileException, IOException {
		AudioInputStream input = AudioSystem.getAudioInputStream(new File(file));
		
		byte data[] = new byte[100000];
		
		for (int i = 0; input.available() > 0; i++) {
			input.read(data); // TODO  Respect return value
		}
		
		return new Sample(data);
	}
	
	public static Sample createSample(double freq, int ms) {
		int samples = ms * OutputLine.sampleRate / 1000;
		PackedCollection<Scalar> output = Scalar.scalarBank(samples);

		double period = (double) OutputLine.sampleRate / freq;
		for (int i = 0; i < output.getCount(); i++) {
			double angle = 2.0 * Math.PI * i / period;
			output.get(i).setValue(Math.sin(angle) * CollectionTemporalCellAdapter.depth);
		}

		return new Sample(output);
	}
}
