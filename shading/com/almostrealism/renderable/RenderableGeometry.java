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

import com.almostrealism.gl.GLPrintWriter;
import com.almostrealism.gl.GLDriver;
import com.almostrealism.gl.GLMaterial;
import io.almostrealism.code.CodePrintWriter;
import org.almostrealism.geometry.BasicGeometry;

public abstract class RenderableGeometry<T extends BasicGeometry> extends RenderableGLAdapter 
						implements RenderDelegate {
	private T geo;

	public RenderableGeometry(T geometry) { geo = geometry; }

	public T getGeometry() { return geo; }

	@Override
	public void display(GLDriver gl) {
		gl.pushMatrix();
		applyTransform(gl, geo);
		gl.pushShaders();
		super.display(gl);
		gl.popShaders();
		render(gl);
		gl.popMatrix();
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		display(new GLPrintWriter(glMember, (String)null, name, p)); // TODO  These may not be the right member names
	}

	public static void applyTransform(GLDriver gl, BasicGeometry g) {
		gl.setMatrix(g.getTransform(true));
	}

	public String toString() { return "RenderableGeometry[" + getGeometry().toString() + "]"; }
}
