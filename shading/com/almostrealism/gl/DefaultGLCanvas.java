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

package com.almostrealism.gl;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

import org.almostrealism.algebra.Vector;
import org.almostrealism.space.BasicGeometry;

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.renderable.Renderable;
import com.jogamp.newt.Window;
import com.jogamp.opengl.util.FPSAnimator;

public abstract class DefaultGLCanvas extends GLJPanel implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {
	private FPSAnimator animator;
	
	private float view_rotx = 20.0f, view_roty = 30.0f;
	private final float view_rotz = 0.0f;
	
	private int swapInterval;
	private boolean toReset;
	
	protected List<Renderable> renderables;
	
	private int prevMouseX, prevMouseY;
	
	public DefaultGLCanvas() {
		renderables = new ArrayList<Renderable>();
		
		animator = new FPSAnimator(20);
		animator.add(this);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.swapInterval = 1;
	}
	
	public void add(Renderable r) { if (r != null) renderables.add(r); }
	
	public void start() { animator.start(); }
	
	public void reset() { toReset = true; }
	
	public void removeAll() { renderables.clear(); }
	
	public abstract PinholeCamera getCamera();
	
	public void lookAt(BasicGeometry g) {
		float f[] = g.getPosition();
		getCamera().setViewingDirection(new Vector(f[0], f[1], f[2]));
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL2 gl = drawable.getGL().getGL2();

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
		
		float pos[] = { 5000.0f, 5000.0f, 5000.0f, 0.0f };
		
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_AUTO_NORMAL);
		
		initRenderables(gl);

		gl.glEnable(GL2.GL_NORMALIZE);
	}
	
	protected void initRenderables(GL2 gl) {
		for (Renderable r : renderables) r.init(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();

		gl.setSwapInterval(swapInterval);
		
		float h = (float) height / (float) width;
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
	}
	
	/**
	 * Configures the {@link GL2#GL_MODELVIEW} matrix to match the
	 * configuration of the {@link PinholeCamera} returned by the
	 * {@link #getCamera()} method.
	 */
	public void doView(GL2 gl) {
		PinholeCamera c = getCamera();
		Vector loc = c.getLocation();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef((float) loc.getX(), (float) loc.getY(), (float) loc.getZ());
	}
	
	/** Does nothing. */
	@Override
	public void dispose(GLAutoDrawable drawable) { }

	@Override
	public void display(GLAutoDrawable drawable) {
		// Get the GL corresponding to the drawable we are animating
		GL2 gl = drawable.getGL().getGL2();
		
		if (toReset) {
			initRenderables(gl);
			toReset = false;
		}
		
		doView(gl);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		if (GLProfile.isAWTAvailable() &&
				(drawable instanceof GLJPanel) &&
				!((GLJPanel) drawable).isOpaque() &&
				((GLJPanel) drawable).shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}
		
		// Rotate the entire assembly based on how the user
		// dragged the mouse around
		gl.glPushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
		
		for (Renderable r : renderables) {
			r.display(gl);
		}
		
		gl.glPopMatrix();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
		prevMouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseDragged(MouseEvent e) {
		final int x = e.getX();
		final int y = e.getY();
		float width = 0, height = 0;
		Object source = e.getSource();
		
		if (source instanceof Window) {
			Window window = (Window) source;
			width = window.getWidth();
			height = window.getHeight();
		} else if (source instanceof GLAutoDrawable) {
			GLAutoDrawable glad = (GLAutoDrawable) source;
			width = glad.getSurfaceWidth();
			height = glad.getSurfaceHeight();
		} else if (GLProfile.isAWTAvailable() && source instanceof java.awt.Component) {
			Component comp = (Component) source;
			width = comp.getWidth();
			height = comp.getHeight();
		} else {
			throw new RuntimeException("Event source neither Window nor Component: " + source);
		}
		
		float thetaY = 360.0f * ((x - prevMouseX) / width);
		float thetaX = 360.0f * ((prevMouseY - y) / height);
		
		prevMouseX = x;
		prevMouseY = y;

		view_rotx += thetaX;
		view_roty += thetaY;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int kc = e.getKeyCode();
		
		if (KeyEvent.VK_LEFT == kc) {
			view_roty -= 1;
		} else if(KeyEvent.VK_RIGHT == kc) {
			view_roty += 1;
		} else if(KeyEvent.VK_UP == kc) {
			view_rotx -= 1;
		} else if(KeyEvent.VK_DOWN == kc) {
			view_rotx += 1;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mouseMoved(MouseEvent e) { }
}