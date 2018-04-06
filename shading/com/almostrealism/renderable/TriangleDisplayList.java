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
import com.jogamp.opengl.GL2;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import org.almostrealism.graph.Triangle;

@Deprecated
public class TriangleDisplayList extends GLDisplayList {
	private Iterable<Triangle> triangles;
	
	public TriangleDisplayList(Iterable<Triangle> t) { triangles = t; }
	
	public void init(GLDriver gl) {
		super.init(gl);
		gl.glNewList(displayListIndex, GL2.GL_COMPILE);
		gl.glBegin(GL2.GL_TRIANGLES);
		initMaterial(gl);
		
		for (Triangle t : triangles) {
			Vector v[] = t.getVertices();
			
			float tex[][] = t.getTextureCoordinates();

	        if (tex != null) gl.uv(new Pair(tex[0][0], tex[0][1]));
			gl.glVertex(v[0]);

	        if (tex != null) gl.uv(new Pair(tex[1][0], tex[1][1]));
			gl.glVertex(v[1]);

	        if (tex != null) gl.uv(new Pair(tex[2][0], tex[2][1]));
			gl.glVertex(v[2]);
		}
		
		gl.glEnd();
		gl.endList();
	}
}
