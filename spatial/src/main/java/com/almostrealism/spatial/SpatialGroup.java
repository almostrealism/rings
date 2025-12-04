/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.algebra.Vector;

import java.util.ArrayList;
import java.util.List;

public class SpatialGroup extends SpatialElement {
	private List<SpatialElement> elements;

	public SpatialGroup() {
		this(new Vector());
	}

	public SpatialGroup(Vector position) {
		super(position);
		elements = new ArrayList<>();
	}

	public List<SpatialElement> getElements() {
		return elements;
	}

	public void setElements(List<SpatialElement> elements) {
		this.elements = elements;
	}
}
