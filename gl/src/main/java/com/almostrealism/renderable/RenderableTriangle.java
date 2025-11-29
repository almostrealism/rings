package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import com.jogamp.opengl.GL2;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Triangle;

public class RenderableTriangle extends RenderableGeometry<Triangle> {
	private RGB a, b, c;

	public RenderableTriangle(Triangle t) {
		super(t);
	}

	public void setVertexColors(RGB a, RGB b, RGB c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public void init(GLDriver gl) { }  //load triangle vertices here?

	@Override
	public void render(GLDriver gl) {
		Vector[] v = getGeometry().getVertices();

		gl.glBegin(GL2.GL_TRIANGLES);
		gl.glColor(a);
		gl.glVertex(v[0]);
		gl.glColor(b);
		gl.glVertex(v[1]);
		gl.glColor(c);
		gl.glVertex(v[2]);
		gl.glEnd();
	}
}
