package com.almostrealism.visualize.renderable;

import javax.media.opengl.GL2;

public interface Renderable {
	public void init(GL2 gl);
	
	public void display(GL2 drawable);
}
