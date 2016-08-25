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

package com.almostrealism.audio.filter;

import org.almostrealism.cells.SummationCell;
import org.almostrealism.cells.delay.Delay;
import org.almostrealism.time.Updatable;

import com.almostrealism.feedgrow.audio.AudioProteinCache;

public class BasicDelayCell extends SummationCell implements Delay {
	public static int bufferDuration = 10;
	
	private long buffer[] = new long[bufferDuration * AudioProteinCache.sampleRate];
	private int cursor;
	private int delay;
	
	private Updatable updatable;
	
	public BasicDelayCell(int delay) {
		setDelay(delay);
	}
	
	public synchronized void setDelay(int msec) {
		this.delay = (int) ((msec / 1000d) * AudioProteinCache.sampleRate);
	}
	
	public synchronized int getDelay() { return 1000 * delay / AudioProteinCache.sampleRate; }
	
	public synchronized void setDelayInFrames(long frames) {
		this.delay = (int) frames;
		if (delay <= 0) delay = 1;
	}
	
	public synchronized long getDelayInFrames() { return this.delay; }
	
	public synchronized Position getPosition() {
		Position p = new Position();
		if (delay == 0) delay = 1;
		p.pos = (cursor % delay) / ((double) delay);
		p.value = buffer[cursor];
		return p;
	}
	
	public void setUpdatable(Updatable ui) { this.updatable = ui; }
	
	public synchronized void push(long i) {
		int dPos = (cursor + delay) % buffer.length;
		
		this.buffer[dPos] += getProtein(i);
		
		long out = addProtein(this.buffer[cursor]);
		
		if (updatable != null && cursor % updatable.getResolution() == 0) updatable.update();
		
		this.buffer[cursor] = 0;
		cursor++;
		cursor = cursor % buffer.length;
		
		super.push(out);
	}
	
	public static class Position {
		public double pos;
		public long value;
	}
}