package com.almostrealism.feedgrow.audio;

import com.almostrealism.feedgrow.content.ProteinCache;

public class AudioProteinCache implements ProteinCache<Long> {
	public static int addWait = 0;
	
	public static int sampleRate = 44100;
	public static int depth = (int) StrictMath.pow(2, 31);
	public static int bufferDuration = 100;
	
	private long data[] = new long[sampleRate * bufferDuration];
	private byte byteData[] = new byte[8 * sampleRate * bufferDuration];
	private int cursor;
	
	public long addProtein(Long p) {
		tryWait();
		
		// Store the 64 bit value
		data[cursor] = p;
		
		// Also store the value as 8 bytes
		int xloc = 8 * cursor;
		byteData[xloc] = (byte) (data[cursor] >> 56);
		byteData[xloc + 1] = (byte) (data[cursor] >> 48);
		byteData[xloc + 2] = (byte) (data[cursor] >> 40);
		byteData[xloc + 3] = (byte) (data[cursor] >> 32);
		byteData[xloc + 4] = (byte) (data[cursor] >> 24);
		byteData[xloc + 5] = (byte) (data[cursor] >> 16);
		byteData[xloc + 6] = (byte) (data[cursor] >> 8);
		byteData[xloc + 7] = (byte) (data[cursor]);
		
		cursor++;
		
		long index = cursor - 1;
		cursor = cursor % data.length;
		return index;
	}
	
	public Long getProtein(long index) { return data[(int) index]; }
	
	public long[] getLongData() { return data; }
	
	public byte[] getByteData() { return byteData; }
	
	private void tryWait() {
		if (addWait == 0) return;
		if (cursor % sampleRate != 0) return;
		
		try {
			Thread.sleep(addWait);
		} catch (InterruptedException e) { }
	}
}
