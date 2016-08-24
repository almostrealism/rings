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

import javax.sound.sampled.LineUnavailableException;

import org.almostrealism.optimize.StableDurationHealthComputation;

import com.almostrealism.feedgrow.ReceptorPlayer;
import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.Envelope;
import com.almostrealism.feedgrow.audio.SineWaveCell;

public class DyadicOrganismTest {
	public static void main(String args[]) throws FileNotFoundException, LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
		BasicDyadicChromosome c = new BasicDyadicChromosome(0.999, 0.99);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(1000, c, cache);
		
		StableDurationHealthComputation h = new StableDurationHealthComputation(cache);
//		h.computeHealth(s);
		
		SineWaveCell sine = new SineWaveCell(cache);
		sine.setNoteLength(500);
		sine.setAmplitude(0.5);
		sine.setFreq(200);
		sine.setEnvelope(new Envelope() {
			public double getScale(double time) {
				if (time < 0.1)
					return (time / 0.1); // Attenuate the first 10% of audio
				else
					return Math.cos(time * Math.PI / 2);
			}
		});
		
		sine.setReceptor(s.getCellA());
		ReceptorPlayer p = new ReceptorPlayer(cache);
		s.getCellA().setMeter(p);
		
		for (long l = 0; l < Long.MAX_VALUE; l++) {
			sine.push(0);
			s.tick();
		}
		
		p.finish();
	}
}
