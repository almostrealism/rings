package io.almostrealism.lambda;

import org.almostrealism.util.Producer;

public interface Realization<T, O extends Producer, P> {
	public O realize(T data, P params);
}
