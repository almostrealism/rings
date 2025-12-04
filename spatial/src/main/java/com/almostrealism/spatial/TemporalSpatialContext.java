/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.algebra.Vector;
import org.almostrealism.io.ConsoleFeatures;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public class TemporalSpatialContext implements ConsoleFeatures {
	public static int LAYER_SPACING = 40;
	public static double MAX_ZOOM = 1000;

	private boolean spatialFrequency;
	private boolean zoom;
	private double duration;
	private DoubleSupplier temporalScale;

	public TemporalSpatialContext() {
		this(() -> 1.0, true);
	}

	public TemporalSpatialContext(DoubleSupplier temporalScale, boolean spatialFrequency) {
		this.temporalScale = temporalScale;
		this.spatialFrequency = spatialFrequency;
		this.zoom = true;
	}

	public boolean isZoom() {
		return zoom;
	}

	public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public DoubleUnaryOperator getSecondsToTime() {
		return seconds -> seconds * temporalScale.getAsDouble();
	}

	public DoubleUnaryOperator getTimeToSeconds() {
		return time -> time / temporalScale.getAsDouble();
	}

	public double seconds(Vector position) {
		double pos = position.getX();
		return getTimeToSeconds().applyAsDouble(pos / getScale());
	}

	public Vector position(double time, double channel, double layer, double frequency) {
		return position(time, channel, layer, frequency, true);
	}

	public Vector position(double time, double channel, double layer, double frequency, boolean vertical) {
		double spacing;
		double y;
		double z;

		if (spatialFrequency) {
			y = frequency;
			z = layer;
			spacing = 200;
		} else {
			y = channel;
			z = layer;
			spacing = 10;
		}

		if (vertical) {
			return new Vector(time * getScale(), y * spacing, z * LAYER_SPACING);
		} else {
			return new Vector(time * getScale(), (z + 1) * spacing, (1 - y) * LAYER_SPACING);
		}
	}

	protected double getScale() {
		if (spatialFrequency) {
			if (zoom && duration > 0) {
				return Math.min(340 / duration, MAX_ZOOM);
			} else {
				return 3;
			}
		}

		return 3;
	}
}
