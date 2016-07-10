/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
