package com.almostrealism.visualize.nurbs;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.almostrealism.visualize.gl.DisplayList;

public class NurbsSurface extends DisplayList {
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
	public void init(GL2 gl) {
		super.init(gl);
		
		if (pts != null) { buildSurface(gl); }
	}
	
	public int getWidth() { return pts.length; }
	public int getHeight() { return pts[0].length; }
	
	public float[][][] getPoints() { return pts; }
	
	protected void buildSurface(GL2 gl) {
		gl.glNewList(displayListIndex, GL2.GL_COMPILE);
		initMaterial(gl);
		glu.gluBeginSurface(nurbs);
		glu.gluNurbsSurface(nurbs, knots.length, knots,
							knots.length, knots,
							getWidth() * 3, 3, flatten(getPoints()),
							getWidth(), getHeight(),
							GL2.GL_MAP2_VERTEX_3);
		glu.gluEndSurface(nurbs);
		gl.glEndList();
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
