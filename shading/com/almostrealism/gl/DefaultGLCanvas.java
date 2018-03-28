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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.almostrealism.renderable.GLDriver;
import com.almostrealism.renderable.Quad3;
import com.jogamp.common.util.IOUtil;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

import com.jogamp.opengl.math.FixedPoint;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.almostrealism.algebra.Vector;
import org.almostrealism.space.BasicGeometry;

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.renderable.Renderable;
import com.jogamp.newt.Window;
import com.jogamp.opengl.util.FPSAnimator;

public abstract class DefaultGLCanvas extends GLJPanel implements GLEventListener, MouseListener,
																MouseMotionListener, KeyListener {
	private static final String[] suffixes = {"posx", "negx", "posy", "negy", "posz", "negz"};
	private static final int[] targets = {GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
			GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
			GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
			GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
			GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
			GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};

	public static final boolean enableProjection = false;
	public static final boolean enableBlending = false;
	public static final boolean enableCamTrackFade = false;

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

	private static GroundPlane sGroundPlane;
	private static Quad3 sFadeQuad; // TODO  Use this quad instead of the buffer;
	private static FloatBuffer quadBuf;

	private Texture skydome;
	private ClassLoader skydomeScope;
	private String skydomeBasename;
	private String skydomeSuffix;
	private boolean skydomeMipmapped;

	private GLLightingConfiguration lighting;

	private int swapInterval;
	private boolean toReset;

	protected List<Renderable> renderables;

	private int prevMouseX, prevMouseY;

	public DefaultGLCanvas() {
		this((Texture) null);
	}

	public DefaultGLCanvas(Texture skydome) {
		this.skydome = skydome;
		lighting = new GLLightingConfiguration();

		seedRandom(15);

		width = 0;
		height = 0;
		x = 0;
		y = 0;

		renderables = new ArrayList<>();

		animator = new FPSAnimator(200);
		animator.add(this);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		swapInterval = 1;
	}

	public DefaultGLCanvas(ClassLoader scope,
							String basename,
							String suffix, boolean mipmapped) {
		this.skydomeScope = scope;
		this.skydomeBasename = basename;
		this.skydomeSuffix = suffix;
		this.skydomeMipmapped = mipmapped;
		lighting = new GLLightingConfiguration();

		seedRandom(15);

		width = 0;
		height = 0;
		x = 0;
		y = 0;

		renderables = new ArrayList<>();

		animator = new FPSAnimator(200);
		animator.add(this);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		swapInterval = 1;
	}

	public void add(Renderable r) { if (r != null) renderables.add(r); }

	public void start() { animator.start(); }

	public void reset() { toReset = true; }

	public void removeAll() { renderables.clear(); }

	public List<Renderable> getRenderables() { return renderables; }

	public abstract PinholeCamera getCamera();

	public void lookAt(BasicGeometry g) {
		float f[] = g.getPosition();
		getCamera().setViewingDirection(new Vector(f[0], f[1], f[2]));
	}

	public static void sInit(GLDriver gl) {
		cComps = gl.isGLES1() ? 4 : 3;
		sGroundPlane = new GroundPlane(gl);
		sFadeQuad = new Quad3(new Vector(-1.0, -1.0, 1.0),
								new Vector(-1.0, -1.0, 1.0),
								new Vector(1.0, -1.0, 1.0),
								new Vector(1.0,-1.0, 1.0));
		quadBuf = GLBuffers.newDirectFloatBuffer(12);
		quadBuf.put(new float[] {
				-1.0f, -1.0f,
				1.0f, -1.0f,
				-1.0f, 1.0f,
				1.0f, -1.0f,
				1.0f, 1.0f,
				-1.0f, 1.0f
		});
		quadBuf.flip();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL2 gl2 = drawable.getGL().getGL2();
		GLDriver gl = new GLDriver(gl2);

		sInit(gl);

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

		gl.glEnable(GL2ES1.GL_NORMALIZE);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glShadeModel(GL2.GL_FLAT);

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHT2);

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

		DefaultGLCanvas.sStartTick = System.currentTimeMillis();
		DefaultGLCanvas.frames = 0;

		if (this.skydomeBasename != null) {
			try {
				this.skydome = loadFromStreams(gl2, skydomeScope, skydomeBasename, skydomeSuffix, skydomeMipmapped);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		initRenderables(gl);
	}

	protected void initRenderables(GLDriver gl) {
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
	public void doView(GLDriver gl) {
		PinholeCamera c = getCamera();
		gl.glProjection(c);
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

		doView(gl);

		// Update the camera position and set the lookat.
//		camTrack(gl); // TODO  Restore camera tracking, but actually modify the camera

		drawSkydome(gl);

		// Configure environment.
		configureLightAndMaterial(gl, lighting);

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

		if (enableBlending && enableCamTrackFade) {
			// Draw fade quad over whole window (when changing cameras).
			drawFadeQuad(gl);
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

	public static void configureLightAndMaterial(GLDriver gl, GLLightingConfiguration lighting) {
		gl.glLight(GL2.GL_LIGHT0, GL2.GL_POSITION, lighting.light0Position);
		gl.glLight(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lighting.light0Diffuse);
		gl.glLight(GL2.GL_LIGHT1, GL2.GL_POSITION, lighting.light1Position);
		gl.glLight(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lighting.light1Diffuse);
		gl.glLight(GL2.GL_LIGHT2, GL2.GL_POSITION, lighting.light2Position);
		gl.glLight(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lighting.light2Diffuse);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
	}

	public void drawRenderables(GLDriver gl, double zScale) {
		gl.glScale(new Vector(1.0, 1.0, zScale));

		for (Renderable r : renderables) {
			System.out.println("Rendering " + r);
			r.display(gl);
			System.out.println("Done rendering " + r);
		}
	}

	public void drawSkydome(GLDriver gl) {
		if (skydome == null) return;

		gl.glDisable(GL.GL_DEPTH_TEST);

		gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.bindTexture(skydome);
		gl.enableTexture(skydome);

		// This is a workaround for a driver bug on Mac OS X where the
		// normals are not being sent down to the hardware in
		// GL_NORMAL_MAP texgen mode. Temporarily enabling lighting
		// causes the normals to be sent down. Thanks to Ken Dyke.
		gl.glEnable(GL2ES1.GL_LIGHTING);

		gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
		gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
		gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);

		gl.glEnable(GL2.GL_TEXTURE_GEN_S);
		gl.glEnable(GL2.GL_TEXTURE_GEN_T);
		gl.glEnable(GL2.GL_TEXTURE_GEN_R);

		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glutSolidSphere(5.0, 40, 20);

		gl.glDisable(GL2ES1.GL_LIGHTING);

		gl.glPopMatrix();
		gl.glMatrixMode(GL2ES1.GL_MODELVIEW);

		gl.glDisable(GL2.GL_TEXTURE_GEN_S);
		gl.glDisable(GL2.GL_TEXTURE_GEN_T);
		gl.glDisable(GL2.GL_TEXTURE_GEN_R);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	public static void drawGroundPlane(GLDriver gl) {
		// Temporarily disable lighting and depth test
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_DEPTH_TEST);

		if (enableBlending) {
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
		}

		sGroundPlane.render(gl);

		if (enableBlending) {
			gl.glDisable(GL.GL_BLEND);
		}

		// Restore lighting and depth test
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
	}

	public static void drawFadeQuad(GLDriver gl) {
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
			gl.glVertexPointer(2, GL.GL_FLOAT, 0, quadBuf);
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

	public static Texture loadFromStreams(GL gl,
										  ClassLoader scope,
										  String basename,
										  String suffix, boolean mipmapped) throws IOException, GLException {
		Texture cubemap = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);

		for (int i = 0; i < suffixes.length; i++) {
			String resourceName = basename + suffixes[i] + "." + suffix;

			URL r = scope.getResource(resourceName);

			InputStream in;
			if (r == null) {
				in = new FileInputStream("resources/" + resourceName);
			} else {
				in = r.openStream();
			}

			TextureData data = TextureIO.newTextureData(GLContext.getCurrentGL().getGLProfile(),
											in, mipmapped, IOUtil.getFileSuffix(resourceName));
			if (data == null) {
				throw new IOException("Unable to load texture " + resourceName);
			}

			cubemap.updateImage(gl, data, targets[i]);
		}

		return cubemap;
	}
}
