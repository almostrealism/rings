package com.almostrealism.webgl;

// TODO  Add parameters for customizing camera
public class WebGLCameraConfig extends WebGLStatement {
	public WebGLCameraConfig() { }
	
	public String toHTML() {
		return "var camera = new THREE.PerspectiveCamera(75, window.innerWidth/window.innerHeight, 0.1, 1000);";
	}
}
