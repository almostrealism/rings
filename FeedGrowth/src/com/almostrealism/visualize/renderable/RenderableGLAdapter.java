package com.almostrealism.visualize.renderable;

import javax.media.opengl.GL2;

public abstract class RenderableGLAdapter implements Renderable, Positioned, Oriented, Colored {
	protected float position[] = { 0.0f, 0.0f, 0.0f };
	protected float orientation[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	protected float color[] = { 0.5f, 0.5f, 0.5f, 0.5f };

	@Override
	public void init(GL2 gl) { }
	
	@Override
	public void setPosition(float x, float y, float z) {
		position = new float[] { x, y, z };
	}

	@Override
	public float[] getPosition() { return position; }
	
	@Override
	public void setOrientation(float angle, float x, float y, float z) {
		orientation = new float[] { angle, x, y, z };
	}

	@Override
	public float[] getOrientation() { return orientation; }
	
	@Override
	public void setColor(float r, float g, float b, float a) {
		color = new float[] { r, g, b, a };
	}

	@Override
	public float[] getColor() { return color; }
	
	public void push(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslatef(position[0], position[1], position[2]);
		gl.glRotatef(orientation[0], orientation[1], orientation[2], orientation[3]);
	}
	
	public void pop(GL2 gl) {
		gl.glPopMatrix();
	}
}
