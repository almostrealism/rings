package com.almostrealism.replicator.geometry;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.engine.SurfaceGroup;

/**
 * A {@link Replicant} combines a set of {@link BasicGeometry}s
 * with a {@link Surface}. The resulting {@link SurfaceGroup} is
 * a collection of {@link Surface}s for each {@link BasicGeometry},
 * with the transformations of the {@link BasicGeometry} applied.
 * 
 * @author  Michael Murray
 */
public class Replicant extends SurfaceGroup {
	public Replicant(Iterable<BasicGeometry> n) {
		
	}
}
