package com.almostrealism.feedgrow.content;

public class FloatingPointProteinCache implements ProteinCache<Double> {
	public static int addWait = 0;
	
	public static int sampleRate = 44100;
	public static int depth = (int) StrictMath.pow(2, 32);
	public static int bufferDuration = 100;
	
	private double data[] = new double[sampleRate * bufferDuration];
	private int cursor;
	
	public long addProtein(Double p) {
		tryWait();
		data[cursor] = p == null ? 0.0 : p;
		cursor++;
		
		long index = cursor - 1;
		cursor = cursor % data.length;
		return index;
	}
	
	public Double getProtein(long index) { return data[(int) index]; }
	
	private void tryWait() {
		if (addWait == 0) return;
		if (cursor % sampleRate != 0) return;
		
		try {
			Thread.sleep(addWait);
		} catch (InterruptedException e) { }
	}
}
