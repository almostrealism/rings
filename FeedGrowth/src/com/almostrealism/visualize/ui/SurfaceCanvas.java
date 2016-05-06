package com.almostrealism.visualize.ui;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.visualize.primitives.RenderableSurfaceFactory;
import com.almostrealism.visualize.renderable.Renderable;

public class SurfaceCanvas extends DefaultGLCanvas {
	public SurfaceCanvas() {
		
	}
	
	public void add(Surface s) {
		addSurface(s);
	}
	
	public void addSurface(Surface s) {
		Renderable r = RenderableSurfaceFactory.createRenderableSurface(s);
	}
}
