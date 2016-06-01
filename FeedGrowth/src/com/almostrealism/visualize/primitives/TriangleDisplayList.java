package com.almostrealism.visualize.primitives;

import javax.media.opengl.GL2;

import net.sf.j3d.util.Vector;

import com.almostrealism.raytracer.primitives.Triangle;
import com.almostrealism.visualize.gl.DisplayList;

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
