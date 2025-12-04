/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import io.almostrealism.code.Constant;
import io.almostrealism.code.Operator;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.geometry.ContinuousField;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.AbstractSurface;

public class SpatialSurface extends AbstractSurface {
	private SpatialElement element;

	public SpatialSurface(SpatialElement element) {
		this.element = element;
	}

	public SpatialElement getElement() { return element; }

	@Override
	public Producer<PackedCollection> getNormalAt(Producer<PackedCollection> point) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ContinuousField intersectAt(Producer<?> ray) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Operator<PackedCollection> get() {
		return new Constant<>(pack(0));
	}

	@Override
	public Operator<PackedCollection> expect() {
		return new Constant<>(pack(1));
	}
}
