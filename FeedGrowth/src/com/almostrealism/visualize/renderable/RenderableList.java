package com.almostrealism.visualize.renderable;

import java.util.ArrayList;

import javax.media.opengl.GL2;

public class RenderableList extends ArrayList<Renderable> implements Renderable {
	@Override
	public void init(GL2 gl) {
		for (Renderable r : this) r.init(gl);
	}
	
	@Override
	public void display(GL2 gl) {
		for (Renderable r : this) r.display(gl);
	}
}
