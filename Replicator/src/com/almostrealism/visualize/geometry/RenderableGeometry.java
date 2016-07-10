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

package com.almostrealism.visualize.geometry;

import javax.media.opengl.GL2;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.visualize.renderable.RenderDelegate;
import com.almostrealism.visualize.renderable.Renderable;

public abstract class RenderableGeometry implements Renderable, RenderDelegate {
	private BasicGeometry geo;
	
	public RenderableGeometry(BasicGeometry geometry) { geo = geometry; }
	
	public BasicGeometry getGeometry() { return geo; }
	
	@Override
	public void display(GL2 gl) {
		gl.glPushMatrix();
		applyTransform(gl, geo);
		render(gl);
		gl.glPopMatrix();
	}
	
	public static void applyTransform(GL2 gl, BasicGeometry g) {
		// TODO Perform full transformation
		float p[] = g.getPosition();
		gl.glTranslatef(p[0], p[1], p[2]);
	}
}
