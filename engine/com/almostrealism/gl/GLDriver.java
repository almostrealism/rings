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
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import org.almostrealism.algebra.*;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBA;
import org.almostrealism.graph.Triangle;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

public class GLDriver {
	protected boolean enableDoublePrecision = false;
	protected boolean useGlMatrixStack = false;

	protected GL2 gl;
	protected GLU glu;
	protected GLUT glut;

	protected Stack<Integer> begins;

	protected Camera camera;
	protected Stack<Camera> cameraStack;
	protected TransformMatrix projection;

	protected TransformMatrix transform;
	protected Stack<TransformMatrix> matrixStack;

	public GLDriver(GL2 gl) {
		this.gl = gl;

		if (gl != null) {
			glu = new GLU();
			glut = new GLUT();
		}

		this.begins = new Stack<>();

		this.cameraStack = new Stack<>();
		this.projection = new TransformMatrix();

		this.transform = new TransformMatrix();
		this.matrixStack = new Stack<>();
	}

	public void setLighting(GLLightingConfiguration lighting) {
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lighting.light0Position);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lighting.light0Diffuse);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lighting.light1Position);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lighting.light1Diffuse);
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, lighting.light2Position);
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lighting.light2Diffuse);
	}

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

	public void glMaterial(GLMaterial mat) {
		if (enableDoublePrecision) {
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, Scalar.toFloat(mat.ambient.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, Scalar.toFloat(mat.diffuse.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, Scalar.toFloat(mat.specular.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, new float[]{(float) mat.shininess.getValue()}, 0);
		} else {
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, Scalar.toFloat(mat.ambient.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, Scalar.toFloat(mat.diffuse.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, Scalar.toFloat(mat.specular.toArray()), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, new float[]{(float) mat.shininess.getValue()}, 0);
		}
	}

	public void glInitNames() { gl.glInitNames(); }

	public void glLoadName(int name) { gl.glLoadName(name); }

	public void glPushName(int name) { gl.glPushName(name); }

	/** It is recommended to use {@link org.almostrealism.texture.Texture} instead. */
	@Deprecated
	public void genTextures(int code, int textures[]) { gl.glGenTextures(code, IntBuffer.wrap(textures)); }

	public void bindTexture(Texture t) { t.bind(gl); }

	public void bindTexture(String code, int tex) {
		try {
			gl.glBindTexture(GL2.class.getField(code).getInt(gl), tex);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, byte buf[]) {
		gl.glTexImage2D(a, b, c, d, e, f, g, h, ByteBuffer.wrap(buf));
	}

	public void glTexEnvf(int a, int b, float f) { gl.glTexEnvf(a, b, f); }

	public void glTexParameter(int code, int param, int value) { gl.glTexParameteri(code, param, value); }

	public void glLineWidth(double width) { gl.glLineWidth((float) width); }

	public void glPointSize(double size) { gl.glPointSize((float) size); }

	public void glutBitmapCharacter(int font, char c) { glut.glutBitmapCharacter(font, c); }

	public void enableTexture(Texture t) { t.enable(gl); }

	public void disableTexture(Texture t) { t.disable(gl); }

	public void glActiveTexture(int code) { gl.glActiveTexture(code); }

	public void glVertex(Vector v) {
		v = transformPosition(v);
		v.setZ(-v.getZ());

		if (enableDoublePrecision) {
			gl.glVertex3d(v.getX(), v.getY(), v.getZ());
		} else {
			gl.glVertex3f((float) v.getX(),
					(float) v.getY(),
					(float) v.getZ());
		}
	}

	public void glVertex(Pair p) {
		// TODO  What about transform matrices?

		if (enableDoublePrecision) {
			gl.glVertex2d(p.getX(), p.getY());
		} else {
			gl.glVertex2f((float) p.getX(), (float) p.getY());
		}
	}

	public void glNormal(Vector n) {
		n = transformDirection(n);

		if (enableDoublePrecision) {
			gl.glNormal3d(n.getX(), n.getY(), n.getZ());
		} else {
			gl.glNormal3f((float) n.getX(), (float) n.getY(), (float) n.getZ());
		}
	}

	/** It is recommended to use {@link org.almostrealism.color.Light}s instead. */
	@Deprecated
	public void glLightModel(int code, RGBA color) {
		gl.glLightModelfv(code, FloatBuffer.wrap(Scalar.toFloat(color.toArray())));
	}

	public boolean isLightingOn() { return gl.glIsEnabled(GL2.GL_LIGHTING); }

	public void glTranslate(Vector t) { glMultMatrix(TransformMatrix.createTranslationMatrix(t)); }

	public void scale(Vector s) { glMultMatrix(TransformMatrix.createScaleMatrix(s)); }

	public void scale(double s) {
		scale(new Vector(s, s, s));
	}

	public void glRotate(double angle, Vector v) {
		glMultMatrix(TransformMatrix.createRotateMatrix(Math.toRadians(angle), v));
	}

	public void triangle(Triangle t) {
		Vector v[] = t.getVertices();
		float tex[][] = t.getTextureCoordinates();

		glVertex(v[0]);
		uv(new Pair(tex[0][0], tex[0][1]));
		glVertex(v[1]);
		uv(new Pair(tex[1][0], tex[1][1]));
		glVertex(v[2]);
		uv(new Pair(tex[2][0], tex[2][1]));
	}

	public void glAccum(int param, double value) { gl.glAccum(param, (float) value); }

	public void glColorMask(boolean r, boolean g, boolean b, boolean a) { gl.glColorMask(r, g, b, a); }

	public void clearColorBuffer() { gl.glClear(GL.GL_COLOR_BUFFER_BIT); }

	public void glClearColor(RGBA c) { gl.glClearColor(c.r(), c.g(), c.b(), c.a()); }

	public void glClearAccum(RGBA c) {
		gl.glClearAccum((float) c.getRed(), (float) c.getGreen(), (float) c.getBlue(), (float) c.getAlpha());
	}

	public void glDepthFunc(int code) { gl.glDepthFunc(code); }

	public void glStencilFunc(int func, int ref, int mask) { gl.glStencilFunc(func, ref, mask); }

	public void glStencilOp(int sfail, int dpfail, int dppass) { gl.glStencilOp(sfail, dpfail, dppass); }

	public void clearDepth(double d) {
		if (enableDoublePrecision) {
			gl.glClearDepth(d);
		} else {
			gl.glClearDepthf((float) d);
		}
	}

	public void glClearStencil(int param) {
		gl.glClearStencil(param);
	}

	@Deprecated
	public void glClear(int bits) { gl.glClear(bits); }

	public void setFog(FogParameters f) {
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP);
		gl.glFogfv(GL2.GL_FOG_COLOR, FloatBuffer.wrap(Scalar.toFloat(f.fogColor.toArray())));
		gl.glFogf(GL2.GL_FOG_DENSITY, (float) f.fogDensity);
		gl.glFogf(GL2.GL_FOG_START, 0.0f);
		gl.glFogf(GL2.GL_FOG_END, Float.MAX_VALUE);
		gl.glEnable(GL2.GL_FOG);
	}

	@Deprecated
	public void glQuads() { glBegin(GL2.GL_QUADS); }

	@Deprecated
	public void glBegin(int code) {
		gl.glBegin(code);
		begins.push(code);
	}

	@Deprecated
	public void enable(int code) { gl.glEnable(code); }

	public void enable(String code) {
		try {
			gl.glEnable(GL2.class.getField(code).getInt(gl));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void glEnableClientState(int code) { gl.glEnableClientState(code); }

	public void glPolygonMode(int param, int value) { gl.glPolygonMode(param, value); }

	public void blendFunc(String sfactor, String dfactor) {
		try {
			gl.glBlendFunc(GL2.class.getField(sfactor).getInt(gl),
					GL2.class.getField(dfactor).getInt(gl));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void glShadeModel(int model) { gl.glShadeModel(model); }

	public int glRenderMode(int mode) { return gl.glRenderMode(mode); }

	public void glPushAttrib(int attrib) { gl.glPushAttrib(attrib); }

	public void glPopAttrib() { gl.glPopAttrib(); }

	public void glGenBuffers(int a, int b[], int c) { gl.glGenBuffers(a, b, c); }

	public void glBindBuffer(int code, int v) { gl.glBindBuffer(code, v); }

	public void glBufferData(int code, int l, ByteBuffer buf, int d) { gl.glBufferData(code, l, buf, d); }

	public void glSelectBuffer(int size, IntBuffer buf) { gl.glSelectBuffer(size, buf); }

	public void uv(Pair texCoord) {
		if (enableDoublePrecision) {
			gl.glTexCoord2d(texCoord.getA(), texCoord.getB());
		} else {
			gl.glTexCoord2f((float) texCoord.getA(), (float) texCoord.getB());
		}
	}

	public void pushMatrix() {
		if (useGlMatrixStack) {
			gl.glPushMatrix();
		} else {
			TransformMatrix t = new TransformMatrix();
			t.setMatrix(transform.getMatrix());
			matrixStack.push(t);
		}
	}

	public void popMatrix() {
		if (useGlMatrixStack) {
			gl.glPopMatrix();
		} else {
			transform = matrixStack.pop();
		}
	}

	public void glLoadIdentity() { transform = new TransformMatrix(); }

	public void glMultMatrix(TransformMatrix m) { transform = transform.multiply(m); }

	public void setMatrix(TransformMatrix m) { transform = m; }

	public void glRasterPos(Vector pos) {
		if (enableDoublePrecision) {
			gl.glRasterPos3d(pos.getX(), pos.getY(), pos.getZ());
		} else {
			gl.glRasterPos3f((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
		}
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Deprecated
	public void setViewport(int x, int y, int w, int h) { gl.glViewport(x, y, w, h); }

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Deprecated
	public void gluOrtho2D(double a, double b, double c, double d) { glu.gluOrtho2D(a, b, c, d); }

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

	protected Vector transformPosition(Vector in) {
		return this.projection.multiply(this.transform).transformAsLocation(in);
	}

	protected Vector transformDirection(Vector in) {
		return this.projection.multiply(this.transform).transformAsOffset(in);
	}

	/**
	 * If {@link Camera} is null, load the identity matrix into the projection stack
	 * and modelview stack, otherwise delegate to {@link #glProjection(Camera)}.
	 *
	 * @param c Camera to use for projection, or null to reset matrices.
	 */
	public void setCamera(Camera c) {
		if (c == null) {
			this.projection = new TransformMatrix();
			this.transform = new TransformMatrix();
		} else {
			this.glProjection(c);
			// TODO  Update projection when camera is updated?
		}

		this.camera = c;
	}

	/** Returns the {@link Camera} assigned via {@link #setCamera(Camera)}. */
	public Camera getCamera() { return camera; }

	/**
	 * Saves the current {@link Camera} (see {@link #setCamera(Camera)}) so that
	 * it can be restored with {@link #popCamera()}.
	 */
	public void pushCamera() { this.cameraStack.push(camera); }

	/**
	 * Restores the projection matrix to match the {@link Camera} saved with the
	 * {@link #pushCamera()} method.
	 */
	public void popCamera() {
		this.camera = cameraStack.pop();
		glProjection(camera);
	}

	/** Delegates to {@link #setCamera(Camera)} with null {@link Camera}. */
	public void resetProjection() { setCamera(null); }

	protected void glProjection(Camera c) {
		this.projection = new TransformMatrix();

		if (c instanceof PinholeCamera) {
			PinholeCamera camera = (PinholeCamera) c;

			float width = (float) camera.getProjectionWidth();
			float height = (float) camera.getProjectionHeight();
			projection = getPerspectiveMatrix(Math.toDegrees(camera.getFOV()[0]), width / height, 1, 1e9);
		}

		if (c instanceof OrthographicCamera) {
			OrthographicCamera camera = (OrthographicCamera) c;

			Vector cameraLocation = camera.getLocation();
			Vector cameraTarget = cameraLocation.add(camera.getViewingDirection());
			Vector up = camera.getUpDirection();

			gluLookAt(cameraLocation, cameraTarget, up.getX(), up.getY(), up.getZ());
		}

		glLoadIdentity();
	}

	@Deprecated
	public void setPerspective(double fovyInDegrees, double aspectRatio, double near, double far) {
		resetProjection();
		projection = getPerspectiveMatrix(fovyInDegrees, aspectRatio, near, far);
	}

	protected TransformMatrix getPerspectiveMatrix(double fovyInDegrees, double aspectRatio, double near, double far) {
		double ymax, xmax;
		ymax = near * Math.tan(fovyInDegrees * Math.PI / 360.0);
		// ymin = -ymax;
		// xmin = -ymax * aspectRatio;
		xmax = ymax * aspectRatio;
		return getFrustum(-xmax, xmax, -ymax, ymax, near, far);
	}

	private TransformMatrix getFrustum(double left, double right, double bottom, double top, double near, double far) {
		double temp, temp2, temp3, temp4;
		temp = 2.0 * near;
		temp2 = right - left;
		temp3 = top - bottom;
		temp4 = far - near;

		TransformMatrix t = new TransformMatrix(
				new double[][]{{temp / temp2, 0.0, (right + left) / temp2, 0.0},
						{0.0, temp / temp3, (top + bottom) / temp3, 0.0},
						{0.0, 0.0, (-far - near) / temp4, (-temp * far) / temp4},
						{0.0, 0.0, -1.0, 0.0}});
		return t;
	}

	// TODO  Should accept Plane instance
	public void glClipPlane(int plane, DoubleBuffer eqn) { gl.glClipPlane(plane, eqn); }

	public void glCullFace(int param) { gl.glCullFace(param); }

	public void glFrontFace(int param) { gl.glFrontFace(param); }

	public void glFlush() { gl.glFlush(); }

	@Deprecated
	public int glEnd() {
		gl.glEnd();
		return begins.pop();
	}

	@Deprecated
	public void endList() { gl.glEndList(); }

	@Deprecated
	public void glDisable(int code) { gl.glDisable(code); }

	@Deprecated
	public void glDisableClientState(int code) { gl.glDisableClientState(code); }

	@Deprecated
	public void hint(int param, int value) { gl.glHint(param, value); }

	public void hint(String param, String value) throws NoSuchFieldException, IllegalAccessException {
		gl.glHint(GL2.class.getField(param).getInt(gl), GL2.class.getField(value).getInt(gl));
	}

	public void wireCube(double size) {
		// TODO  Replace with RenderableCube
		glut.glutWireCube((float) size);
	}

	public void glutSolidSphere(double radius, int slices, int stacks) {
		int i, j;

    	/* Adjust z and radius as stacks are drawn. */
		double z1;
		double r1;

		int n = -slices;
		int size = Math.abs(n);
		double angle = 2 * Math.PI / (double) ((n == 0) ? 1 : n);
		double sint1[] = new double[size + 1];
		double cost1[] = new double[size + 1];
		circleTable(sint1, cost1, size, angle);

		n = stacks * 2;
		size = Math.abs(n);
		angle = 2 * Math.PI / (double) ((n == 0) ? 1 : n);
		double sint2[] = new double[size + 1];
		double cost2[] = new double[size + 1];
		circleTable(sint2, cost2, size, angle);

    	/* The top stack is covered with a triangle fan */

		double z0 = 1.0;
		z1 = cost2[(stacks > 0) ? 1 : 0];
		double r0 = 0.0;
		r1 = sint2[(stacks > 0) ? 1 : 0];

		glBegin(GL2.GL_TRIANGLE_FAN);

		glNormal(new Vector(0, 0, 1));
		glVertex(new Vector(0, 0, radius));

		for (j = slices; j >= 0; j--) {
			glNormal(new Vector(cost1[j] * r1, sint1[j] * r1, z1));
			glVertex(new Vector(cost1[j] * r1 * radius, sint1[j] * r1 * radius, z1 * radius));
		}

		glEnd();

    	/* Cover each stack with a quad strip, except the top and bottom stacks */

		for (i = 1; i < stacks - 1; i++) {
			z0 = z1;
			z1 = cost2[i + 1];
			r0 = r1;
			r1 = sint2[i + 1];

			glBegin(GL2.GL_QUAD_STRIP);

			for (j = 0; j <= slices; j++) {
				glNormal(new Vector(cost1[j] * r1, sint1[j] * r1, z1));
				glVertex(new Vector(cost1[j] * r1 * radius, sint1[j] * r1 * radius, z1 * radius));
				glNormal(new Vector(cost1[j] * r0, sint1[j] * r0, z0));
				glVertex(new Vector(cost1[j] * r0 * radius, sint1[j] * r0 * radius, z0 * radius));
			}

			glEnd();
		}

    	/* The bottom stack is covered with a triangle fan */

		z0 = z1;
		r0 = r1;

		glBegin(GL2.GL_TRIANGLE_FAN);

		glNormal(new Vector(0, 0, -1));
		glVertex(new Vector(0, 0, -radius));

		for (j = 0; j <= slices; j++) {
			glNormal(new Vector(cost1[j] * r0, sint1[j] * r0, z0));
			glVertex(new Vector(cost1[j] * r0 * radius, sint1[j] * r0 * radius, z0 * radius));
		}

		glEnd();
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

	public void requestFocus() {
		if (gl instanceof Component)
			((Component) gl).requestFocus();
	}

	protected GLException exceptionHelper(GLException gle) {
		if (gle.getMessage() == null) return gle;
		if (gle.getMessage().contains("May not call this between glBegin and glEnd")) {
			return new GLException(gle.getMessage() + " (begin = " + this.begins.peek() + ")");
		}

		return gle;
	}

	private static void circleTable(double sint[], double cost[], int size, double angle) {
		int i;

    	/* Compute cos and sin around the circle */
		sint[0] = 0.0;
		cost[0] = 1.0;

		for (i = 1; i < size; i++) {
			sint[i] = Math.sin(angle * i);
			cost[i] = Math.cos(angle * i);
		}

    	/* Last sample is duplicate of the first */
		sint[size] = sint[0];
		cost[size] =cost[0];
	}
}
