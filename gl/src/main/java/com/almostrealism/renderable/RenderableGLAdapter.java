/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.renderable;

import com.almostrealism.gl.GLPrintWriter;
import com.almostrealism.gl.GLMaterial;
import com.almostrealism.gl.TextureManager;
import com.almostrealism.gl.GLDriver;

import com.almostrealism.gl.shaders.FragmentShader;
import com.almostrealism.gl.shaders.GLDiffuseShader;
import com.almostrealism.gl.shaders.VertexShader;
import com.almostrealism.shade.Colored;
import io.almostrealism.code.CodePrintWriter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGBA;
import org.almostrealism.geometry.Oriented;
import org.almostrealism.geometry.Positioned;
import org.almostrealism.texture.ImageSource;

public abstract class RenderableGLAdapter implements Renderable, Positioned, Oriented, Colored {
	protected static final TextureManager textureManager = new TextureManager();
	
	private Vector position = new Vector(0.0, 0.0, 0.0);
	private double orientationAngle = 0.0;
	private Vector orientationVector = Vector.Z_AXIS;
	
	private GLMaterial mat;
	private ImageSource texture;

	private VertexShader vShader;
	private FragmentShader fShader;
	
	public RenderableGLAdapter() {
		mat = new GLMaterial();
		vShader = new GLDiffuseShader();
	}
	
	@Override
	public void init(GLDriver gl) {
		initTexture(gl);
		// TODO  Compile shaders
	}

	public void initTexture(GLDriver gl) {
		if (texture == null) return;
		textureManager.addTexture(gl, texture);
	}
	
	public void initMaterial(GLDriver gl) { }

	public void setVertexShader(VertexShader s) { this.vShader = s; }
	public void setFragmentShader(FragmentShader s) { this.fShader = s; }
	public VertexShader getVertexShader() { return vShader; }
	public FragmentShader getFragmentShader() { return fShader; }

	@Override
	public void display(GLDriver gl) {
		gl.glMaterial(mat);
		gl.setVertexShader(getVertexShader());
		gl.setFragmentShader(getFragmentShader());
	}

	/**
	 * This default implementation of {@link #write(String, String, CodePrintWriter)}
	 * uses a {@link GLPrintWriter} as the argument to {@link #display(GLDriver)}.
	 * Most subclasses will want to override this to provide a way of encoding the
	 * GL data that is sure to work properly in an external Open GL system.
	 *
	 * @param glMember  The name of the Open GL singleton Object.
	 * @param name  The name of this {@link Renderable} as it should appear in the exported code.
	 * @param p  The {@link CodePrintWriter} that will be wrapped by {@link GLPrintWriter}.
	 */
	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		display(new GLPrintWriter(glMember, "mat4", name, p)); // TODO  These may not be the right member names
	}
	
	public void push(GLDriver gl) {
		gl.pushMatrix();
		gl.glTranslate(position);
		gl.glRotate(orientationAngle, orientationVector);
		if (texture != null) textureManager.pushTexture(gl, texture);
	}
	
	public void pop(GLDriver gl) {
		if (texture != null) textureManager.popTexture(gl);
		gl.popMatrix();
	}
	
	@Override
	public void setPosition(float x, float y, float z) {
		position.setX(x);
		position.setY(y);
		position.setZ(z);
	}

	@Override
	public float[] getPosition() {
		return new float[] { (float) position.getX(), (float) position.getY(), (float) position.getZ() };
	}
	
	@Override
	public void setOrientation(float angle, float x, float y, float z) {
		orientationAngle = angle;
		orientationVector.setX(x);
		orientationVector.setY(y);
		orientationVector.setZ(z);
	}

	@Override
	public float[] getOrientation() {
		return new float[] { (float) orientationAngle,
							(float) orientationVector.getX(),
							(float) orientationVector.getY(),
							(float) orientationVector.getZ() };
	}
	
	public void setTexture(ImageSource tex) { this.texture = tex; }
	
	@Override
	public void setColor(float r, float g, float b, float a) {
		mat.diffuse = new RGBA(r, g, b, a);
	}

	@Override
	public float[] getColor() { return Scalar.toFloat(mat.diffuse.toArray()); }
	
	public void setMaterial(GLMaterial m) { this.mat = m; }
	public GLMaterial getMaterial() { return this.mat; }
}
