package com.almostrealism.feedgrow.heredity;

public class IdentityFactor<T> implements Factor<T> {
	public T getResultant(T value) { return value; }
}
