package com.almostrealism.feedgrow.heredity;

public interface Gene<T> {
	public Factor<T> getFactor(int index);
	
	public int length();
}
