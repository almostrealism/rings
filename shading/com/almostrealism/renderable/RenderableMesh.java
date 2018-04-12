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

import com.almostrealism.gl.GLDriver;
import com.almostrealism.gl.GLMaterial;
import com.jogamp.opengl.GL2;
import io.almostrealism.code.CodePrintWriter;
import io.almostrealism.code.ResourceVariable;
import org.almostrealism.graph.Mesh;

import org.almostrealism.graph.MeshResource;
import org.almostrealism.graph.Triangle;

public class RenderableMesh extends RenderableGeometry<Mesh> {
	public RenderableMesh(Mesh m) { super(m); }
	
	@Override
	public void init(GLDriver gl) { } // TODO  Load mesh vertices into a vertex buffer object

	@Override
	public void render(GLDriver gl) {
		gl.glMaterial(getMaterial());

		gl.glBegin(GL2.GL_TRIANGLES);

		for (Triangle t : getGeometry().triangles()) {
			gl.triangle(t);
		}

		gl.glEnd();
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		ResourceVariable v = new ResourceVariable(name + "Mesh", new MeshResource(getGeometry()));
		p.println(v);
		// TODO  Render mesh
	}
}
