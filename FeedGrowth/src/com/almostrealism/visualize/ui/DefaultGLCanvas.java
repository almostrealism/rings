package com.almostrealism.visualize.ui;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.almostrealism.visualize.renderable.Renderable;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.opengl.util.FPSAnimator;

public class DefaultGLCanvas extends GLJPanel implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {
	private FPSAnimator animator;
	
	private float view_rotx = 20.0f, view_roty = 30.0f;
	private final float view_rotz = 0.0f;
	
	private int swapInterval;
	
	private List<Renderable> scene;
	
	private int prevMouseX, prevMouseY;
	
	public DefaultGLCanvas() {
		scene = new ArrayList<Renderable>();
		
		animator = new FPSAnimator(this, 60);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.swapInterval = 1;
	}
	
	public void add(Renderable r) { scene.add(r); }
	
	public void start() { animator.start(); }
	
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

		float pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };

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
		for (Renderable r : scene) r.init(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();

		gl.setSwapInterval(swapInterval);
		
		float h = (float) height / (float) width;
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		
		new GLUgl2().gluPerspective(55.0f, 1.0f, 2.0f, 24.0f);

		gl.glLoadIdentity();
		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -40.0f);
		gl.glRotatef(330.0f, 1.0f, 0.0f, 0.0f);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) { }

	@Override
	public void display(GLAutoDrawable drawable) {
		// Get the GL corresponding to the drawable we are animating
		GL2 gl = drawable.getGL().getGL2();

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

		// Rotate the entire assembly of gears based on how the user
		// dragged the mouse around
		gl.glPushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

		for (Renderable r : scene) r.display(gl);

		// Remember that every push needs a pop; this one is paired with
		// rotating the entire gear assembly
		gl.glPopMatrix();
	}

	class GearsKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
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
	}

	class GearsMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
			prevMouseX = e.getX();
			prevMouseY = e.getY();
		}

		@Override
		public void mouseReleased(com.jogamp.newt.event.MouseEvent e) { }

		@Override
		public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {
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
				width = glad.getWidth();
				height = glad.getHeight();
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
			width = glad.getWidth();
			height = glad.getHeight();
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
