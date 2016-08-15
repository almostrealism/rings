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
