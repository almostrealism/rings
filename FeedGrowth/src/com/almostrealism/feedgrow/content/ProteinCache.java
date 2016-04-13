package com.almostrealism.feedgrow.content;

public interface ProteinCache<T> {
	public long addProtein(T p);
	
	public T getProtein(long index);
}
