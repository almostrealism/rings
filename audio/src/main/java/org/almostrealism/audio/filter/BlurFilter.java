package org.almostrealism.audio.filter;

import io.almostrealism.scope.Scope;
import org.almostrealism.graph.ByteFunction;

public class BlurFilter implements ByteFunction<byte[]> {
	@Override
	public byte[] operate(byte[] b) {
		return b;
	}

	@Override
	public Scope<byte[]> getScope() { throw new RuntimeException("Not implemented"); }
}
