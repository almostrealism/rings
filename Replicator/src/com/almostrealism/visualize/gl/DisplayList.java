package com.almostrealism.visualize.gl;

import javax.media.opengl.GL2;

public class DisplayList extends RenderableGLAdapter {
	protected int displayListIndex;
	
	protected DisplayList() { }
	
	public DisplayList(int displayListIndex) {
		this.displayListIndex = displayListIndex;
	}
	
	public void init(GL2 gl) {
		super.init(gl);
		displayListIndex = gl.glGenLists(1);
	}
	
	@Override
	public void display(GL2 gl) {
		push(gl);
		gl.glCallList(displayListIndex);
		pop(gl);
	}
}
