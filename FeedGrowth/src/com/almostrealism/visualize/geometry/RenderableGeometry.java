package com.almostrealism.visualize.geometry;

import javax.media.opengl.GL2;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.visualize.renderable.RenderDelegate;
import com.almostrealism.visualize.renderable.Renderable;

public abstract class RenderableGeometry implements Renderable, RenderDelegate {
	private BasicGeometry geo;
	
	public RenderableGeometry(BasicGeometry geometry) {
		geo = geometry;
	}
	
	@Override
	public void display(GL2 gl) {
		// TODO Perform transformations
		render(gl);
	}
}
