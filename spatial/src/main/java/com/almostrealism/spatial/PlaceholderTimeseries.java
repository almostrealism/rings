/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.algebra.Vector;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderTimeseries implements SpatialTimeseries {
	private double seconds;

	public PlaceholderTimeseries(double seconds) {
		this.seconds = seconds;
	}

	@Override
	public String getKey() { return null; }

	@Override
	public List<SpatialValue> elements(TemporalSpatialContext context) {
		List<SpatialValue> elements = new ArrayList<>();

		for (double t = 0; t < seconds; t = t + 0.25) {
			double freq = 0.5;
			Vector position = context.position(
					context.getSecondsToTime().applyAsDouble(t),
					0, 0, freq);
			elements.add(new SpatialValue<>(
					position, 1.5, 0.0, false));
		}

		return elements;
	}

	@Override
	public double getDuration(TemporalSpatialContext context) {
		return context.getSecondsToTime().applyAsDouble(seconds);
	}

	@Override
	public boolean isEmpty() { return false; }
}
