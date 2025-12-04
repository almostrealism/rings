/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

public interface SpatialDataListener<T extends SpatialTimeseries> {
	void published(SpatialData<T> data);

	default void scan(double time) { }
}
