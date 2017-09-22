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

import org.almostrealism.algebra.Vector;

import com.almostrealism.gl.DisplayList;
import com.almostrealism.raytracer.primitives.Triangle;

public class TriangleDisplayList extends DisplayList {
	private Iterable<Triangle> triangles;
	
	public TriangleDisplayList(Iterable<Triangle> t) { triangles = t; }
	
	public void init(GL2 gl) {
		super.init(gl);
		gl.glNewList(displayListIndex, GL2.GL_COMPILE);
		gl.glBegin(GL2.GL_TRIANGLES);
		initMaterial(gl);
		
		for (Triangle t : triangles) {
			Vector v[] = t.getVertices();
			
			float tex[][] = t.getTextureCoordinates();
			
			float f[] = v[0].toFloat();
	        if (tex != null) gl.glTexCoord2f(tex[0][0], tex[0][1]);
			gl.glVertex3f(f[0], f[1], f[2]);
			
			f = v[1].toFloat();
	        if (tex != null) gl.glTexCoord2f(tex[1][0], tex[1][1]);
			gl.glVertex3f(f[0], f[1], f[2]);
			
			f = v[2].toFloat();
	        if (tex != null) gl.glTexCoord2f(tex[2][0], tex[2][1]);
			gl.glVertex3f(f[0], f[1], f[2]);
		}
		
		gl.glEnd();
		gl.glEndList();
	}
}
