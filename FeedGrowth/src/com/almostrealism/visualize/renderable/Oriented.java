package com.almostrealism.visualize.renderable;

public interface Oriented {
	public void setOrientation(float angle, float x, float y, float z);
	
	public float[] getOrientation();
}
