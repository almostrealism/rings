/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.algebra.Vector;

public class SpatialElement implements Spatial {
	private Vector position;

	public SpatialElement() { }

	public SpatialElement(Vector position) {
		this.position = position;
	}

	@Override
	public Vector getPosition() { return position; }
	public void setPosition(Vector position) { this.position = position; }
}
