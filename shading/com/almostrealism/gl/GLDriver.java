/*
 * Copyright 2018 Michael Murray
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

import com.almostrealism.projection.OrthographicCamera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.config.FogParameters;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import org.almostrealism.algebra.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBA;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

public class GLDriver {
	public static final boolean enableDoublePrecision = false;

	protected GL2 gl;
	protected GLUT glut = new GLUT();
	protected GLU glu = new GLU();

	protected Stack<Integer> begins;

	public GLDriver(GL2 gl) {
		this.gl = gl;
		this.begins = new Stack<Integer>();
	}

	public boolean isGLES1() { return gl.isGLES1(); }

	public void glColor(RGB color) {
		if (enableDoublePrecision) {
			gl.glColor3d(color.getRed(), color.getGreen(), color.getBlue());
		} else {
			gl.glColor3f((float) color.getRed(),
						(float) color.getGreen(),
						(float) color.getBlue());
		}
	}

	public void glColor(RGBA color) {
		if (enableDoublePrecision) {
			gl.glColor4d(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		} else {
			gl.glColor4f((float) color.getRed(),
						(float) color.getGreen(),
						(float) color.getBlue(),
						(float) color.getAlpha());
		}
	}

	/**
	 * Use {@link #glColor(RGBA)} instead.
	 *
	 * @param r  Red channel
	 * @param g  Green channel
	 * @param b  Blue channel
	 * @param a  Alpha channel
	 */
	@Deprecated
	public void glColor(double r, double g, double b, double a) {
		gl.glColor4d(r, g, b, a);
	}

	/**
	 * Use {@link #glColor(RGBA)} instead.
	 *
	 * @param r  Red channel
	 * @param g  Green channel
	 * @param b  Blue channel
	 * @param a  Alpha channel
	 */
	@Deprecated
	public void glColor4f(float r, float g, float b, float a) { gl.glColor4f(r, g, b, a); }

	/** It is recommended to use {@link #glColor(RGB)} instead. */
	public void glColorPointer(GLArrayDataWrapper data) { gl.glColorPointer(data); }

	@Deprecated public void glMaterial(int code, int prop, FloatBuffer buf) {
		if (buf == null) {
			throw new IllegalArgumentException("FloatBuffer is null");
		}

		gl.glMaterialfv(code, prop, buf);
	}

	@Deprecated public void glMaterial(int code, int param, float f[], int i) {
		gl.glMaterialfv(code, param, f, i);
	}

	@Deprecated public void glMaterial(int code, int param, double f) {
		gl.glMaterialf(code, param, (float) f);
	}

	public void glMaterial(GLMaterial mat) {
		if (enableDoublePrecision) {
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, Scalar.toFloat(mat.ambient.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, Scalar.toFloat(mat.diffuse.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, Scalar.toFloat(mat.specular.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, new float[] { (float) mat.shininess.getValue() }, 0);
		} else {
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, Scalar.toFloat(mat.ambient.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, Scalar.toFloat(mat.diffuse.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, Scalar.toFloat(mat.specular.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, new float[]{ (float) mat.shininess.getValue() }, 0);
		}
	}

	/** It is recommended to use {@link #glMaterial(GLMaterial)}. */
	@Deprecated public void glColorMaterial(int param, int value) { gl.glColorMaterial(param, value); }

	public void glInitNames() { gl.glInitNames(); }
	public void glLoadName(int name) { gl.glLoadName(name); }
	public void glPushName(int name) { gl.glPushName(name); }
	public void glGenTextures(int code, IntBuffer buf) { gl.glGenTextures(code, buf); }
	public void bindTexture(Texture t) { t.bind(gl); }
	public void glBindTexture(int code, int tex) { gl.glBindTexture(code, tex); }
	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, ByteBuffer buf) {
		gl.glTexImage2D(a, b, c, d, e, f, g, h, buf);
	}

	public void glTexGeni(int a, int b, int c) { gl.glTexGeni(a, b, c); }
	public void glTexEnvi(int a, int b, int c) { gl.glTexEnvi(a, b, c); }
	public void glTexEnvf(int a, int b, float f) { gl.glTexEnvf(a, b, f); }

	public void glLineWidth(double width) { gl.glLineWidth((float) width); }
	public void glPointSize(double size) { gl.glPointSize((float) size); }

	public void glutBitmapCharacter(int font, char c) { glut.glutBitmapCharacter(font, c); }

	public void enableTexture(Texture t) { t.enable(gl); }

	public void glTexParameter(int code, int param, int value) { gl.glTexParameteri(code, param, value); }

	public void glActiveTexture(int code) { gl.glActiveTexture(code); }

	public void glVertex(Vector v) {
		if (enableDoublePrecision) {
			gl.glVertex3d(v.getX(), v.getY(), v.getZ());
		} else {
			gl.glVertex3f((float) v.getX(),
						(float) v.getY(),
						(float) v.getZ());
		}
	}

	public void glVertex(Pair p) {
		if (enableDoublePrecision) {
			gl.glVertex2d(p.getX(), p.getY());
		} else {
			gl.glVertex2f((float) p.getX(), (float) p.getY());
		}
	}

	@Deprecated public void glVertexPointer(int a, int b, int c, FloatBuffer f) { gl.glVertexPointer(a, b, c, f); }
	public void glVertexPointer(GLArrayDataWrapper data) {
		try {
			gl.glVertexPointer(data);
		} catch (GLException gl) {
			throw exceptionHelper(gl);
		}
	}

	public void glNormal(Vector n) {
		if (enableDoublePrecision) {
			gl.glNormal3d(n.getX(), n.getY(), n.getZ());
		} else {
			gl.glNormal3f((float) n.getX(), (float) n.getY(), (float) n.getZ());
		}
	}

	public void glNormalPointer(GLArrayDataWrapper data) { gl.glNormalPointer(data); }

	public void glLight(int light, int prop, FloatBuffer buf) { gl.glLightfv(light, prop, buf); }
	public void glLight(int light, int prop, float f) { gl.glLightf(light, prop, f); }
	@Deprecated public void glLight(int light, int prop, float f[], int a) { gl.glLightfv(light, prop, f, a); }

	/** It is recommended to use {@link org.almostrealism.color.Light}s instead. */
	@Deprecated public void glLightModel(int code, RGBA color) {
		gl.glLightModelfv(code, FloatBuffer.wrap(Scalar.toFloat(color.toArray())));
	}

	public boolean isLightingOn() { return gl.glIsEnabled(GL2.GL_LIGHTING); }

	public void glTranslate(Vector t) {
		if (enableDoublePrecision) {
			gl.glTranslated(t.getX(), t.getY(), t.getZ());
		} else {
			gl.glTranslatef((float) t.getX(),
							(float) t.getY(),
							(float) t.getZ());
		}
	}

	public void glScale(Vector s) {
		if (enableDoublePrecision) {
			gl.glScaled(s.getX(), s.getY(), s.getZ());
		} else {
			gl.glScalef((float) s.getX(),
					(float) s.getY(),
					(float) s.getZ());
		}
	}

	public void glScale(double s) {
		glScale(new Vector(s, s, s));
	}

	public void glRotate(double a, double b, double c, double d) {
		if (enableDoublePrecision) {
			gl.glRotated(a, b, c, d);
		} else {
			gl.glRotatef((float) a, (float) b, (float) c, (float) d);
		}
	}

	public void glAccum(int param, double value) { gl.glAccum(param, (float) value); }

	public void glColorMask(boolean a, boolean b, boolean c, boolean d) {
		gl.glColorMask(a, b, c, d);
	}

	public void clearColorBuffer() { gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
	public void glClearColor(RGBA c) { gl.glClearColor(c.r(), c.g(), c.b(), c.a()); }
	public void glClearAccum(RGBA c) {
		gl.glClearAccum((float) c.getRed(), (float) c.getGreen(), (float) c.getBlue(), (float) c.getAlpha());
	}

	public void glDepthFunc(int code) { gl.glDepthFunc(code); }
	public void glStencilFunc(int param, int a, int b) { gl.glStencilFunc(param, a, b); }
	public void glStencilOp(int a, int b, int c) { gl.glStencilOp(a, b, c); }

	public void glClearDepth(double d) {
		if (enableDoublePrecision) {
			gl.glClearDepth(d);
		} else {
			gl.glClearDepthf((float) d);
		}
	}

	public void glClearStencil(int param) {
		gl.glClearStencil(param);
	}

	public void glClear(int bits) { gl.glClear(bits); }

	public void setFog(FogParameters f) {
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP);
		gl.glFogfv(GL2.GL_FOG_COLOR, FloatBuffer.wrap(Scalar.toFloat(f.fogColor.toArray())));
		gl.glFogf(GL2.GL_FOG_DENSITY, (float) f.fogDensity);
		gl.glFogf(GL2.GL_FOG_START, 0.0f);
		gl.glFogf(GL2.GL_FOG_END, Float.MAX_VALUE);
		gl.glEnable(GL2.GL_FOG);
	}

	public void glQuads() { glBegin(GL2.GL_QUADS); }

	public void glBegin(int code) {
		gl.glBegin(code);
		begins.push(code);
	}

	public void glEnable(int code) {
		gl.glEnable(code);
	}
	public void glEnableClientState(int code) { gl.glEnableClientState(code); }

	public void glBlendFunc(int c1, int c2) { gl.glBlendFunc(c1, c2); }
	public void glShadeModel(int model) { gl.glShadeModel(model); }
	public int glRenderMode(int mode) { return gl.glRenderMode(mode); }

	public void glPushAttrib(int attrib) { gl.glPushAttrib(attrib); }
	public void glPopAttrib() { gl.glPopAttrib(); }

	public void glGenBuffers(int a, int b[], int c) { gl.glGenBuffers(a, b, c); }
	public void glBindBuffer(int code, int v) { gl.glBindBuffer(code, v); }
	public void glBufferData(int code, int l, ByteBuffer buf, int d) { gl.glBufferData(code, l, buf, d); }
	public void glSelectBuffer(int size, IntBuffer buf) { gl.glSelectBuffer(size, buf); }

	public int glGenLists(int code) { return gl.glGenLists(code); }
	public void glCallList(int list) { gl.glCallList(list); }
	public void glNewList(int list, int code) { gl.glNewList(list, code); }

	public void uv(Pair texCoord) {
		if (enableDoublePrecision) {
			gl.glTexCoord2d(texCoord.getA(), texCoord.getB());
		} else {
			gl.glTexCoord2f((float) texCoord.getA(), (float) texCoord.getB());
		}
	}

	public void glMatrixMode(int code) { gl.glMatrixMode(code); }
	public void glModelView() { glMatrixMode(GL2.GL_MODELVIEW);}
	public void glPushMatrix() { gl.glPushMatrix(); }
	public void glPopMatrix() { gl.glPopMatrix(); }
	public void glLoadIdentity() { gl.glLoadIdentity(); }

	public void glMultMatrix(FloatBuffer mat) { gl.glMultMatrixf(mat); }

	public void glRasterPos(Vector pos) {
		if (enableDoublePrecision) {
			gl.glRasterPos3d(pos.getX(), pos.getY(), pos.getZ());
		} else {
			gl.glRasterPos3f((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
		}
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Deprecated public void glViewport(int x, int y, int w, int h) { gl.glViewport(x, y, w, h); }

	/** It is recommended to use a {@link PinholeCamera} instead. */
	@Deprecated public void gluPerspective(double d1, double d2, double d3, double d4) { glu.gluPerspective(d1, d2, d3, d4); }

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Deprecated public void gluOrtho2D(double a, double b, double c, double d) { glu.gluOrtho2D(a, b, c, d); }

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
		glu.gluLookAt(e.getX(), e.getY(), e.getZ(), c.getX(), c.getY(), c.getZ(), var13, var15, var17);
	}

	public boolean gluUnProject(Vector w, double modelview[], double projection[], int viewport[], Vector worldpos) {
		return glu.gluUnProject((float) w.getX(), (float) w.getY(), (float) w.getZ(),
								FloatBuffer.wrap(Scalar.toFloat(modelview)), FloatBuffer.wrap(Scalar.toFloat(projection)),
								IntBuffer.wrap(viewport), FloatBuffer.wrap(Scalar.toFloat(worldpos.toArray())));
	}

	public boolean gluUnProject(Vector w, Vector worldpos) {
		int[] viewport = getViewport();
		double[] modelview = getModelViewMatrix();
		double[] projection = getProjectionMatrix();
		return gluUnProject(w, modelview, projection, viewport, worldpos);
	}

	public void gluPickMatrix(float x, float y, float w, float h, int viewport[]) {
		glu.gluPickMatrix(x, y, w, h, IntBuffer.wrap(viewport));
	}

	public void glProjection(Camera c) {
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		if (c instanceof PinholeCamera) {
			PinholeCamera camera = (PinholeCamera) c;

			float width = (float) camera.getProjectionWidth();
			float height = (float) camera.getProjectionHeight();
			glu.gluPerspective(Math.toDegrees(camera.getFOV()[0]), width / height, 1, 1e9);
		}

		if (c instanceof OrthographicCamera) {
			OrthographicCamera camera = (OrthographicCamera) c;

			Vector cameraLocation = camera.getLocation();
			Vector cameraTarget = cameraLocation.add(camera.getViewingDirection());
			Vector up = camera.getUpDirection();

			gluLookAt(cameraLocation, cameraTarget, up.getX(), up.getY(), up.getZ());
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	// TODO  Should accept Plane instance
	public void glClipPlane(int plane, DoubleBuffer eqn) { gl.glClipPlane(plane, eqn); }

	public void glCullFace(int param) { gl.glCullFace(param); }
	public void glFrontFace(int param) { gl.glFrontFace(param); }

	public void glFlush() { gl.glFlush(); }
	public int glEnd() { gl.glEnd(); return begins.pop(); }
	public void glEndList() { gl.glEndList(); }
	@Deprecated public void glDisable(int code) { gl.glDisable(code); }
	@Deprecated public void glDisableClientState(int code) { gl.glDisableClientState(code); }

	public void glHint(int param, int value) { gl.glHint(param, value); }

	public void glutWireCube(double size) {
		// TODO  Replace with our own cube code
		glut.glutWireCube((float) size);
	}

	public void glutSolidSphere(double radius, int slices, int stacks) {
		// TODO  Replace with our own sphere code
		glut.glutSolidSphere(radius, slices, stacks);
	}

	public void glDrawArrays(int code, int a, int b) { gl.glDrawArrays(code, a, b); }

	public String glGetString(int code) { return gl.glGetString(code); }

	public void glReadPixels(int x, int y, int w, int h, int comp, int form, DoubleBuffer buf) {
		gl.glReadPixels(x, y, w, h, comp, form, buf);
	}

	public void glReadPixels(int x, int y, int w, int h, int comp, int form, ByteBuffer buf) {
		gl.glReadPixels(x, y, w, h, comp, form, buf);
	}

	public void glPixelStorei(int param, int value) {
		gl.glPixelStorei(param, value);
	}

	public float getLineWidthGranularity() {
		FloatBuffer f = FloatBuffer.allocate(1);
		gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, f);
		return f.get(0);
	}

	public Pair getLineWidthRange() {
		FloatBuffer f = FloatBuffer.allocate(2);
		gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, f);
		return new Pair(f.get(0), f.get(1));
	}

	public double[] getModelViewMatrix() {
		double modelview[] = new double[16];
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, DoubleBuffer.wrap(modelview));
		return modelview;
	}

	public double[] getProjectionMatrix() {
		double projection[] = new double[16];
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, DoubleBuffer.wrap(projection));
		return projection;
	}

	public int[] getViewport() {
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, IntBuffer.wrap(viewport));
		return viewport;
	}

	public int getMatrixMode() {
		int matrixMode[] = new int[1];
		gl.glGetIntegerv(GL2.GL_MATRIX_MODE, IntBuffer.wrap(matrixMode));
		return matrixMode[0];
	}

	protected GLException exceptionHelper(GLException gle) {
		if (gle.getMessage() == null) return gle;
		if (gle.getMessage().contains("May not call this between glBegin and glEnd")) {
			return new GLException(gle.getMessage() + " (begin = " + this.begins.peek() + ")");
		}

		return gle;
	}
}
