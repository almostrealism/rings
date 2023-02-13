package org.almostrealism.audio.filter;

import io.almostrealism.scope.Scope;
import org.almostrealism.graph.ByteFunction;

public class SineFilter implements ByteFunction<byte[]> {
	private int x = 0, len = 100;
	private double scale = 2 * Math.PI / len;
	private double mix = 0.5;

	@Override
	public byte[] operate(byte[] b) {
		if (x == len) x = 0;
		
		byte bx[] = new byte[b.length];
		
		for (int i = 0; i < b.length; i++) {
			bx[i] = (byte) ((1.0 - mix) * b[i] + Math.sin(scale * x) * b[i] * mix);
		}
		
		x++;
		
		return bx;
	}

	@Override
	public Scope<byte[]> getScope() { throw new RuntimeException("Not implemented"); }
	
	public void setSampleLength(int len) { this.len = len; }
	public void setScale(double scale) { this.scale = scale; }
	public void setMixLevel(double mix) { this.mix = mix; }
	public int getSampleLength() { return this.len; }
	public double getScale() { return this.scale; }
	public double getMixLevel() { return this.mix; }

	@Override
	public String toString() {
		return "SineFilter(" + scale + " " + mix + ")";
	}
}
