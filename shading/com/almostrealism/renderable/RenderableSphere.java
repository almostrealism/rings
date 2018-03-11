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

import com.almostrealism.gl.DisplayList;
import com.almostrealism.raytracer.primitives.Sphere;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * @author  Michael Murray
 */
public class RenderableSphere extends RenderableGeometry {
	protected DisplayList list;
	
	public RenderableSphere(Sphere s) {
		super(s);
		list = new DisplayList() {
			public void init(GLDriver gl) {
				super.init(gl);
				gl.glNewList(displayListIndex, GL2.GL_COMPILE);
				initMaterial(gl);
				gl.glutSolidSphere(1, 40, 40);
				gl.glEndList();
			}
		};
	}
	
	@Override
	public void init(GLDriver gl) { list.init(gl); }

	@Override
	public void render(GLDriver gl) { list.display(gl); }
}
