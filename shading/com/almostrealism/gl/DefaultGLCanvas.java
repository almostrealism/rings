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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.almostrealism.renderable.GLDriver;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

import com.jogamp.opengl.math.FixedPoint;
import com.jogamp.opengl.util.GLBuffers;
import org.almostrealism.algebra.Vector;
import org.almostrealism.space.BasicGeometry;

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.renderable.Renderable;
import com.jogamp.newt.Window;
import com.jogamp.opengl.util.FPSAnimator;

public abstract class DefaultGLCanvas extends GLJPanel implements GLEventListener, MouseListener,
																MouseMotionListener, KeyListener {
	public static final boolean enableProjection = false;
	public static final boolean enableBlending = true;

	private static long sRandomSeed = 0;

	private FPSAnimator animator;
	public static int frames;
	public static int x, y;
	public static int width, height;

	public static int cComps;

	private float view_rotx = 20.0f, view_roty = 30.0f;
	private final float view_rotz = 0.0f;
	public static long sTick, sStartTick;

	public static int sCurrentCamTrack = 0;
	public static long sCurrentCamTrackStartTick = 0;
	public static long sNextCamTrackStartTick = 0x7fffffff;

	private static GLSpatial sGroundPlane;

	private FloatBuffer quadVertices;
	private FloatBuffer materialSpecular;

	private GLLightingConfiguration lighting;

	private int swapInterval;
	private boolean toReset;

	protected List<Renderable> renderables;

	private int prevMouseX, prevMouseY;

	public DefaultGLCanvas() {
		quadVertices = GLBuffers.newDirectFloatBuffer(12);
		quadVertices.put(new float[] {
				-1.0f, -1.0f,
				1.0f, -1.0f,
				-1.0f, 1.0f,
				1.0f, -1.0f,
				1.0f, 1.0f,
				-1.0f, 1.0f
		});
		quadVertices.flip();

		materialSpecular = GLBuffers.newDirectFloatBuffer(4);
		materialSpecular.put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
		materialSpecular.flip();

		this.lighting = new GLLightingConfiguration();

		seedRandom(15);

		width = 0;
		height = 0;
		x = 0;
		y = 0;

		renderables = new ArrayList<>();

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

	public static void sInit(GLDriver gl) {
		cComps = gl.isGLES1() ? 4 : 3;
		sGroundPlane = new GroundPlane(gl);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL2 gl = drawable.getGL().getGL2();

		sInit(new GLDriver(gl));

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

		float pos[] = {5000.0f, 5000.0f, 5000.0f, 0.0f};

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

		if (enableProjection) {
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
		}

		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;

		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glClearColor(0.1f, 0.2f, 0.3f, 1.0f); // TODO  What is this for?

		gl.glShadeModel(gl.GL_FLAT);
		gl.glDisable(GL.GL_DITHER);
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
		/*
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
		*/

		long tick = System.currentTimeMillis();

		GLDriver gl = new GLDriver((GL2) drawable.getGL());

		// Actual tick value is "blurred" a little bit.
		sTick = (sTick + tick - sStartTick) >> 1;

		gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.gluPerspective(45.0f, (float) width / (float) height, 0.5f, 150.0f);

		// Update the camera position and set the lookat.
		camTrack(gl);

		// Configure environment.
		configureLightAndMaterial(gl, lighting, materialSpecular);

		if (enableBlending) {
			gl.glEnable(GL.GL_CULL_FACE);

			// Draw the reflection by drawing models with negated Z-axis.
			gl.glPushMatrix();
			drawRenderables(gl,-1.0);
			gl.glPopMatrix();
		}

		// Draw the ground plane to the window. (opt. blending)
		drawGroundPlane(gl);

		if (enableBlending) {
			gl.glDisable(GL.GL_CULL_FACE);
		}

		// Draw all the models normally.
		drawRenderables(gl, 1.0);

		if (enableBlending) {
			// Draw fade quad over whole window (when changing cameras).
			drawFadeQuad(gl, quadVertices);
		}

		frames++;
		tick = System.currentTimeMillis();
	}

	public static void camTrack(GLDriver gl) {
		float lerp[] = new float[5];
		float eX, eY, eZ, cX, cY, cZ;
		float trackPos;
		AnimationTrack cam;
		long currentCamTick;
		int a;

		if (sNextCamTrackStartTick <= sTick) {
			++sCurrentCamTrack;

			if (sCurrentCamTrack >= AnimationTrack.tracks.length) sCurrentCamTrack = 0;
			sCurrentCamTrackStartTick = sNextCamTrackStartTick;
		}

		sNextCamTrackStartTick = sCurrentCamTrackStartTick +
				AnimationTrack.tracks[sCurrentCamTrack].len * AnimationTrack.LEN;

		cam = AnimationTrack.tracks[sCurrentCamTrack];
		currentCamTick = sTick - sCurrentCamTrackStartTick;
		trackPos = (float) currentCamTick / (AnimationTrack.LEN * cam.len);

		for (a = 0; a < 5; ++a) { lerp[a] = (cam.src[a] + cam.dest[a] * trackPos) * 0.01f; }

		if (cam.dist > 0) {
			float dist = cam.dist * 0.1f;
			cX = lerp[0];
			cY = lerp[1];
			cZ = lerp[2];
			eX = cX - (float) Math.cos(lerp[3]) * dist;
			eY = cY - (float) Math.sin(lerp[3]) * dist;
			eZ = cZ - lerp[4];
		} else {
			eX = lerp[0];
			eY = lerp[1];
			eZ = lerp[2];
			cX = eX + (float) Math.cos(lerp[3]);
			cY = eY + (float) Math.sin(lerp[3]);
			cZ = eZ + lerp[4];
		}

		gl.gluLookAt(new Vector(eX, eY, eZ), new Vector(cX, cY, cZ), 0, 0, 1);
	}

	public static void configureLightAndMaterial(GLDriver gl, GLLightingConfiguration lighting,
												 	FloatBuffer materialSpecular) {
		gl.glLight(GL2.GL_LIGHT0, GL2.GL_POSITION, lighting.light0Position);
		gl.glLight(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lighting.light0Diffuse);
		gl.glLight(GL2.GL_LIGHT1, GL2.GL_POSITION, lighting.light1Position);
		gl.glLight(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lighting.light1Diffuse);
		gl.glLight(GL2.GL_LIGHT2, GL2.GL_POSITION, lighting.light2Position);
		gl.glLight(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lighting.light2Diffuse);
		gl.glMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, materialSpecular);

		gl.glMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 60.0f);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
	}

	public void drawRenderables(GLDriver gl, double zScale) {
		// TODO
	}

	public static void drawGroundPlane(GLDriver gl) {
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_DEPTH_TEST);

		if (enableBlending) {
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
		}

		sGroundPlane.draw(gl);

		if (enableBlending) {
			gl.glDisable(GL.GL_BLEND);
		}

		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
	}

	public static void drawFadeQuad(GLDriver gl, FloatBuffer quadVertices) {
		final int beginFade = (int) (sTick - sCurrentCamTrackStartTick);
		final int endFade = (int) (sNextCamTrackStartTick - sTick);
		final int minFade = beginFade < endFade ? beginFade : endFade;

		if (minFade < 1024) {
			final float fadeColor = FixedPoint.toFloat(minFade << 7);
			gl.glColor4f(fadeColor, fadeColor, fadeColor, 0f);

			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
			gl.glDisable(GL2.GL_LIGHTING);

			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();

			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();

			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glVertexPointer(2, GL.GL_FLOAT, 0, quadVertices);
			gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

			gl.glModelView();

			gl.glEnable(GL2.GL_LIGHTING);
			gl.glDisable(GL.GL_BLEND);
			gl.glEnable(GL.GL_DEPTH_TEST);
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
		} else if (KeyEvent.VK_RIGHT == kc) {
			view_roty += 1;
		} else if (KeyEvent.VK_UP == kc) {
			view_rotx -= 1;
		} else if (KeyEvent.VK_DOWN == kc) {
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

	public static void seedRandom(long seed) {
		sRandomSeed = seed;
	}

	public static int randomUInt() {
		sRandomSeed = sRandomSeed * 0x343fd + 0x269ec3;
		return Math.abs((int) (sRandomSeed >> 16));
	}
}
