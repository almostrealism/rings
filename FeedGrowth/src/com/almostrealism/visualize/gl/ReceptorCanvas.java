package com.almostrealism.visualize.gl;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;

import com.jogamp.opengl.util.FPSAnimator;

public class ReceptorCanvas extends GLJPanel implements GLEventListener {
	private FPSAnimator animator;
	
	public ReceptorCanvas() {
		animator = new FPSAnimator(this, 60);
		addGLEventListener(this);
	}

	@Override
	public void init(GLAutoDrawable d) {
	}
	
	@Override
	public void display(GLAutoDrawable d) {
	}

	@Override
	public void dispose(GLAutoDrawable d) {
	}
	
	@Override
	public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {
	}
}
