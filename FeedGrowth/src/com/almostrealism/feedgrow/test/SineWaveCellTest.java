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

package com.almostrealism.feedgrow.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.sound.sampled.LineUnavailableException;

import com.almostrealism.audio.AudioProteinCache;
import com.almostrealism.audio.filter.Envelope;
import com.almostrealism.feedgrow.ReceptorPlayer;
import com.almostrealism.feedgrow.audio.WaveOutput;
import com.almostrealism.feedgrow.metering.AudioMeter;
import com.almostrealism.synth.SineWaveCell;

public class SineWaveCellTest {
	public static int max = 10 * 1000 * AudioProteinCache.sampleRate; // 100 Seconds
	
	public static void main(String args[]) throws LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
		AudioMeter meter = new AudioMeter(cache);
		meter.setReportingFrequency(100);
		meter.setClipValue(1000000000);
		
		try {
			meter.setForwarding(new WaveOutput(new FileOutputStream("Test.wav"), cache));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		((CellAdapter<Long>) organ.firstCell()).setMeter(meter);
		
		SineWaveCell generator = new SineWaveCell(cache);
		generator.setFreq(880);
		generator.setNoteLength(10000);
		generator.setAmplitude(0.1);
		generator.setEnvelope(new Envelope() {
			public double getScale(double time) {
				if (time < 0.1)
					return (time / 0.1); // Attenuate the first 10% of audio
				else
					return Math.cos(time * Math.PI / 2);
			}
		});
		
		ReceptorPlayer p = new ReceptorPlayer(null);
		p.setProteinCache(cache);
		
		generator.setReceptor(p);
//		meter.setForwarding(p);
		
		long l;
		
		for (l = 0; l < max; l++) {
			generator.push(0);
		}
		
		p.finish();
	}
}
