/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Genome;

public interface SpatialGenomic {
	Genome<PackedCollection> getGenome();
}
