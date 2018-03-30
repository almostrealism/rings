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
import io.almostrealism.code.CodePrintWriter;
import io.almostrealism.code.ResourceVariable;
import org.almostrealism.graph.Mesh;

import org.almostrealism.graph.MeshResource;

public class RenderableMesh extends RenderableGeometry<Mesh> {
	protected TriangleDisplayList list;
	
	public RenderableMesh(Mesh m) {
		super(m);
		list = createDisplayList(m);
	}
	
	@Override
	public void init(GLDriver gl) { list.init(gl); }

	@Override
	public void render(GLDriver gl) { list.display(gl); }

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		ResourceVariable v = new ResourceVariable(name + "Mesh", new MeshResource(getGeometry()));
		p.println(v);
		// TODO  Render mesh
	}

	private static TriangleDisplayList createDisplayList(Mesh m) {
		return new TriangleDisplayList(m.triangles());
	}
}
