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

package com.almostrealism.renderable;

import com.jogamp.opengl.GL2;

import com.almostrealism.raytracer.primitives.Mesh;
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
	
	@Override
	public WebGLExportable getWebGLMaterial() {
		throw new RuntimeException("Not implemented");
	}
	
	private static TriangleDisplayList createDisplayList(Mesh m) {
		return new TriangleDisplayList(m);
	}
}
