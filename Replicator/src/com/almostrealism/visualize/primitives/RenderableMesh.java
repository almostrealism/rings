package com.almostrealism.visualize.primitives;

import javax.media.opengl.GL2;

import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.visualize.geometry.RenderableGeometry;
import com.almostrealism.webgl.WebGLExportable;
import com.almostrealism.webgl.WebGLMeshGeometry;
import com.almostrealism.webgl.WebGLRenderable;

public class RenderableMesh extends RenderableGeometry implements WebGLRenderable {
	protected TriangleDisplayList list;
	
	public RenderableMesh(Mesh m) {
		super(m);
		list = createDisplayList(m);
	}
	
	@Override
	public void init(GL2 gl) { list.init(gl); }
	
	public void render(GL2 gl) { list.display(gl); }
	
	public WebGLExportable getWebGLGeometry() {
		return new WebGLMeshGeometry(((Mesh) getGeometry()).getVertexData());
	}
	
	private static TriangleDisplayList createDisplayList(Mesh m) {
		return new TriangleDisplayList(m);
	}
}
