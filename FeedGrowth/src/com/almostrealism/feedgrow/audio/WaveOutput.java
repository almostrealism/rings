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

package com.almostrealism.feedgrow.audio;

import java.io.IOException;
import java.io.OutputStream;

import org.almostrealism.cells.Receptor;
import org.almostrealism.protein.ProteinCache;

public class WaveOutput implements Receptor<Long> {
	public static double scale = 0.00001;
	
	private static int riffHead[] = {0x52, 0x49, 0x46, 0x46}; // ASCII "RIFF"
	private static int chunkSize[] = {0, 0, 0, 0};
	private static int waveHead[] = {0x57, 0x41, 0x56, 0x45}; // ASCII "WAVE"
	private static int fmtHead[] = {0x66, 0x6d, 0x74, 0x20}; // ASCII "fmt "
	private static int subchunk1Head[] = {0x10, 0x0, 0x0, 0x0}; // 16
	private static int audioHead[] = {0x01, 0x0}; // PCM
	private static int channelHead[] = {0x02, 0x0}; // 2 Channels
	private static int sampleRate[] = {0x22, 0x56, 0x00, 0x00}; // 220500 sample rate (per channel)
	private static int byteRate[] = {0x88, 0x58, 0x01, 0x00}; // 88200 byte rate
	private static int blockAlign[] = {0x04, 0x00};
	private static int bitsPerSample[] = {0x10, 0x00};  // 16 bits per sample
	private static int dataHead[] = {0x64, 0x61, 0x74, 0x61}; // ASCII "data"
	private static int subchunk2Head[] = {0x00, 0x08, 0x00, 0x00}; // Subchunk 2 size is 2048
	
	private OutputStream out;
	private AudioProteinCache p;
	
	public WaveOutput(OutputStream out, AudioProteinCache c) {
		this.out = out;
		this.setProteinCache(c);
	}
	
	public void writeHead() throws IOException {
		for (int i = 0; i < riffHead.length; i++) out.write(riffHead[i]);
		
		int s[] = chunkSize;
		for (int i = 0; i < s.length; i++) out.write(s[i]);
		
		for (int i = 0; i < waveHead.length; i++) out.write(waveHead[i]);
		for (int i = 0; i < fmtHead.length; i++) out.write(fmtHead[i]);
		for (int i = 0; i < subchunk1Head.length; i++) out.write(subchunk1Head[i]);
		for (int i = 0; i < audioHead.length; i++) out.write(audioHead[i]);
		for (int i = 0; i < channelHead.length; i++) out.write(channelHead[i]);
		for (int i = 0; i < sampleRate.length; i++) out.write(sampleRate[i]);
		for (int i = 0; i < byteRate.length; i++) out.write(byteRate[i]);
		for (int i = 0; i < blockAlign.length; i++) out.write(blockAlign[i]);
		for (int i = 0; i < bitsPerSample.length; i++) out.write(bitsPerSample[i]);
		for (int i = 0; i < dataHead.length; i++) out.write(dataHead[i]);
		for (int i = 0; i < subchunk2Head.length; i++) out.write(subchunk2Head[i]);
	}
	
	public void setProteinCache(ProteinCache<Long> p) { this.p = (AudioProteinCache) p; }
	
	public void push(long index) {
		long frame = (long) (p.getProtein(index).longValue() * scale);
		byte b[] = longToByte(frame);
		
		try {
			out.write(b); // Left
			out.write(b); // Right
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] longToByte(long l) {
		byte b[] = new byte[4];
		b[3] = (byte) (l >> 24);
		b[2] = (byte) (l >> 16);
		b[1] = (byte) (l >> 8);
		b[0] = (byte) l;
		return b;
	}
}
