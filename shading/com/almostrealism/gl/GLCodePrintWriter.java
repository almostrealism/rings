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
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.texture.Texture;
import io.almostrealism.code.CodePrintWriter;
import io.almostrealism.code.Method;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBA;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * {@link GLCodePrintWriter} allows for convenient encoding of GL variables and methods using
 * the same methods that {@link com.almostrealism.renderable.Renderable#display(GLDriver)} uses
 * to display GL content. In many cases this means that passing a {@link GLCodePrintWriter} to
 * the {@link com.almostrealism.renderable.Renderable#display(GLDriver)} method will correctly
 * encode the GL content to the {@link CodePrintWriter} that is wrapped by this
 * {@link GLCodePrintWriter}.
 */
public class GLCodePrintWriter extends GLDriver {
	private String glMember, gluMember, glutMember, name;
	private CodePrintWriter p;

	public GLCodePrintWriter(String glMember, String gluMember, String glutMember, String name, CodePrintWriter p) {
		super(null);
		this.glMember = glMember;
		this.gluMember = gluMember;
		this.glutMember = glutMember;
		this.name = name;
		this.p = p;
	}

	@Override
	public void glColor(RGB color) {
		if (gl != null) super.glColor(color);

		if (enableDoublePrecision) {
			Variable<Double> r = new Variable<>("r", color.getRed());
			Variable<Double> g = new Variable<>("g", color.getGreen());
			Variable<Double> b = new Variable<>("b", color.getBlue());

			HashMap args = new HashMap<String, Variable<Double>>();
			args.put("red", r);
			args.put("green", g);
			args.put("blue", b);

			p.println(new Method(glMember, "glColor3d", Arrays.asList("red", "green", "blue"), args));
		} else {
			Variable<Float> r = new Variable<>("r", (float) color.getRed());
			Variable<Float> g = new Variable<>("g", (float) color.getGreen());
			Variable<Float> b = new Variable<>("b", (float) color.getBlue());

			HashMap args = new HashMap<String, Variable<Double>>();
			args.put("red", r);
			args.put("green", g);
			args.put("blue", b);

			p.println(new Method(glMember, "glColor3f", Arrays.asList("red", "green", "blue"), args));
		}
	}

	@Override
	public void glColor(RGBA color) {
		if (gl != null) super.glColor(color);

		if (enableDoublePrecision) {
			Variable<Double> r = new Variable<>("r", color.getRed());
			Variable<Double> g = new Variable<>("g", color.getGreen());
			Variable<Double> b = new Variable<>("b", color.getBlue());
			Variable<Double> a = new Variable<>("a", color.getAlpha());

			HashMap args = new HashMap<String, Variable<Double>>();
			args.put("red", r);
			args.put("green", g);
			args.put("blue", b);
			args.put("alpha", a);

			p.println(new Method(glMember, "glColor4d", Arrays.asList("red", "green", "blue", "alpha"), args));
		} else {
			Variable<Float> r = new Variable<>("r", (float) color.getRed());
			Variable<Float> g = new Variable<>("g", (float) color.getGreen());
			Variable<Float> b = new Variable<>("b", (float) color.getBlue());
			Variable<Float> a = new Variable<>("a", (float) color.getAlpha());

			HashMap args = new HashMap<String, Variable<Double>>();
			args.put("red", r);
			args.put("green", g);
			args.put("blue", b);
			args.put("alpha", a);

			p.println(new Method(glMember, "glColor4f", Arrays.asList("red", "green", "blue", "alpha"), args));
		}
	}

	/** It is recommended to use {@link #glColor(RGB)} instead. */
	@Override
	public void glColorPointer(GLArrayDataWrapper data) {
		if (gl != null) super.glColorPointer(data);
		throw new NotImplementedException("glColorPointer");
	}

	@Override
	@Deprecated public void glMaterial(int code, int prop, FloatBuffer buf) {
		if (gl != null) super.glMaterial(code, prop, buf);
		throw new NotImplementedException("glMaterial");
	}

	@Override
	@Deprecated public void glMaterial(int code, int param, float f[], int i) {
		if (gl != null) super.glMaterial(code, param, f, i);
		throw new NotImplementedException("glMaterial");
	}

	@Override
	@Deprecated public void glMaterial(int code, int param, double f) {
		if (gl != null) super.glMaterial(code, param, f);
		throw new NotImplementedException("glMaterial");
	}

	@Override
	public void glMaterial(GLMaterial mat) {
		if (gl != null) super.glMaterial(mat);

		p.println(glMethod("glMaterialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_AMBIENT", GL2.GL_AMBIENT),
								new Variable<>("ambient", Scalar.toFloat(mat.ambient.toArray())),
								new Variable<>("zero", 0))));
		p.println(glMethod("glMaterialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_DIFFUSE", GL2.GL_DIFFUSE),
								new Variable<>("diffuse", Scalar.toFloat(mat.diffuse.toArray())),
								new Variable<>("zero", 0))));
		p.println(glMethod("glMaterialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_SPECULAR", GL2.GL_SPECULAR),
								new Variable("specular", Scalar.toFloat(mat.specular.toArray())),
								new Variable("zero", 0))));
		p.println(glMethod("glMaterialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable("GL_SHININESS", GL2.GL_SHININESS),
								new Variable("shininess", new float[] { (float) mat.shininess.getValue() }),
								new Variable("zero", 0))));
	}

	/** It is recommended to use {@link #glMaterial(GLMaterial)}. */
	@Override
	@Deprecated public void glColorMaterial(int param, int value) {
		if (gl != null) super.glColorMaterial(param, value);
		throw new NotImplementedException("glColorMaterial");
	}

	@Override
	public void glInitNames() {
		if (gl != null) super.glInitNames();
		throw new NotImplementedException("glInitNames");
	}

	@Override
	public void glLoadName(int name) {
		if (gl != null) super.glLoadName(name);
		throw new NotImplementedException("glLoadName");
	}

	@Override
	public void glPushName(int name) {
		if (gl != null) super.glPushName(name);
		throw new NotImplementedException("glPushName");
	}

	@Override
	public void glGenTextures(int code, int textures[]) {
		if (gl != null) super.glGenTextures(code, textures);
		p.println(glMethod("glGenTextures",
				Arrays.asList(new Variable<>("code", code), new Variable<>("textures", textures))));
	}

	@Override
	public void bindTexture(Texture t) {
		if (gl != null) super.bindTexture(t);
		throw new NotImplementedException("bindTextures");
	}

	@Override
	public void glBindTexture(int code, int tex) {
		if (gl != null) super.glBindTexture(code, tex);
		p.println(glMethod("glBindTexture",
				Arrays.asList(new Variable<>("code", code), new Variable<>("tex", tex))));
	}

	@Override
	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, byte buf[]) {
		if (gl != null) super.glTexImage2D(a, b, c, d, e, f, g, h, buf);
		p.println(glMethod("glTexImage2D",
				Arrays.asList(new Variable<>("a", a),
								new Variable<>("b", b),
								new Variable<>("c", c),
								new Variable<>("d", d),
								new Variable<>("e", e),
								new Variable<>("f", f),
								new Variable<>("g", g),
								new Variable<>("h", h),
								new Variable<>("buf", buf))));
	}

	@Override
	public void glTexGeni(int a, int b, int c) {
		if (gl != null) super.glTexGeni(a, b, c);
		throw new NotImplementedException("glTexGeni");
	}

	@Override
	public void glTexEnvi(int a, int b, int c) {
		if (gl != null) super.glTexEnvi(a, b, c);
		throw new NotImplementedException("glTexEnvi");
	}

	@Override
	public void glTexEnvf(int a, int b, float f) {
		if (gl != null) super.glTexEnvf(a, b, f);
		throw new NotImplementedException("glTexEnvf");
	}

	@Override
	public void glTexParameter(int code, int param, int value) {
		if (gl != null) super.glTexParameter(code, param, value);
		p.println(glMethod("glTexParameter",
				Arrays.asList(new Variable<>("code", code),
								new Variable<>("param", param),
								new Variable<>("value", value))));
	}

	@Override
	public void glLineWidth(double width) {
		if (gl != null) super.glLineWidth(width);
		throw new NotImplementedException("glLineWidth");
	}

	@Override
	public void glPointSize(double size) {
		if (gl != null) super.glPointSize(size);
		throw new NotImplementedException("glPointSize");
	}

	@Override
	public void glutBitmapCharacter(int font, char c) {
		if (glut != null) super.glutBitmapCharacter(font, c);
		throw new NotImplementedException("glutBitmapCharacter");
	}

	@Override
	public void enableTexture(Texture t) {
		if (gl != null) super.enableTexture(t);
		throw new NotImplementedException("enableTexture");
	}

	@Override
	public void glActiveTexture(int code) {
		if (gl != null) super.glActiveTexture(code);
		throw new NotImplementedException("glActiveTexture");
	}

	@Override
	public void glVertex(Vector v) {
		if (gl != null) super.glVertex(v);

		if (enableDoublePrecision) {
			p.println(glMethod("glVertex3d",
					Arrays.asList(new Variable<>("x", v.getX()),
								new Variable<>("y", v.getY()),
								new Variable<>("z", v.getZ()))));
		} else {
			p.println(glMethod("glVertex3f",
					Arrays.asList(new Variable<>("x", (float) v.getX()),
								new Variable<>("y", (float) v.getY()),
								new Variable<>("z", (float) v.getZ()))));
		}
	}

	@Override
	public void glVertex(Pair p) {
		if (gl != null) super.glVertex(p);

		if (enableDoublePrecision) {
			this.p.println(glMethod("glVertex2d",
					Arrays.asList(new Variable<>("x", p.getX()),
							new Variable<>("y", p.getY()))));
		} else {
			this.p.println(glMethod("glVertex2f",
					Arrays.asList(new Variable<>("x", (float) p.getX()),
							new Variable<>("y", (float) p.getY()))));
		}
	}

	@Override
	@Deprecated public void glVertexPointer(int a, int b, int c, FloatBuffer f) {
		if (gl != null) super.glVertexPointer(a, b, c, f);
		throw new NotImplementedException("glVertexPointer");
	}

	@Override
	@Deprecated public void glVertexPointer(GLArrayDataWrapper data) {
		if (gl != null) super.glVertexPointer(data);
		throw new NotImplementedException("glVertexPointer");
	}

	@Override
	public void glNormal(Vector n) {
		if (gl != null) super.glNormal(n);

		if (enableDoublePrecision) {
			p.println(glMethod("glNormal3d",
					Arrays.asList(new Variable<>("x", n.getX()),
							new Variable<>("y", n.getY()),
							new Variable<>("z", n.getZ()))));
		} else {
			p.println(glMethod("glNormal3f",
					Arrays.asList(new Variable<>("x", (float) n.getX()),
							new Variable<>("y", (float) n.getY()),
							new Variable<>("z", (float) n.getZ()))));
		}
	}

	@Override
	public void glNormalPointer(GLArrayDataWrapper data) {
		if (gl != null) super.glNormalPointer(data);
		throw new NotImplementedException("glNormalPointer");
	}

	@Override
	public void glLight(int light, int prop, FloatBuffer buf) {
		if (gl != null) super.glLight(light, prop, buf);
		throw new NotImplementedException("glLight");
	}

	@Override
	public void glLight(int light, int prop, float f) {
		if (gl != null) super.glLight(light, prop, f);
		throw new NotImplementedException("glLight");
	}

	@Override
	@Deprecated public void glLight(int light, int prop, float f[], int a) {
		if (gl != null) super.glLight(light, prop, f, a);
		throw new NotImplementedException("glLight");
	}

	/** It is recommended to use {@link org.almostrealism.color.Light}s instead. */
	@Override
	@Deprecated public void glLightModel(int code, RGBA color) {
		if (gl != null) super.glLightModel(code, color);
		throw new NotImplementedException("glLightModel is deprecated in OpenGL");
	}

	@Override
	public void glTranslate(Vector t) {
		if (gl != null) super.glTranslate(t);

		if (enableDoublePrecision) {
			p.println(glMethod("glTranslated",
					Arrays.asList(new Variable<>("x", t.getX()),
							new Variable<>("y", t.getY()),
							new Variable<>("z", t.getZ()))));
		} else {
			p.println(glMethod("glTranslatef",
					Arrays.asList(new Variable<>("x", (float) t.getX()),
							new Variable<>("y", (float) t.getY()),
							new Variable<>("z", (float) t.getZ()))));
		}
	}

	@Override
	public void glScale(Vector s) {
		if (gl != null) super.glScale(s);

		if (enableDoublePrecision) {
			p.println(glMethod("glScaled",
					Arrays.asList(new Variable<>("x", s.getX()),
							new Variable<>("y", s.getY()),
							new Variable<>("z", s.getZ()))));
		} else {
			p.println(glMethod("glScalef",
					Arrays.asList(new Variable<>("x", (float) s.getX()),
							new Variable<>("y", (float) s.getY()),
							new Variable<>("z", (float) s.getZ()))));
		}
	}

	@Override
	public void glScale(double s) {
		glScale(new Vector(s, s, s));
	}

	@Override
	public void glRotate(double a, double b, double c, double d) {
		if (gl != null) super.glRotate(a, b, c, d);
		throw new NotImplementedException("glRotate");
	}

	@Override
	public void glAccum(int param, double value) {
		if (gl != null) super.glAccum(param, value);
		throw new NotImplementedException("glAccum");
	}

	@Override
	public void glColorMask(boolean a, boolean b, boolean c, boolean d) {
		if (gl != null) super.glColorMask(a, b, c, d);
		throw new NotImplementedException("glColorMask");
	}

	@Override
	public void clearColorBuffer() {
		if (gl != null) super.clearColorBuffer();
		throw new NotImplementedException("clearColorBuffer");
	}

	@Override
	public void glClearColor(RGBA c) {
		if (gl != null) super.glClearColor(c);
		p.println(glMethod("glClearColor",
						Arrays.asList(new Variable("r", c.r()),
									new Variable("g", c.g()),
									new Variable("b", c.b()),
									new Variable("a", c.a()))));
	}

	@Override
	public void glClearAccum(RGBA c) {
		if (gl != null) super.glClearAccum(c);
		throw new NotImplementedException("glClearAccum");
	}

	@Override
	public void glDepthFunc(int code) {
		if (gl != null) super.glDepthFunc(code);
		p.println(glMethod("glDepthFunc", Arrays.asList(new Variable<>("code", code))));
	}

	@Override
	public void glStencilFunc(int param, int a, int b) {
		if (gl != null) super.glStencilFunc(param, a, b);
		throw new NotImplementedException("glStencilFunc");
	}

	@Override
	public void glStencilOp(int a, int b, int c) {
		if (gl != null) super.glStencilOp(a, b, c);
		throw new NotImplementedException("glStencilOp");
	}

	@Override
	public void glClearDepth(double d) {
		if (gl != null) super.glClearDepth(d);

		if (enableDoublePrecision) {
			p.println(glMethod("glClearDepth", Arrays.asList(new Variable<>("d", d))));
		} else {
			p.println(glMethod("glClearDepthf", Arrays.asList(new Variable<>("d", d))));
		}
	}

	@Override
	public void glClearStencil(int param) {
		if (gl != null) super.glClearStencil(param);
		p.println(glMethod("glClearStencil", Arrays.asList(new Variable<>("param", param))));
	}

	@Override
	@Deprecated public void glClear(int bits) {
		if (gl != null) super.glClear(bits);
	}

	@Override
	public void setFog(FogParameters f) {
		if (gl != null) super.setFog(f);
		throw new NotImplementedException("setFog");
	}

	@Override
	public void glQuads() {
		if (gl != null) super.glQuads();
		throw new NotImplementedException("glQuads");
	}

	@Override
	public void glBegin(int code) {
		if (gl != null) {
			super.glBegin(code);
		} else {
			begins.push(code);
		}

		throw new NotImplementedException("glBegin");
	}

	@Override
	public void glEnable(int code) {
		if (gl != null) super.glEnable(code);
		p.println(glMethod("glEnable", Arrays.asList(new Variable<>("code", code))));
	}

	@Override
	public void glEnableClientState(int code) {
		if (gl != null) super.glEnableClientState(code);
		throw new NotImplementedException("glEnableClientState");
	}

	@Override
	public void glBlendFunc(int c1, int c2) {
		if (gl != null) super.glBlendFunc(c1, c2);
		p.println(glMethod("glBlendFunc",
				Arrays.asList(new Variable<>("c1", c1), new Variable<>("c2", c2))));
	}

	@Override
	public void glShadeModel(int model) {
		if (gl != null) super.glShadeModel(model);
		throw new NotImplementedException("glShadeModel");
	}

	@Override
	public int glRenderMode(int mode) {
		if (gl != null) super.glRenderMode(mode);
		throw new NotImplementedException("glRenderMode");
	}

	@Override
	public void glPushAttrib(int attrib) {
		if (gl != null) super.glPushAttrib(attrib);
		throw new NotImplementedException("glPushAttrib");
	}

	@Override
	public void glPopAttrib() {
		if (gl != null) super.glPopAttrib();
		throw new NotImplementedException("glPopAttrib");
	}

	@Override
	public void glGenBuffers(int a, int b[], int c) {
		if (gl != null) super.glGenBuffers(a, b, c);
		throw new NotImplementedException("glGenBuffers");
	}

	@Override
	public void glBindBuffer(int code, int v) {
		if (gl != null) super.glBindBuffer(code, v);
		throw new NotImplementedException("glBindBuffer");
	}

	@Override
	public void glBufferData(int code, int l, ByteBuffer buf, int d) {
		if (gl != null) super.glBufferData(code, l, buf, d);
		throw new NotImplementedException("glBufferData");
	}

	@Override
	public void glSelectBuffer(int size, IntBuffer buf) {
		if (gl != null) super.glSelectBuffer(size, buf);
		throw new NotImplementedException("glSelectBuffer");
	}

	@Override
	public int glGenLists(int code) {
		if (gl != null) super.glGenLists(code);
		throw new NotImplementedException("glGenLists");
	}

	@Override
	public void glCallList(int list) {
		if (gl != null) super.glCallList(list);
		throw new NotImplementedException("glCallList");
	}

	@Override
	public void glNewList(int list, int code) {
		if (gl != null) super.glNewList(list, code);
		throw new NotImplementedException("glNewList");
	}

	@Override
	public void uv(Pair texCoord) {
		if (enableDoublePrecision) {
			p.println(glMethod("glTexCoord2d",
						Arrays.asList(new Variable<>("u", texCoord.getA()),
									new Variable<>("v", texCoord.getB()))));
		} else {
			p.println(glMethod("glTexCoord2f",
					Arrays.asList(new Variable<>("u", (float) texCoord.getA()),
									new Variable<>("v", (float) texCoord.getB()))));
		}
	}

	@Override
	public void glMatrixMode(int code) {
		if (gl != null) super.glMatrixMode(code);
		p.println(glMethod("glMatrixMode", Arrays.asList(new Variable<>("code", code))));
	}

	@Override
	public void glModelView() {
		if (gl != null) super.glModelView();
		p.println(glMethod("glMatrixMode",
					Arrays.asList(new Variable<>("GL_MODELVIEW", GL2.GL_MODELVIEW))));
	}

	@Override
	public void glPushMatrix() {
		if (gl != null) super.glPushMatrix();
		p.println(glMethod("glPushMatrix", Arrays.asList()));
	}

	@Override
	public void glPopMatrix() {
		if (gl != null) super.glPopMatrix();
		p.println(glMethod("glPopMatrix", Arrays.asList()));
	}

	@Override
	public void glLoadIdentity() {
		if (gl != null) super.glLoadIdentity();
		p.println(glMethod("glLoadIdentity", Arrays.asList()));
	}

	@Override
	@Deprecated public void glMultMatrix(FloatBuffer mat) {
		if (gl != null) super.glMultMatrix(mat);
		throw new NotImplementedException("glMultMatrix");
	}

	@Override
	public void glRasterPos(Vector pos) {
		if (gl != null) super.glRasterPos(pos);
		throw new NotImplementedException("glRasterPos");
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Override
	@Deprecated public void glViewport(int x, int y, int w, int h) {
		if (gl != null) super.glViewport(x, y, w, h);
		throw new NotImplementedException("glViewport");
	}

	/** It is recommended to use a {@link PinholeCamera} instead. */
	@Override
	@Deprecated public void gluPerspective(double d1, double d2, double d3, double d4) {
		if (glu != null) super.gluPerspective(d1, d2, d3, d4);
		p.println(gluMethod("gluPerspective",
					Arrays.asList(new Variable<>("d1", d1),
								new Variable<>("d2", d2),
								new Variable<>("d3", d3),
								new Variable<>("d4", d4))));
	}

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Override
	@Deprecated public void gluOrtho2D(double a, double b, double c, double d) {
		if (glu != null) super.gluOrtho2D(a, b, c, d);
		throw new NotImplementedException("gluOrtho2D");
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Override
	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
		if (glu != null) super.gluLookAt(e, c, var13, var15, var17);
		p.println(gluMethod("gluLookAt",
							Arrays.asList(new Variable<>("ex", (float) e.getX()),
										new Variable<>("ey", (float) e.getY()),
										new Variable("ez", (float) e.getZ()),
										new Variable("cx", (float) c.getX()),
										new Variable("cy", (float) c.getY()),
										new Variable("cz", (float) c.getZ()),
										new Variable("var13", (float) var13),
										new Variable("var15", (float) var15),
										new Variable("var17", (float) var17))));
	}

	@Override
	public boolean gluUnProject(Vector w, double modelview[], double projection[], int viewport[], Vector worldpos) {
		if (glu != null) super.gluUnProject(w, modelview, projection, viewport, worldpos);
		throw new NotImplementedException("gluUnProject");
	}

	@Override
	public boolean gluUnProject(Vector w, Vector worldpos) {
		if (glu != null) super.gluUnProject(w, worldpos);
		throw new NotImplementedException("gluUnProject");
	}

	@Override
	public void gluPickMatrix(float x, float y, float w, float h, int viewport[]) {
		if (glu != null) super.gluPickMatrix(x, y, w, h, viewport);
		throw new NotImplementedException("gluPickMatrix");
	}

	@Override
	public void glProjection(Camera c) {
		if (gl != null) super.glProjection(c);

		p.println(glMethod("glMatrixMode",
						Arrays.asList(new Variable<>("GL_PROJECTION", GL2.GL_PROJECTION))));
		p.println(glMethod("glLoadIdentity"));

		if (c instanceof PinholeCamera) {
			PinholeCamera camera = (PinholeCamera) c;

			float width = (float) camera.getProjectionWidth();
			float height = (float) camera.getProjectionHeight();
			p.println(gluMethod("gluPerspective",
					Arrays.asList(new Variable<>("fov", Math.toDegrees(camera.getFOV()[0])),
								new Variable<>("aspect",width / height),
								new Variable<>("min", 1),
								new Variable<>("max", 1e9))));
		}

		if (c instanceof OrthographicCamera) {
			OrthographicCamera camera = (OrthographicCamera) c;

			Vector cameraLocation = camera.getLocation();
			Vector cameraTarget = cameraLocation.add(camera.getViewingDirection());
			Vector up = camera.getUpDirection();

			gluLookAt(cameraLocation, cameraTarget, up.getX(), up.getY(), up.getZ());
		}

		p.println(glMethod("glMatrixMode",
						Arrays.asList(new Variable<>("GL_MODELVIEW", GL2.GL_MODELVIEW))));
		p.println(glMethod("glLoadIdentity"));
	}

	@Override
	public void glClipPlane(int plane, DoubleBuffer eqn) {
		if (gl != null) super.glClipPlane(plane, eqn);
		throw new NotImplementedException("glClipPlane");
	}

	@Override
	public void glCullFace(int param) {
		if (gl != null) super.glCullFace(param);
		throw new NotImplementedException("glCullFace");
	}

	@Override
	public void glFrontFace(int param) {
		if (gl != null) super.glFrontFace(param);
		throw new NotImplementedException("glFrontFace");
	}

	@Override
	public void glFlush() {
		if (gl != null) super.glFlush();
		throw new NotImplementedException("glFlush");
	}

	@Override
	public int glEnd() {
		if (gl != null) {
			super.glEnd();
		} else {
			begins.pop();
		}

		throw new NotImplementedException("glEnd");
	}

	@Override
	public void glEndList() {
		if (gl != null) super.glEndList();
		throw new NotImplementedException("glEndList");
	}

	@Override
	@Deprecated public void glDisable(int code) {
		if (gl != null) super.glDisable(code);
		throw new NotImplementedException("glDisable");
	}

	@Override
	@Deprecated public void glDisableClientState(int code) {
		if (gl != null) super.glDisableClientState(code);
		throw new NotImplementedException("glDisableClientState");
	}

	@Override
	public void glHint(int param, int value) {
		if (gl != null) super.glHint(param, value);
		p.println(glMethod("glHint",
				Arrays.asList(new Variable("param", param), new Variable("value", value))));
	}

	@Override
	public void glutWireCube(double size) {
		if (glut != null) super.glutWireCube(size);
		throw new NotImplementedException("glutWireCube");
	}

	@Override
	public void glutSolidSphere(double radius, int slices, int stacks) {
		if (glut != null) super.glutSolidSphere(radius, slices, stacks);
		throw new NotImplementedException("glutSolidSphere");
	}

	@Override
	public void glDrawArrays(int code, int a, int b) {
		if (gl != null) super.glDrawArrays(code, a, b);
		throw new NotImplementedException("glDrawArrays");
	}

	protected Method glMethod(String name) {
		return glMethod(glMember, name, new ArrayList<>());
	}

	protected Method glMethod(String name, List<Variable> args) {
		return glMethod(glMember, name, args);
	}

	protected Method gluMethod(String name, List<Variable> args) {
		return glMethod(gluMember, name, args);
	}

	protected Method glutMethod(String name, List<Variable> args) {
		return glMethod(glutMember, name, args);
	}

	protected static Method glMethod(String glMember, String name, List<Variable> args) {
		List<String> argList = new ArrayList<>();
		for (int i = 0; i < args.size(); i++) {
			argList.add(String.valueOf(i));
		}

		Map<String, Variable> argMap = new HashMap<>();
		for (int i = 0; i < args.size(); i++) {
			argMap.put(String.valueOf(i), args.get(i));
		}

		return new Method(glMember, name, argList, argMap);
	}
}
