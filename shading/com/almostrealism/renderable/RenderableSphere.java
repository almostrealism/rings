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

import com.almostrealism.gl.GLCodePrintWriter;
import com.almostrealism.gl.GLDriver;
import com.almostrealism.raytracer.primitives.Sphere;
import com.jogamp.opengl.GL2;

/**
 * @author  Michael Murray
 */
public class RenderableSphere extends RenderableGeometry {
	public static final int SLICES = 40;
	public static final int STACKS = 40;

	protected GLDisplayList list;
	
	public RenderableSphere(Sphere s) {
		super(s);
		list = new GLDisplayList() {
			public void init(GLDriver gl) {
				super.init(gl);
				gl.glNewList(displayListIndex, GL2.GL_COMPILE);
				initMaterial(gl);
				gl.glutSolidSphere(s.getSize(), SLICES, STACKS);
				gl.glEndList();
			}
		};
	}
	
	@Override
	public void init(GLDriver gl) { list.init(gl); }

	@Override
	public void render(GLDriver gl) {
		if (gl instanceof GLCodePrintWriter) {
			// Display lists are not supported by external OpenGL systems
			list.initMaterial(gl);
			gl.glutSolidSphere(getGeometry().getSize(), SLICES, STACKS);
		} else {
			list.display(gl);
		}
	}
}
