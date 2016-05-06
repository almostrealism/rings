package com.almostrealism.visualize.primitives;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.visualize.renderable.Renderable;

public class RenderableSurfaceFactory {
	public static Renderable createRenderableSurface(Surface s) {
		if (s instanceof Renderable) {
			return (Renderable) s;
		} else if (s instanceof Mesh) {
			return new RenderableMesh((Mesh) s);
		}
		
		return null;
	}
}
