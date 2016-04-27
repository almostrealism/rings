package com.almostrealism.visualize.nurbs;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

public class AnimatedNurbsSurface extends NurbsSurface {
	private static final float defaultKnots[] = { 0.0f, 0.0f, 0.0f, 0.0f,
												0.0f, 0.0f, 0.0f, 0.0f,
												1.0f, 1.0f, 1.0f, 1.0f,
												1.0f, 1.0f, 1.0f, 1.0f };
	
	private int width, height;
	private float[][][] points = new float[8][8][3];
	
	public AnimatedNurbsSurface(int w, int h, GLUgl2 glu, GLUnurbs nurbs) {
		super(defaultKnots, null, glu, nurbs);
		
		width = w;
		height = h;
		
		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
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
	
	public int getWidth() { return width; }
	
	public int getHeight() { return height; }
	
	public float[][][] getPoints() {
		for (int u = 0; u < width; u++) {
			for (int v=0; v < height; v++) {
				points[u][v][2] = points[u][v][2] + 0.5f * (float) (Math.random() - 0.5);
			}
		}
		
		return points;
	}
}
