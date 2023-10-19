/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import java.util.List;

import io.almostrealism.code.Precision;
import org.almostrealism.c.CLanguageOperations;
import org.almostrealism.projection.OrthographicCamera;
import com.almostrealism.renderable.GroundPlane;
import com.almostrealism.renderable.Quad3;
import com.jogamp.common.util.IOUtil;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGBA;
import org.almostrealism.geometry.BasicGeometry;

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.renderable.Renderable;
import com.jogamp.newt.Window;
import com.jogamp.opengl.util.FPSAnimator;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

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
	public static final boolean enableCameraReshape = false;

	private static long sRandomSeed = 0;

	private FPSAnimator animator;
	public static int frames;
	public int x, y;
	public int width, height;

	private static final boolean LOG_FPS = true;
	private int framesThisSecond = 0;
	private long startOfSecond = System.currentTimeMillis();

	public static int cComps;

	private float viewRotx = 20.0f, viewRoty = 30.0f;
	private final float viewRotz = 0.0f;
	public static long sTick, sStartTick;

	public static int sCurrentCamTrack = 0;
	public static long sCurrentCamTrackStartTick = 0;
	public static long sNextCamTrackStartTick = 0x7fffffff;

	private static GroundPlane sGroundPlane;
	private static Quad3 sFadeQuad; // TODO  Use this quad instead of the buffer
	private static FloatBuffer quadBuf;

	// TODO  Move skydome into GLScene
	private Texture skydome;
	private ClassLoader skydomeScope;
	private String skydomeBasename;
	private String skydomeSuffix;
	private boolean skydomeMipmapped;

	private GLLightingConfiguration lighting;

	private boolean toReset;

	protected GLScene renderables;
	protected GLDriver gl;
	protected GLRenderingEngine engine;

	private int prevMouseX, prevMouseY;

	public DefaultGLCanvas() {
		this(new GLScene(new Scene()), (Texture) null);
	}

	public DefaultGLCanvas(GLScene s, Texture skydome) {
		this.skydome = skydome;
		lighting = new GLLightingConfiguration(s.getScene().getLights());
		engine = new DefaultGLRenderingEngine();

		seedRandom(15);

		width = 0;
		height = 0;
		x = 0;
		y = 0;

		renderables = s;

		animator = new FPSAnimator(100);
		animator.add(this);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	public DefaultGLCanvas(GLScene s, ClassLoader scope,
						   String basename, String suffix,
						   boolean mipmapped) {
		this.skydomeScope = scope;
		this.skydomeBasename = basename;
		this.skydomeSuffix = suffix;
		this.skydomeMipmapped = mipmapped;
		lighting = new GLLightingConfiguration(s.getScene().getLights());
		engine = new DefaultGLRenderingEngine();

		seedRandom(15);

		width = 0;
		height = 0;
		x = 0;
		y = 0;

		renderables = s;

		animator = new FPSAnimator(s.getFPS());
		animator.add(this);
		addGLEventListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	public void add(Renderable r) { if (r != null) renderables.add(r); }

	public void start() { animator.start(); }

	public void reset() { toReset = true; }

	public void removeAll() { renderables.clear(); }

	public List<Renderable> getRenderables() { return renderables; }

	public Scene<ShadableSurface> getScene() { return renderables.getScene(); }

	public PinholeCamera getCamera() { return (PinholeCamera) renderables.getCamera(); }

	public void lookAt(BasicGeometry g) {
		float f[] = g.getPosition();
		getCamera().setViewingDirection(new Vector(f[0], f[1], f[2]));
	}

	public static void sInit(GLDriver gl) {
		cComps = 4;
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
		GL2 gl2 = drawable.getGL().getGL2();
		gl = new GLDriver(new CLanguageOperations(Precision.FP32, false, false), gl2);

		sInit(gl);

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

		gl.enable(GL2ES1.GL_NORMALIZE);
		gl.enable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glShadeModel(GL2.GL_FLAT);

		gl.enable(GL2.GL_LIGHTING);
		gl.enable(GL2.GL_LIGHT0);
		gl.enable(GL2.GL_LIGHT1);
		gl.enable(GL2.GL_LIGHT2);

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

	protected void initRenderables(GLDriver gl) { renderables.init(gl); }

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;

		if (renderables == null || gl == null) return; // TODO  Display a warning?

		if (enableCameraReshape) {
			OrthographicCamera c = renderables.getCamera();
			c.setProjectionHeight(c.getProjectionWidth() / c.getAspectRatio());
			gl.setCamera(c);
		}

		gl.glClearColor(new RGBA(0.0, 0.0, 0.0, 1.0));
		gl.glShadeModel(GL2.GL_FLAT);
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
		if (LOG_FPS) {
			logFps();
		}

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
		gl.pushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

		for (Renderable r : renderables) {
			r.display(gl);
		}

		gl.popMatrix();
		*/

		if (gl == null) return;

		long tick = System.currentTimeMillis();

		// Actual tick value is "blurred" a little bit.
		sTick = (sTick + tick - sStartTick) >> 1;

		gl.glClearColorAndDepth();
		gl.glClear(GL2.GL_STENCIL_BUFFER_BIT);

		doView(gl);

		// Update the camera position and set the lookat.
//		camTrack(gl); // TODO  Restore camera tracking, but actually modify the camera

		drawSkydome(gl);

		// Configure environment.
		configureLightAndMaterial(gl, lighting);

		if (enableBlending) {
			gl.enable(GL.GL_CULL_FACE);

			// Draw the reflection by drawing models with negated Z-axis.
			gl.pushMatrix();
			drawRenderables(gl,-1.0);
			gl.popMatrix();
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

	private void logFps() {
		long sinceStartofSecond = System.currentTimeMillis() - startOfSecond;
		if (sinceStartofSecond > 1000) {
			if (framesThisSecond > 0) {
				System.out.println("FPS: " + framesThisSecond);
			} else {
				System.err.println("WARNING: The current frame took " + sinceStartofSecond/1000d + "s to display!");
			}
			framesThisSecond = 0;
			startOfSecond = System.currentTimeMillis();
		} else {
			framesThisSecond++;
		}
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
		gl.setLighting(lighting);
		gl.enable(GL2.GL_COLOR_MATERIAL);
	}

	public void drawRenderables(GLDriver gl, double zScale) {
		engine.drawRenderables(gl, renderables, zScale);
	}

	public void drawSkydome(GLDriver gl) {
		if (skydome == null) return;

		gl.glDisable(GL.GL_DEPTH_TEST);

		gl.glLoadIdentity();

		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.bindTexture(skydome);
		gl.enableTexture(skydome);

//		gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);  TODO
//		gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);  TODO
//		gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);  TODO

		gl.enable(GL2.GL_TEXTURE_GEN_S);
		gl.enable(GL2.GL_TEXTURE_GEN_T);
		gl.enable(GL2.GL_TEXTURE_GEN_R);

//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);  TODO

//		gl.glMatrixMode(GL.GL_TEXTURE);  TODO  There must be a modern way of disabling the texture transformation
//		gl.pushMatrix();
//		gl.glLoadIdentity();

		gl.glutSolidSphere(5.0, 40, 20);

		gl.glDisable(GL2ES1.GL_LIGHTING);

//		gl.popMatrix();
//		gl.glMatrixMode(GL2ES1.GL_MODELVIEW);

		gl.glDisable(GL2.GL_TEXTURE_GEN_S);
		gl.glDisable(GL2.GL_TEXTURE_GEN_T);
		gl.glDisable(GL2.GL_TEXTURE_GEN_R);
		gl.enable(GL.GL_DEPTH_TEST);
	}

	public static void drawGroundPlane(GLDriver gl) {
		// Temporarily disable lighting and depth test
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_DEPTH_TEST);

		if (enableBlending) {
			gl.enable(GL.GL_BLEND);
			gl.blendFunc("GL_ZERO", "GL_SRC_COLOR");
		}

		sGroundPlane.render(gl);

		if (enableBlending) {
			gl.glDisable(GL.GL_BLEND);
		}

		// Restore lighting and depth test
		gl.enable(GL.GL_DEPTH_TEST);
		gl.enable(GL2.GL_LIGHTING);
	}

	public static void drawFadeQuad(GLDriver gl) {
		/*
		final int beginFade = (int) (sTick - sCurrentCamTrackStartTick);
		final int endFade = (int) (sNextCamTrackStartTick - sTick);
		final int minFade = beginFade < endFade ? beginFade : endFade;

		if (minFade < 1024) {
			final float fadeColor = FixedPoint.toFloat(minFade << 7);
			gl.glColor(new RGBA(fadeColor, fadeColor, fadeColor, 0.0));

			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.enable(GL.GL_BLEND);
			gl.blendFunc("GL_ZERO", "GL_SRC_COLOR");
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

			gl.enable(GL2.GL_LIGHTING);
			gl.glDisable(GL.GL_BLEND);
			gl.enable(GL.GL_DEPTH_TEST);
		}
		*/
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
		float width, height;
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

		viewRotx += thetaX;
		viewRoty += thetaY;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int kc = e.getKeyCode();

		if (KeyEvent.VK_LEFT == kc) {
			viewRoty -= 1;
		} else if (KeyEvent.VK_RIGHT == kc) {
			viewRoty += 1;
		} else if (KeyEvent.VK_UP == kc) {
			viewRotx -= 1;
		} else if (KeyEvent.VK_DOWN == kc) {
			viewRotx += 1;
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
