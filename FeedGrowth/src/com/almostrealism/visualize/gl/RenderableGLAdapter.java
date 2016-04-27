package com.almostrealism.visualize.gl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL2;

import com.almostrealism.visualize.renderable.Colored;
import com.almostrealism.visualize.renderable.Oriented;
import com.almostrealism.visualize.renderable.Positioned;
import com.almostrealism.visualize.renderable.Renderable;
import com.almostrealism.visualize.shading.Diffuse;
import com.almostrealism.visualize.shading.Specular;

public abstract class RenderableGLAdapter implements Renderable, Positioned, Oriented, Colored, Diffuse, Specular {
	private float position[] = { 0.0f, 0.0f, 0.0f };
	private float orientation[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	
	private float color[] = { 0.5f, 0.5f, 0.5f, 0.5f };
	private float diffuse[] = {0.0f, 0.0f, 0.0f, 0.0f };
	private float specular[] = { 0.0f, 0.0f, 0.0f, 0.0f };
	private float shininess = 0.0f;
	
	private boolean ambient;
	
	public RenderableGLAdapter() {
		ambient = true;
	}
	
	@Override
	public void init(GL2 gl) { }
	
	public void initMaterial(GL2 gl) {
		if (ambient) {
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0);
		} else {
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, FloatBuffer.wrap(diffuse));
		}
		
		if (shininess > 0.0f) {
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, FloatBuffer.wrap(specular));
			gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, shininess);
		}
	}
	
	public void push(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslatef(position[0], position[1], position[2]);
		gl.glRotatef(orientation[0], orientation[1], orientation[2], orientation[3]);
	}
	
	public void pop(GL2 gl) {
		gl.glPopMatrix();
	}
	
	@Override
	public void setPosition(float x, float y, float z) { position = new float[] { x, y, z }; }

	@Override
	public float[] getPosition() { return position; }
	
	@Override
	public void setOrientation(float angle, float x, float y, float z) { orientation = new float[] { angle, x, y, z }; }

	@Override
	public float[] getOrientation() { return orientation; }
	
	@Override
	public void setColor(float r, float g, float b, float a) { color = new float[] { r, g, b, a }; }

	@Override
	public float[] getColor() { return color; }
	
	public void setAmbient(boolean a) { ambient = a; }
	
	@Override
	public void setDiffuse(float r, float g, float b, float a) { specular = new float[] { r, g, b, a}; }
	
	@Override
	public float[] getDiffuse() { return diffuse; }
	
	@Override
	public void setSpecular(float r, float g, float b, float a) { specular = new float[] { r, g, b, a}; }

	@Override
	public float[] getSpecular() { return specular; }

	@Override
	public void setShininess(float s) { shininess = s; }

	@Override
	public float getShininess() { return shininess; }
}
