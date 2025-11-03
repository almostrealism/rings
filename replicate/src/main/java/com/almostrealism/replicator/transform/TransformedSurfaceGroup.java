package com.almostrealism.replicator.transform;

import org.almostrealism.geometry.TransformMatrix;
import org.almostrealism.geometry.BasicGeometry;
import org.almostrealism.color.ShadableSurface;
import org.almostrealism.space.SurfaceGroup;

// TODO  Move to Common
public class TransformedSurfaceGroup<T extends ShadableSurface> extends SurfaceGroup<T> {
	private BasicGeometry geo;

	public TransformedSurfaceGroup(BasicGeometry g) { geo = g; }

	@Override
	public TransformMatrix getTransform(boolean include) {
		TransformMatrix m = super.getTransform(include);

		if (m == null) {
			return geo.getTransform(include);
		} else {
			// TODO  Is this reversed?
			return geo.getTransform(include).multiply(super.getTransform(include));
		}
	}
}
