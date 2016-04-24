package com.almostrealism.feedgrow.delay;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.SummationCell;
import com.almostrealism.receptor.ui.Updatable;

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
	
	public synchronized void setDelayInFrames(long frames) { this.delay = (int) frames; }
	
	public synchronized long getDelayInFrames() { return this.delay; }
	
	public synchronized Position getPosition() {
		Position p = new Position();
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