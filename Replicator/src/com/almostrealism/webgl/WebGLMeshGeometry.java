package com.almostrealism.webgl;

import com.almostrealism.html.HTMLContent;
import com.almostrealism.raytracer.primitives.Mesh;

public class WebGLMeshGeometry implements WebGLExportable {
	private Mesh.VertexData data;
	
	public WebGLMeshGeometry(Mesh.VertexData data) {
		
	}
	
	@Override
	public HTMLContent getWebGLContent() {
		return null;
	}
}
