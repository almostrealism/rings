/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import java.util.List;

public interface SpatialTimeseries {
	String getKey();

	List<SpatialValue> elements(TemporalSpatialContext context);

	double getDuration(TemporalSpatialContext context);

	boolean isEmpty();
}
