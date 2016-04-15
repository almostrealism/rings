package com.almostrealism.feedgrow.audio;

import com.almostrealism.feedgrow.cellular.CellAdapter;

public abstract class AudioCellAdapter extends CellAdapter<Long> {
	public static int depth = Byte.MAX_VALUE; // AudioProteinCache.depth / 100;
	public static double PI = Math.PI;
	
	public AudioCellAdapter(AudioProteinCache c) {
		setProteinCache(c);
	}
	
	protected int toFrames(int msec) { return (int) ((msec / 1000d) * AudioProteinCache.sampleRate); }
}
