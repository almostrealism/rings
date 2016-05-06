package com.almostrealism.visualize.ui;

import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.almostrealism.visualize.gl.RenderableGLList;
import com.almostrealism.visualize.models.NurbsMoleHill;
import com.almostrealism.visualize.nurbs.AnimatedNurbsSurface;

public class ReceptorCanvas extends DefaultGLCanvas {
	public ReceptorCanvas() {
		NurbsMoleHill m = new NurbsMoleHill();
		RenderableGLList l = new RenderableGLList(m);
		l.setSpecular(1.0f, 1.0f, 1.0f, 1.0f);
		l.setShininess(100);
//		add(l);

		GLUgl2 glu = new GLUgl2();
		GLUnurbs nurbs = glu.gluNewNurbsRenderer();
		
		AnimatedNurbsSurface n = new AnimatedNurbsSurface(8, 8, glu, nurbs);
		n.setPosition(-6.0f, -5.0f, 0.0f);
		n.setDiffuse(0.7f, 0.0f, 0.1f, 1.0f);
		
		add(n);
	}
}
