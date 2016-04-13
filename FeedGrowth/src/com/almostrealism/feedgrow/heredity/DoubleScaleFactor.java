package com.almostrealism.feedgrow.heredity;

public class DoubleScaleFactor implements Factor<Double> {
	private double scale;
	
	public DoubleScaleFactor() { }
	
	public DoubleScaleFactor(double scale) { this.scale = scale; }
	
	public Double getResultant(Double value) { return value * scale; }
	
	public void setScale(double s) { this.scale = s; }
	
	public double getScale() { return scale; }
}
