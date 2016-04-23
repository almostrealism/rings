package com.almostrealism.feedgrow.audio;

import com.almostrealism.feedgrow.content.ProteinCache;

public class AudioProteinCache implements ProteinCache<Long> {
	public static int addWait = 0;
	
	public static int sampleRate = 24 * 1024; // 44100
	public static int bufferDuration = 100;
	
	public static int depth = Integer.MAX_VALUE;
	public static long convertToByte = depth / Byte.MAX_VALUE;
	
	private long data[] = new long[sampleRate * bufferDuration];
	private int cursor;
	
	public AudioProteinCache() { }
	
	public long addProtein(Long p) {
		tryWait();
		
		if (p == null) p = 0l;
		
		// Store the 64 bit value
		data[cursor] = p;
		
		// Also store the value as 8 bytes
		// insertIntoByteBuffer(cursor, p);
		
		// Instead flatten to one byte
		// byteData[cursor] = flatten(p);
		
		cursor++;
		
		long index = cursor - 1;
		cursor = cursor % data.length;
		return index;
	}
	
	public Long getProtein(long index) { return data[(int) index]; }
	
	public long[] getLongData() { return data; }
	
	private void tryWait() {
		if (addWait == 0) return;
		if (cursor % sampleRate != 0) return;
		
		try {
			Thread.sleep(addWait);
		} catch (InterruptedException e) { }
	}
}
