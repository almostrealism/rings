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

package com.almostrealism.synth;

import com.almostrealism.audio.AudioProteinCache;
import com.almostrealism.audio.filter.AudioCellAdapter;
import com.almostrealism.audio.filter.Envelope;

public class SineWaveCell extends AudioCellAdapter {
	private Envelope env;
	
	private double wavePos, wavelength = 1.0;
	private double wavelengthFrames = AudioProteinCache.sampleRate;
	
	private double ampScale = 0.5;
	private double notePos, phase;
	private int note;
	private boolean noteEnded = false;
	
	public SineWaveCell(AudioProteinCache c) { super(c); }
	
	public void strike() { this.notePos = 0; }
	
	public void setFreq(double hertz) {
		this.wavelengthFrames = ((double) AudioProteinCache.sampleRate) / hertz;
		this.wavelength = 1.0 / wavelengthFrames;
	}
	
	public void setPhase(double phase) { this.phase = phase; }
	
	public void setEnvelope(Envelope e) { this.env = e; }
	
	public void setAmplitude(double amp) { this.ampScale = amp; }
	
	public void setNoteLength(int msec) { this.note = toFrames(msec); }
	
	public void push(long index) {
		if (noteEnded) {
			super.push(addProtein(0l));
			return;
		}
		
		if (notePos >= 1.0  && note != 0) {
			noteEnded = true;
			super.push(addProtein(0l));
			return;
		}
		
		double d = ampScale * Math.sin((wavePos + phase) * 2 * PI) * depth;
		if (env != null) d = d * env.getScale(notePos);
		
		long l = addProtein((long) d);
		wavePos += wavelength;
		notePos += 1.0 / note;
		
		super.push(l);
	}
}