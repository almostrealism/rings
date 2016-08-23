package com.almostrealism.glitchfarm.filter;

public class AmplitudeRangeFilter implements LineFilter {
	private int min = 0;
	private int max = 128;
	
	public byte[] filter(byte[] b) {
		byte bx[] = new byte[b.length];
		
		for (int i = 0; i < b.length; i++) {
			if (Math.abs(b[i]) < this.min || Math.abs(b[i]) > this.max) {
				bx[i] = 0;
			} else {
				bx[i] = b[i];
			}
		}
		
		return bx;
	}
	
	/**
	 * @param min  [0 - 128]
	 */
	public void setMinimumAmplitude(int min) { this.min = min; }
	
	/**
	 * @param max  [0 - 128]
	 */
	public void setMaximumAmplitude(int max) { this.max = max; }
	
	public int getMinimumAmplitude() { return this.min; }
	public int getMaximumAmplitude() { return this.max; }
}
