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

package com.almostrealism.gl.models;

import com.almostrealism.renderable.GLDriver;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLUnurbs;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.almostrealism.renderable.RenderableList;

public class NurbsMoleHill extends RenderableList {
	private GLUgl2 glu;
	
	float knots[] = { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f };
	float pts1[][][] = new float[4][4][3];
	float pts2[][][] = new float[4][4][3];
	float pts3[][][] = new float[4][4][3];
	float pts4[][][] = new float[4][4][3];

	GLUnurbs nurb;

	int u, v;
	
	public NurbsMoleHill() {
		glu = new GLUgl2();
		nurb = glu.gluNewNurbsRenderer();
		
		buildNurbs();
	}
	
	protected void buildNurbs() {
		/* Build control points for NURBS mole hills. */
		for (u=0; u<4; u++) {
			for(v=0; v<4; v++) {
				/* Red. */
				pts1[u][v][0] = 2f * u;
				pts1[u][v][1] = 2f * v;
				if((u==1 || u == 2) && (v == 1 || v == 2))
					/* Stretch up middle. */
					pts1[u][v][2] = 6.0f;
				else
					pts1[u][v][2] = 0.0f;

				/* Green. */
				pts2[u][v][0] = 2f * (u - 3.0f);
				pts2[u][v][1] = 2f * (v - 3.0f);
				if((u==1 || u == 2) && (v == 1 || v == 2))
					if(u == 1 && v == 1) 
						/* Pull hard on single middle square. */
						pts2[u][v][2] = 15.0f;
					else
						/* Push down on other middle squares. */
						pts2[u][v][2] = -2.0f;
				else
					pts2[u][v][2] = 0.0f;

				/* Blue. */
				pts3[u][v][0] = 2f * (u - 3.0f);
				pts3[u][v][1] = 2f * (v - 3.0f);
				if ((u==1 || u == 2) && (v == 1 || v == 2))
					if(u == 1 && v == 2)
						/* Pull up on single middple square. */
						pts3[u][v][2] = 11.0f;
					else
						/* Pull up slightly on other middle squares. */
						pts3[u][v][2] = 2.0f;
				else
					pts3[u][v][2] = 0.0f;

				/* Yellow. */
				pts4[u][v][0] = 2f * u;
				pts4[u][v][1] = 2f * (v - 3f);
				if ((u==1 || u == 2 || u == 3) && (v == 1 || v == 2))
					if(v == 1) 
						/* Push down front middle and right squares. */
						pts4[u][v][2] = -2.0f;
					else
						/* Pull up back middle and right squares. */
						pts4[u][v][2] = 5.0f;
				else
					pts4[u][v][2] = 0.0f;
			}
		}
		/* Stretch up red's far right corner. */
		pts1[3][3][2] = 6;
		/* Pull down green's near left corner a little. */
		pts2[0][0][2] = -2;
		/* Turn up meeting of four corners. */
		pts1[0][0][2] = 1;
		pts2[3][3][2] = 1;
		pts3[3][0][2] = 1;
		pts4[0][3][2] = 1;
		
		NurbsSurface n1 = new NurbsSurface(knots, pts1, glu, nurb);
		n1.getMaterial().setDiffuse(0.7f, 0.0f, 0.1f, 1.0f);
		add(n1);
		
		NurbsSurface n2 = new NurbsSurface(knots, pts2, glu, nurb);
		n2.getMaterial().setDiffuse(0.0f, 0.7f, 0.1f, 1.0f);
		add(n2);
		
		NurbsSurface n3 = new NurbsSurface(knots, pts3, glu, nurb);
		n3.getMaterial().setDiffuse(0.0f, 0.1f, 0.7f, 1.0f);
		add(n3);
		
		NurbsSurface n4 = new NurbsSurface(knots, pts4, glu, nurb);
		n4.getMaterial().setDiffuse(0.7f, 0.8f, 0.1f, 1.0f);
		add(n4);
	}
	
	public void init(GLDriver gl) {
//		glu.gluNurbsProperty(nurb, GLUgl2.GLU_SAMPLING_TOLERANCE, 25.0);
//		glu.gluNurbsProperty(nurb, GLUgl2.GLU_DISPLAY_MODE, GLUgl2.GLU_FILL);
		
		super.init(gl);
	}
}
