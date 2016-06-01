package com.almostrealism.visualize.ui;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.visualize.primitives.RenderableSurfaceFactory;

public class SurfaceCanvas extends DefaultGLCanvas {
	public SurfaceCanvas() {
		
	}
	
	public void add(Surface s) {
		addSurface(s);
	}
	
	public void addSurface(Surface s) {
		super.add(RenderableSurfaceFactory.createRenderableSurface(s));
	}
}
