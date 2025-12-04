/*
 * Copyright 2023 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

public interface SpatialSelectionListener<T extends SpatialTimeseries> {
	void selected(T info);
}
