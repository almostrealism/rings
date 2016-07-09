package com.almostrealism.webgl;

import com.almostrealism.html.HTMLFragment;
import com.almostrealism.html.HTMLPage;

public class WebGLExporter {
	private Iterable<WebGLRenderable> renderables;
	
	public WebGLExporter(Iterable<WebGLRenderable> renderables) {
		this.renderables = renderables;
	}
	
	public HTMLPage render() {
		HTMLPage p = new HTMLPage();
		
		// TODO  Camera config, etc
		
		HTMLFragment geometry = new HTMLFragment(HTMLFragment.Type.SCRIPT);
		int index = 0;
		
		for (WebGLRenderable r : renderables) {
			geometry.add(r.getWebGLGeometry().getWebGLContent());
			geometry.add(r.getWebGLMaterial().getWebGLContent());
			
			final String mesh = "renderable" + index;
			
			geometry.add(() -> "var " + mesh + " = new THREE.Mesh( geometry, material );");
			geometry.add(() -> "scene.add(" + mesh + ")");
			
			index++;
		}
		
		p.add(geometry);
		
		return p;
	}
}
