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

package org.almostrealism.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class Sample {
	private boolean stop = true;
	private boolean mute = false;
	private boolean loop = true;
	
	private final AudioFormat format;
	
	byte[][] data;
	byte[] empty;
	double[] realFFT;
	
	int pos, loopStart, loopEnd;
	
	public Sample(byte[][] data, AudioFormat format) {
		this.data = data;
		this.loopStart = 0;
		this.loopEnd = this.data.length;
		this.empty = new byte[this.data[0].length];
		this.format = format;
	}
	
	public void loadFFT(int n) {
//		this.realFFT = new double[2*n];
//		
//		for (int i = 0; i < realFFT.length; i++) {
//			this.realFFT[i] = ((double) data[i][1]) / 255.0;
//		}
//		
//		DoubleFFT_1D fftd = new DoubleFFT_1D(n);
//		fftd.realForward(this.realFFT);
	}
	
	public Thread playThread(final SourceDataLine line) {
		return new Thread(new Runnable() {
			public void run() {
				play(line);
			}
		});
	}
	
	public synchronized void play(SourceDataLine line) {
		System.out.println(this + ": Play");
		
		stop = false;
		
		for (pos = loopStart; pos < loopEnd; pos++) {
			if (mute) {
				line.write(empty, 0, empty.length);
			} else {
				line.write(data[pos], 0, data[pos].length);
			}
			
			if (loop && pos + 1 == loopEnd) pos = loopStart - 1;
			if (stop) return;
		}
		
		stop = true;
	}
	
	public void stop() {
		System.out.println(this + ": Stop");
		stop = true;
	}
	
	public void restart() { this.pos = this.loopStart; }
	
	public AudioFormat getFormat() { return this.format; }
	
	public boolean isStopped() { return stop; }
	
	public void mute() { this.mute = true; }
	public void unmute() { this.mute = false; }
	public boolean toggleMute() { return this.mute = !this.mute; }
	public boolean isMuted() { return this.mute; }
	
	public void loop() { this.loop = true; }
	public void unloop() { this.loop = false; }
	public boolean toggleLoop() { return this.loop = !this.loop; }
	public boolean isLooped() { return this.loop; }
	
	/**
	 * Splits the loop of this sample evenly into the specified number of smaller samples.
	 * If the number of pieces does not evenly divide the loop length, the loop will be extended
	 * to evenly divided the number of pieces. By default, these subsamples will be unlooped.
	 * 
	 * @param pieces  Number of new samples to create.
	 * @return  Array of subsamples.
	 */
	public Sample[] splitLoop(int pieces) {
		int totSize = this.loopEnd - this.loopStart;
		if (totSize % pieces != 0)
			totSize += pieces - (totSize % pieces);
		int size = totSize / pieces;
		
		Sample[] s = new Sample[pieces];
		
		for (int i = 0; i < pieces; i++) {
			byte[][] newData = new byte[size][this.data[0].length];
			System.arraycopy(this.data, this.loopStart + i * size, newData, 0, size);
			s[i] = new Sample(newData, this.format);
			s[i].unloop();
		}
		
		return s;
	}
	
	public String toString() { return "JavaAudioSample[" + this.data.length + ":" + this.hashCode() + "]"; }
}
