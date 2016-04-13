package com.almostrealism.feedgrow.heredity;

public class LongScaleFactor implements Factor<Long> {
	private double scale;
	
	public LongScaleFactor() { }
	
	public LongScaleFactor(double scale) { this.scale = scale; }
	
	public Long getResultant(Long value) { return (long) (value * scale); }
	
	public void setScale(double s) { this.scale = s; }
	
	public double getScale() { return scale; }
}
