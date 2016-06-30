package com.almostrealism.webgl;

import com.almostrealism.html.HTMLFragment;
import com.almostrealism.html.HTMLPage;

public class WebGLExporter {
	private Iterable<WebGLRenderable> renderables;
	
	public WebGLExporter(Iterable<WebGLRenderable> renderables) {
		this.renderables = renderables;
	}
	
	public String render() {
		HTMLPage p = new HTMLPage();
		
		// TODO  Camera config, etc
		
		HTMLFragment geometry = new HTMLFragment();
		
		for (WebGLRenderable r : renderables) {
			geometry.add(r.getWebGLGeometry().getWebGLContent());
			geometry.add(r.getWebGLMaterial().getWebGLContent());
		}
		
		p.add(geometry);
		
		return p.toHTML();
	}
}
