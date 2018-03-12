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

import com.almostrealism.gl.GLMaterial;
import org.almostrealism.space.BasicGeometry;

import com.jogamp.opengl.GL2;

public abstract class RenderableGeometry<T extends BasicGeometry> implements Renderable, RenderDelegate {
	private T geo;

	private GLMaterial mat;

	public RenderableGeometry(T geometry) { geo = geometry; mat = new GLMaterial(); }

	public T getGeometry() { return geo; }

	@Override
	public void display(GLDriver gl) {
		gl.glPushMatrix();
		applyTransform(gl, geo);
		gl.glMaterial(mat);
		render(gl);
		gl.glPopMatrix();
	}

	public void setMaterial(GLMaterial m) { this.mat = m; }
	public GLMaterial getMaterial() { return this.mat; }

	public static void applyTransform(GLDriver gl, BasicGeometry g) {
		// TODO Perform full transformation
		gl.glTranslate(g.getLocation());
	}
}
