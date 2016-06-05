package com.almostrealism.visualize.primitives;

import javax.media.opengl.GL2;

import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.visualize.geometry.RenderableGeometry;

public class RenderableMesh extends RenderableGeometry {
	protected TriangleDisplayList list;
	
	public RenderableMesh(Mesh m) {
		super(m);
		list = createDisplayList(m);
	}
	
	@Override
	public void init(GL2 gl) { list.init(gl); }
	
	public void render(GL2 gl) { list.display(gl); }
	
	private static TriangleDisplayList createDisplayList(Mesh m) {
		return new TriangleDisplayList(m);
	}
}
