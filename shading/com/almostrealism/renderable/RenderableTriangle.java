package com.almostrealism.renderable;

import com.jogamp.opengl.GL2;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.graph.Triangle;

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
	public void init(GL2 gl) { }

	@Override
	public void render(GL2 gl) {
		GLDriver d = new GLDriver(gl);

		Vector v[] = getGeometry().getVertices();

		gl.glBegin(GL2.GL_TRIANGLES);
		d.glColor(a);
		d.glVertex(v[0]);
		d.glColor(b);
		d.glVertex(v[1]);
		d.glColor(c);
		d.glVertex(v[2]);
		gl.glEnd();
	}
}
