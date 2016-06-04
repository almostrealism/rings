package com.almostrealism.visualize.shading;

public interface Specular {
	public void setSpecular(float r, float g, float b, float a);
	
	public float[] getSpecular();
	
	public void setShininess(float s);
	
	public float getShininess();
}
