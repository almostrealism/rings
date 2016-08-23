package com.almostrealism.glitchfarm.transform;

public class LinearResampleTransformer extends ResampleTransformer {
	public byte resample(byte a, byte b, double delta) {
		return (byte) (a + (b - a) * delta);
	}
	
	public String toString() { return "LinearResampleTransformer"; }
}
