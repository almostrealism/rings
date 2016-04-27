package com.almostrealism.visualize.nurbs;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

public class AnimatedNurbsSurface extends NurbsSurface {
	private static final float defaultKnots[] = { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f };
	
	private float[][][] points = new float[4][4][3];
	
	public AnimatedNurbsSurface(GLUgl2 glu, GLUnurbs nurbs) {
		super(defaultKnots, null, glu, nurbs);
		
		for (int u = 0; u < 4; u++) {
			for (int v=0; v < 4; v++) {
				/* Red. */
				points[u][v][0] = 2f * u;
				points[u][v][1] = 2f * v;
				
				if ((u==1 || u == 2) && (v == 1 || v == 2))
					/* Stretch up middle. */
					points[u][v][2] = 0.0f;
				else
					points[u][v][2] = 0.0f;
			}
		}
	}
	
	public void display(GL2 gl) {
		buildSurface(gl);
		super.display(gl);
	}
	
	public float[][][] getPoints() {
		for (int u = 0; u < 4; u++) {
			for (int v=0; v < 4; v++) {
				points[u][v][2] = points[u][v][2] + 0.5f * (float) (Math.random() - 0.5);
			}
		}
		
		return points;
	}
}
