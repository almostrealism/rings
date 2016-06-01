package com.almostrealism.visualize.geometry;

import javax.media.opengl.GL2;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.visualize.renderable.RenderDelegate;
import com.almostrealism.visualize.renderable.Renderable;

public abstract class RenderableGeometry implements Renderable, RenderDelegate {
	private BasicGeometry geo;
	
	public RenderableGeometry(BasicGeometry geometry) { geo = geometry; }
	
	@Override
	public void display(GL2 gl) {
		gl.glPushMatrix();
		applyTransform(gl, geo);
		render(gl);
		gl.glPopMatrix();
	}
	
	public static void applyTransform(GL2 gl, BasicGeometry g) {
		// TODO Perform full transformation
		float p[] = g.getPosition();
		gl.glTranslatef(p[0], p[1], p[2]);
	}
}
