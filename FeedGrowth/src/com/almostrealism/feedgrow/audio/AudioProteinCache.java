package com.almostrealism.feedgrow.audio;

import com.almostrealism.feedgrow.content.ProteinCache;

public class AudioProteinCache implements ProteinCache<Long> {
	public static int addWait = 0;
	
	public static int sampleRate = 44100;
	public static int depth = (int) StrictMath.pow(2, 31);
	public static int bufferDuration = 100;
	
	private long data[] = new long[sampleRate * bufferDuration];
	private int cursor;
	
	public long addProtein(Long p) {
		tryWait();
		data[cursor] = p;
		cursor++;
		
		long index = cursor - 1;
		cursor = cursor % data.length;
		return index;
	}
	
	public Long getProtein(long index) { return data[(int) index]; }
	
	private void tryWait() {
		if (addWait == 0) return;
		if (cursor % sampleRate != 0) return;
		
		try {
			Thread.sleep(addWait);
		} catch (InterruptedException e) { }
	}
}
