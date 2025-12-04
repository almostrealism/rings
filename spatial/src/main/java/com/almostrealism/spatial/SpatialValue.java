/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.algebra.Vector;

public class SpatialValue<T> extends SpatialElement {
	private double value;
	private double temperature;
	private int index;
	private boolean alt;
	private boolean dot;
	private T referent;

	public SpatialValue(Vector position, double value) {
		this(position, value, 0.0, false);
	}

	public SpatialValue(Vector position, double value, double temperature, boolean dot) {
		this(position, value, temperature, dot, false);
	}

	public SpatialValue(Vector position, double value, double temperature, boolean dot, boolean alt) {
		super(position);
		this.value = value;
		this.temperature = temperature;
		this.index = -1;
		this.dot = dot;
		this.alt = alt;
	}

	public double getValue() { return value; }
	public void setValue(double value) { this.value = value; }

	public double getTemperature() { return temperature; }
	public void setTemperature(double temperature) { this.temperature = temperature; }

	public int getIndex() { return index; }
	public void setIndex(int index) { this.index = index; }

	public boolean isAlt() { return alt; }
	public void setAlt(boolean alt) { this.alt = alt; }

	public boolean isDot() {
		return dot;
	}

	public void setDot(boolean dot) {
		this.dot = dot;
	}

	public T getReferent() { return referent; }
	public void setReferent(T referent) { this.referent = referent; }
}
