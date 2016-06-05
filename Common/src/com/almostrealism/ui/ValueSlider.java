package com.almostrealism.ui;

import javax.swing.JSlider;

import com.almostrealism.util.ValueProducer;

public class ValueSlider extends JSlider implements ValueProducer {
	private static final int scale = 10000;
	
	private double min, max;
	
	public ValueSlider(int orient, double min, double max, double value) {
		super(orient, 0, scale, 0);
		this.min = min;
		this.max = max;
		this.setValue((int) (scale * (value - min) / (max - min)));
	}
	
	public double value() {
		return (max - min) * (getValue() / (double) scale) + min;
	}
}
