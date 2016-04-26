package com.almostrealism.visualize.gl;

import javax.media.opengl.GL2;

public class DisplayList extends RenderableGLAdapter {
	protected int displayListIndex;
	
	protected DisplayList() { }
	
	public DisplayList(int displayListIndex) {
		this.displayListIndex = displayListIndex;
	}

	@Override
	public void display(GL2 gl) {
		push(gl);
		gl.glCallList(displayListIndex);
		pop(gl);
	}
}
