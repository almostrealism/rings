package com.almostrealism.visualize.gl;

import javax.media.opengl.GL2;

import com.almostrealism.visualize.renderable.RenderableList;

public class RenderableGLList extends RenderableGLAdapter {
	private RenderableList renderables;
	
	public RenderableGLList() { this(new RenderableList()); }
	
	public RenderableGLList(RenderableList r) { this.renderables = r; }
	
	@Override
	public void init(GL2 gl) { super.init(gl); renderables.init(gl); }
	
	@Override
	public void display(GL2 gl) {
		push(gl);
		renderables.display(gl);
		pop(gl);
	}
}
