package org.almostrealism.audio.filter;

import io.almostrealism.scope.Scope;
import org.almostrealism.graph.ByteFunction;

public class AmplitudeRangeFilter implements ByteFunction<byte[]> {
	private int min = 0;
	private int max = 128;

	@Override
	public byte[] operate(byte[] b) {
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

	@Override
	public Scope<byte[]> getScope() { throw new RuntimeException("Not implemented"); }

	/** @param min  [0 - 128] */
	public void setMinimumAmplitude(int min) { this.min = min; }
	
	/** @param max  [0 - 128] */
	public void setMaximumAmplitude(int max) { this.max = max; }
	
	public int getMinimumAmplitude() { return this.min; }
	public int getMaximumAmplitude() { return this.max; }
}
