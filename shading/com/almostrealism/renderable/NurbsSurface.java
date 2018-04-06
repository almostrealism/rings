/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLUnurbs;
import com.jogamp.opengl.glu.gl2.GLUgl2;

// TODO  GLDisplayList is deprecated
public class NurbsSurface extends GLDisplayList {
	private float knots[];
	private float pts[][][];
	private GLUgl2 glu;
	private GLUnurbs nurbs;
	
	public NurbsSurface(float knots[], float pts[][][], GLUgl2 glu, GLUnurbs nurbs) {
		this.knots = knots;
		this.pts = pts;
		this.nurbs = nurbs;
		this.glu = glu;
	}
	
	@Override
	public void init(GLDriver gl) {
		super.init(gl);
		
		if (pts != null) { buildSurface(gl); }
	}
	
	public int getWidth() { return pts.length; }
	public int getHeight() { return pts[0].length; }
	
	public float[][][] getPoints() { return pts; }
	
	protected void buildSurface(GLDriver gl) {
		gl.glNewList(displayListIndex, GL2.GL_COMPILE);
		initMaterial(gl);
		glu.gluBeginSurface(nurbs);
		glu.gluNurbsSurface(nurbs, knots.length, knots,
							knots.length, knots,
							getWidth() * 3, 3, flatten(getPoints()),
							getWidth(), getHeight(),
							GL2.GL_MAP2_VERTEX_3);
		glu.gluEndSurface(nurbs);
		gl.endList();
	}
	
	protected float[] flatten(float f[][][]) {
		float flat[] = new float[f.length * f[0].length * f[0][0].length];
		int index = 0;
		
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				for (int k = 0; k < f[0][0].length; k++) {
					flat[index++] = f[i][j][k];
				}
			}
		}
		
		return flat;
	}
}
