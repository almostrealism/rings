package com.almostrealism.receptor.synth;

public class Sample implements Source {
	private int i;
	private long data[];
	
	public Sample(byte data[]) {
		this.data = new long[data.length];
		
		for (int i = 0; i < data.length; i++) {
			this.data[i] = (long) data[i];
		}
	}
	
	public Sample(long data[]) {
		this.data = data;
	}
	
	public long next() { return isDone() ? 0 : data[i++]; }
	
	public boolean isDone() { return i >= data.length; };
}
