package com.almostrealism.webgl;

// TODO  Add material
public interface WebGLRenderable {
	public WebGLExportable getWebGLGeometry();
	
	public WebGLExportable getWebGLMaterial();
}
