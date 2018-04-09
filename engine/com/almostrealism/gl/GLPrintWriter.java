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
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.texture.Texture;
import io.almostrealism.code.CodePrintWriter;
import io.almostrealism.code.InstanceReference;
import io.almostrealism.code.Method;
import io.almostrealism.code.Variable;
import io.almostrealism.js.JavaScriptPrintWriter;
import org.almostrealism.algebra.*;
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
 * {@link GLPrintWriter} allows for convenient encoding of GL variables and methods using
 * the same methods that {@link com.almostrealism.renderable.Renderable#display(GLDriver)} uses
 * to display GL content. In many cases this means that passing a {@link GLPrintWriter} to
 * the {@link com.almostrealism.renderable.Renderable#display(GLDriver)} method will correctly
 * encode the GL content to the {@link CodePrintWriter} that is wrapped by this
 * {@link GLPrintWriter}.
 */
public class GLPrintWriter extends GLDriver {
	private String glMember, gluMember, glutMember;
	private String name; // TODO Use name
	private CodePrintWriter p;

	public GLPrintWriter(String glMember, String gluMember, String glutMember, String name, CodePrintWriter p) {
		super(null);
		this.glMember = glMember;
		this.gluMember = gluMember;
		this.glutMember = glutMember;
		this.name = name;
		this.p = p;
		this.enableDoublePrecision = p instanceof JavaScriptPrintWriter;
	}

	@Override
	public void glColor(RGB color) {
		if (gl != null) super.glColor(color);

		if (enableDoublePrecision) {
			p.println(glMethod("color3d",
								new Variable<>("r", color.getRed()),
								new Variable<>("g", color.getGreen()),
								new Variable<>("b", color.getBlue())));
		} else {
			p.println(glMethod("color3f",
								new Variable<>("r", (float) color.getRed()),
								new Variable<>("g", (float) color.getGreen()),
								new Variable<>("b", (float) color.getBlue())));
		}
	}

	@Override
	public void glColor(RGBA color) {
		if (gl != null) super.glColor(color);

		if (enableDoublePrecision) {
			p.println(glMethod("color4d",
								new Variable<>("r", color.getRed()),
								new Variable<>("g", color.getGreen()),
								new Variable<>("b", color.getBlue()),
								new Variable<>("a", color.getAlpha())));
		} else {
			p.println(glMethod("color4f",
								new Variable<>("r", (float) color.getRed()),
								new Variable<>("g", (float) color.getGreen()),
								new Variable<>("b", (float) color.getBlue()),
								new Variable<>("a", (float) color.getAlpha())));
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

		p.println(glMethod("materialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_AMBIENT", GL2.GL_AMBIENT),
								new Variable<>("ambient", Scalar.toFloat(mat.ambient.toArray())),
								new Variable<>("zero", 0))));
		p.println(glMethod("materialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_DIFFUSE", GL2.GL_DIFFUSE),
								new Variable<>("diffuse", Scalar.toFloat(mat.diffuse.toArray())),
								new Variable<>("zero", 0))));
		p.println(glMethod("materialfv",
				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
								new Variable<>("GL_SPECULAR", GL2.GL_SPECULAR),
								new Variable("specular", Scalar.toFloat(mat.specular.toArray())),
								new Variable("zero", 0))));
		p.println(glMethod("materialfv",
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
	public void genTextures(int count, int textures[]) {
		if (gl != null) super.genTextures(count, textures);
//		p.println(glMethod("genTextures",
//							new Variable<>("count", count),
//							new Variable<>("textures", textures)));
		System.out.println("GLPrintWriter[WARN]: genTextures is not supported by some versions of OpenGL. Use Texture type instead.");
		// TODO  This should be an exception
	}

	@Override
	public void bindTexture(Texture t) {
		if (gl != null) super.bindTexture(t);
		throw new NotImplementedException("bindTextures");
	}

	@Override
	public void bindTexture(String code, int tex) {
		if (gl != null) super.bindTexture(code, tex);
		p.println(glMethod("bindTexture",
						new InstanceReference(glMember + "." + code),
						new Variable<>("tex", tex)));
	}

	@Override
	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, byte buf[]) {
		if (gl != null) super.glTexImage2D(a, b, c, d, e, f, g, h, buf);
		p.println(glMethod("texImage2D",
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
		throw new NotImplementedException("texGeni");
	}

	@Override
	public void glTexGen(int a, int b, float f) {
		if (gl != null) super.glTexGen(a, b, f);
		throw new NotImplementedException("texGen");
	}

	@Override
	public void glTexGen(int a, int b, float f[], int index) {
		if (gl != null) super.glTexGen(a, b, f, index);
		throw new NotImplementedException("texGen");
	}

	@Override
	public void glTexEnvi(int a, int b, int c) {
		if (gl != null) super.glTexEnvi(a, b, c);
		throw new NotImplementedException("texEnvi");
	}

	@Override
	public void glTexEnvf(int a, int b, float f) {
		if (gl != null) super.glTexEnvf(a, b, f);
		throw new NotImplementedException("texEnvf");
	}

	@Override
	public void glTexParameter(int code, int param, int value) {
		if (gl != null) super.glTexParameter(code, param, value);
		p.println(glMethod("texParameter",
				Arrays.asList(new Variable<>("code", code),
								new Variable<>("param", param),
								new Variable<>("value", value))));
	}

	@Override
	public void glLineWidth(double width) {
		if (gl != null) super.glLineWidth(width);
		throw new NotImplementedException("lineWidth");
	}

	@Override
	public void glPointSize(double size) {
		if (gl != null) super.glPointSize(size);
		throw new NotImplementedException("pointSize");
	}

	@Override
	public void glutBitmapCharacter(int font, char c) {
		if (glut != null) super.glutBitmapCharacter(font, c);
		throw new NotImplementedException("bitmapCharacter");
	}

	@Override
	public void enableTexture(Texture t) {
		if (gl != null) super.enableTexture(t);
		throw new NotImplementedException("enableTexture");
	}

	@Override
	public void disableTexture(Texture t) {
		if (gl != null) super.disableTexture(t);
		throw new NotImplementedException("disableTexture");
	}

	@Override
	public void glActiveTexture(int code) {
		if (gl != null) super.glActiveTexture(code);
		throw new NotImplementedException("activeTexture");
	}

	@Override
	public void glVertex(Vector v) {
		if (gl != null) super.glVertex(v);

		if (enableDoublePrecision) {
			p.println(glMethod("vertex3d",
					Arrays.asList(new Variable<>("x", v.getX()),
								new Variable<>("y", v.getY()),
								new Variable<>("z", v.getZ()))));
		} else {
			p.println(glMethod("vertex3f",
					Arrays.asList(new Variable<>("x", (float) v.getX()),
								new Variable<>("y", (float) v.getY()),
								new Variable<>("z", (float) v.getZ()))));
		}
	}

	@Override
	public void glVertex(Pair p) {
		if (gl != null) super.glVertex(p);

		if (enableDoublePrecision) {
			this.p.println(glMethod("vertex2d",
					Arrays.asList(new Variable<>("x", p.getX()),
							new Variable<>("y", p.getY()))));
		} else {
			this.p.println(glMethod("vertex2f",
					Arrays.asList(new Variable<>("x", (float) p.getX()),
							new Variable<>("y", (float) p.getY()))));
		}
	}

	@Override
	@Deprecated public void glVertexPointer(int a, int b, int c, FloatBuffer f) {
		if (gl != null) super.glVertexPointer(a, b, c, f);
		throw new NotImplementedException("vertexPointer");
	}

	@Override
	@Deprecated public void glVertexPointer(GLArrayDataWrapper data) {
		if (gl != null) super.glVertexPointer(data);
		throw new NotImplementedException("vertexPointer");
	}

	@Override
	public void glNormal(Vector n) {
		if (gl != null) super.glNormal(n);

		if (enableDoublePrecision) {
			p.println(glMethod("normal3d",
					Arrays.asList(new Variable<>("x", n.getX()),
							new Variable<>("y", n.getY()),
							new Variable<>("z", n.getZ()))));
		} else {
			p.println(glMethod("normal3f",
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
	@Deprecated public void glLight(int light, int prop, FloatBuffer buf) {
		if (gl != null) super.glLight(light, prop, buf);
		throw new NotImplementedException("glLight is deprecated in OpenGL");
	}

	@Override
	@Deprecated public void glLight(int light, int prop, float f) {
		if (gl != null) super.glLight(light, prop, f);
		throw new NotImplementedException("glLight is deprecated in OpenGL");
	}

	@Override
	@Deprecated public void glLight(int light, int prop, float f[], int a) {
		if (gl != null) super.glLight(light, prop, f, a);
		throw new NotImplementedException("glLight is deprecated in OpenGL");
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
			p.println(glMethod("translated",
					Arrays.asList(new Variable<>("x", t.getX()),
							new Variable<>("y", t.getY()),
							new Variable<>("z", t.getZ()))));
		} else {
			p.println(glMethod("translatef",
					Arrays.asList(new Variable<>("x", (float) t.getX()),
							new Variable<>("y", (float) t.getY()),
							new Variable<>("z", (float) t.getZ()))));
		}
	}

	@Override
	public void glScale(Vector s) {
		if (gl != null) super.glScale(s);

		if (enableDoublePrecision) {
			p.println(glMethod("scaled",
					Arrays.asList(new Variable<>("x", s.getX()),
							new Variable<>("y", s.getY()),
							new Variable<>("z", s.getZ()))));
		} else {
			p.println(glMethod("scalef",
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
	public void glAccum(int param, double value) {
		if (gl != null) super.glAccum(param, value);
		throw new NotImplementedException("accum");
	}

	@Override
	public void glColorMask(boolean r, boolean g, boolean b, boolean a) {
		if (gl != null) super.glColorMask(r, g, b, a);
		p.println(glMethod("colorMask",
						new Variable<>("r", r),
						new Variable<>("g", g),
						new Variable<>("b", b),
						new Variable<>("a", a)));
	}

	@Override
	public void clearColorBuffer() {
		if (gl != null) super.clearColorBuffer();
		throw new NotImplementedException("clearColorBuffer");
	}

	@Override
	public void glClearColor(RGBA c) {
		if (gl != null) super.glClearColor(c);
		p.println(glMethod("clearColor",
						Arrays.asList(new Variable("r", c.r()),
									new Variable("g", c.g()),
									new Variable("b", c.b()),
									new Variable("a", c.a()))));
	}

	@Override
	public void glClearAccum(RGBA c) {
		if (gl != null) super.glClearAccum(c);
		throw new NotImplementedException("clearAccum");
	}

	@Override
	public void glDepthFunc(int code) {
		if (gl != null) super.glDepthFunc(code);
		p.println(glMethod("depthFunc", Arrays.asList(new Variable<>("code", code))));
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		if (gl != null) super.glStencilFunc(func, ref, mask);
		p.println(glMethod("stencilFunc",
							new Variable<>("func", func),
							new Variable<>("ref", ref),
							new Variable<>("mask", mask)));
	}

	@Override
	public void glStencilOp(int sfail, int dpfail, int dppass) {
		if (gl != null) super.glStencilOp(sfail, dpfail, dppass);
		p.println(glMethod("sStencilOp",
							new Variable<>("sfail", sfail),
							new Variable<>("dpfail", dpfail),
							new Variable<>("dppass", dppass)));
	}

	@Override
	public void clearDepth(double d) {
		if (gl != null) super.clearDepth(d);

		if (enableDoublePrecision) {
			p.println(glMethod("clearDepth", Arrays.asList(new Variable<>("d", d))));
		} else {
			p.println(glMethod("clearDepthf", Arrays.asList(new Variable<>("d", d))));
		}
	}

	@Override
	public void glClearStencil(int param) {
		if (gl != null) super.glClearStencil(param);
		p.println(glMethod("clearStencil", Arrays.asList(new Variable<>("param", param))));
	}

	@Override
	@Deprecated public void glClear(int bits) {
		if (gl != null) super.glClear(bits);
	}

	@Override
	public void setFog(FogParameters f) {
		if (gl != null) super.setFog(f);

//		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP);
//		gl.glFogfv(GL2.GL_FOG_COLOR, FloatBuffer.wrap(Scalar.toFloat(f.fogColor.toArray())));
//		gl.glFogf(GL2.GL_FOG_DENSITY, (float) f.fogDensity);
//		gl.glFogf(GL2.GL_FOG_START, 0.0f);
//		gl.glFogf(GL2.GL_FOG_END, Float.MAX_VALUE);
		enable(GL2.GL_FOG);

		throw new NotImplementedException("setFog");
	}

	@Override
	public void glQuads() {
		if (gl != null) super.glQuads();
		throw new NotImplementedException("quads");
	}

	@Override
	public void glBegin(int code) {
		if (gl != null) {
			super.glBegin(code);
		} else {
			begins.push(code);
		}

		p.println(glMethod("begin", new Variable<>("code", code)));
	}

	@Override
	@Deprecated
	public void enable(int code) {
		if (gl != null) super.enable(code);
		throw new NotImplementedException("enable");
	}

	@Override
	public void enable(String code) {
		if (gl != null) super.enable(code);
		p.println(glMethod("enable", new InstanceReference(glMember + "." + code)));
	}

	@Override
	public void glEnableClientState(int code) {
		if (gl != null) super.glEnableClientState(code);
		throw new NotImplementedException("enableClientState");
	}

	@Override
	public void glPolygonMode(int param, int value) {
		if (gl != null) super.glPolygonMode(param, value);
		throw new NotImplementedException("polygonMode");
	}

	@Override
	public void blendFunc(String sfactor, String dfactor) {
		if (gl != null) super.blendFunc(sfactor, dfactor);
		p.println(glMethod("blendFunc",
							new InstanceReference(glMember + "." + sfactor),
							new InstanceReference(glMember + "." + dfactor)));
	}

	@Override
	public void glShadeModel(int model) {
		if (gl != null) super.glShadeModel(model);
		throw new NotImplementedException("shadeModel");
	}

	@Override
	public int glRenderMode(int mode) {
		if (gl != null) super.glRenderMode(mode);
		throw new NotImplementedException("renderMode");
	}

	@Override
	public void glPushAttrib(int attrib) {
		if (gl != null) super.glPushAttrib(attrib);
		throw new NotImplementedException("pushAttrib");
	}

	@Override
	public void glPopAttrib() {
		if (gl != null) super.glPopAttrib();
		throw new NotImplementedException("popAttrib");
	}

	@Override
	public void glGenBuffers(int a, int b[], int c) {
		if (gl != null) super.glGenBuffers(a, b, c);
		throw new NotImplementedException("genBuffers");
	}

	@Override
	public void glBindBuffer(int code, int v) {
		if (gl != null) super.glBindBuffer(code, v);
		throw new NotImplementedException("bindBuffer");
	}

	@Override
	public void glBufferData(int code, int l, ByteBuffer buf, int d) {
		if (gl != null) super.glBufferData(code, l, buf, d);
		throw new NotImplementedException("bufferData");
	}

	@Override
	public void glSelectBuffer(int size, IntBuffer buf) {
		if (gl != null) super.glSelectBuffer(size, buf);
		throw new NotImplementedException("selectBuffer");
	}

	@Override
	public int glGenLists(int code) {
		if (gl != null) super.glGenLists(code);
		throw new NotImplementedException("genLists");
	}

	@Override
	public void glCallList(int list) {
		if (gl != null) super.glCallList(list);
		throw new NotImplementedException("callList");
	}

	@Override
	public void glNewList(int list, int code) {
		if (gl != null) super.glNewList(list, code);
		throw new NotImplementedException("newList");
	}

	@Override
	public void uv(Pair texCoord) {
		if (enableDoublePrecision) {
			p.println(glMethod("texCoord2d",
						Arrays.asList(new Variable<>("u", texCoord.getA()),
									new Variable<>("v", texCoord.getB()))));
		} else {
			p.println(glMethod("texCoord2f",
					Arrays.asList(new Variable<>("u", (float) texCoord.getA()),
									new Variable<>("v", (float) texCoord.getB()))));
		}
	}

	@Override
	public void glMatrixMode(int code) {
		if (gl != null) super.glMatrixMode(code);
		p.println(glMethod("matrixMode", Arrays.asList(new Variable<>("code", code))));
	}

	@Override
	public void glPushMatrix() {
		if (gl != null) super.glPushMatrix();
		p.println(glMethod("pushMatrix", Arrays.asList()));
	}

	@Override
	public void glPopMatrix() {
		if (gl != null) super.glPopMatrix();
		p.println(glMethod("popMatrix", Arrays.asList()));
	}

	@Override
	public void glLoadIdentity() {
		if (gl != null) super.glLoadIdentity();
		p.println(glMethod("loadIdentity", Arrays.asList()));
	}

	@Override
	@Deprecated public void glMultMatrix(TransformMatrix m) {
		if (gl != null) super.glMultMatrix(m);

		if (enableDoublePrecision) {
			p.println(glMethod("multMatrixd", Arrays.asList(new Variable<>("matrix", m.toArray()))));
		} else {
			p.println(glMethod("multMatrixf", Arrays.asList(new Variable<>("matrix", Scalar.toFloat(m.toArray())))));
		}
	}

	@Override
	public void glRasterPos(Vector pos) {
		if (gl != null) super.glRasterPos(pos);
		throw new NotImplementedException("rasterPos");
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Override
	@Deprecated public void setViewport(int x, int y, int w, int h) {
		if (gl != null) super.setViewport(x, y, w, h);
		throw new NotImplementedException("setViewport");
	}

	/** It is recommended to use a {@link PinholeCamera} instead. */
	@Override
	@Deprecated public void setPerspective(double fovy, double aspect, double zNear, double zFar) {
		if (glu != null) super.setPerspective(fovy, aspect, zNear, zFar);
		p.println(gluMethod("perspective",
					Arrays.asList(new Variable<>("fovy", fovy),
								new Variable<>("aspect", aspect),
								new Variable<>("zNear", zNear),
								new Variable<>("zFar", zFar))));
	}

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Override
	@Deprecated public void gluOrtho2D(double a, double b, double c, double d) {
		if (glu != null) super.gluOrtho2D(a, b, c, d);
		throw new NotImplementedException("ortho2D");
	}

	/** It is recommended to use a {@link org.almostrealism.algebra.Camera} instead. */
	@Override
	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
		if (glu != null) super.gluLookAt(e, c, var13, var15, var17);
		p.println(gluMethod("lookAt",
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
		throw new NotImplementedException("unProject");
	}

	@Override
	public boolean gluUnProject(Vector w, Vector worldpos) {
		if (glu != null) super.gluUnProject(w, worldpos);
		throw new NotImplementedException("unProject");
	}

	@Override
	public void gluPickMatrix(float x, float y, float w, float h, int viewport[]) {
		if (glu != null) super.gluPickMatrix(x, y, w, h, viewport);
		throw new NotImplementedException("pickMatrix");
	}

	@Override
	public void glProjection(Camera c) {
		if (gl != null) super.glProjection(c);

		p.println(glMethod("matrixMode",
						Arrays.asList(new Variable<>("GL_PROJECTION", GL2.GL_PROJECTION))));
		p.println(glMethod("loadIdentity"));

		if (c instanceof PinholeCamera) {
			PinholeCamera camera = (PinholeCamera) c;

			float width = (float) camera.getProjectionWidth();
			float height = (float) camera.getProjectionHeight();
			p.println(gluMethod("perspective",
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

		p.println(glMethod("matrixMode",
						Arrays.asList(new Variable<>("GL_MODELVIEW", GL2.GL_MODELVIEW))));
		p.println(glMethod("loadIdentity"));
	}

	@Override
	public void glClipPlane(int plane, DoubleBuffer eqn) {
		if (gl != null) super.glClipPlane(plane, eqn);
		System.out.println("GLPrintWriter[WARN]: glClipPlane is deprecated and will not be included");
	}

	@Override
	public void glCullFace(int param) {
		if (gl != null) super.glCullFace(param);
		p.println(glMethod("cullFace", new Variable<>("param", param)));
	}

	@Override
	public void glFrontFace(int param) {
		if (gl != null) super.glFrontFace(param);
		p.println(glMethod("frontFace", new Variable<>("param", param)));
	}

	@Override
	public void glFlush() {
		if (gl != null) super.glFlush();
		throw new NotImplementedException("flush");
	}

	@Override
	public int glEnd() {
		int v = begins.peek();

		if (gl != null) {
			super.glEnd();
		} else {
			begins.pop();
		}

		p.println(glMethod("begin"));
		return v;
	}

	@Override
	public void endList() {
		if (gl != null) super.endList();
		throw new NotImplementedException("endList");
	}

	@Override
	@Deprecated public void glDisable(int code) {
		if (gl != null) super.glDisable(code);
		p.println(glMethod("disable", new Variable<>("code", code)));
	}

	@Override
	@Deprecated public void glDisableClientState(int code) {
		if (gl != null) super.glDisableClientState(code);
		throw new NotImplementedException("disableClientState");
	}

	@Override
	@Deprecated
	public void hint(int param, int value) {
		if (gl != null) super.hint(param, value);
		throw new NotImplementedException("hint");
	}

	@Override
	public void hint(String param, String value) throws NoSuchFieldException, IllegalAccessException {
		if (gl != null) super.hint(param, value);
		p.println(glMethod("hint",
						new InstanceReference(glMember + "." + param),
						new InstanceReference(glMember + "." + value)));
	}

	@Override
	public void wireCube(double size) {
		if (glut != null) super.wireCube(size);
		throw new NotImplementedException("wireCube");
	}

	@Override
	public void glutSolidSphere(double radius, int slices, int stacks) {
		if (glut != null) super.glutSolidSphere(radius, slices, stacks);
		p.println(glutMethod("solidSphere",
								new Variable<>("radius", radius),
								new Variable<>("slices", slices),
								new Variable<>("stacks", stacks)));
	}

	@Override
	public void glDrawArrays(int code, int a, int b) {
		if (gl != null) super.glDrawArrays(code, a, b);
		throw new NotImplementedException("drawArrays");
	}

	protected Method glMethod(String name, Variable... args) {
		return glMethod(glMember, p instanceof JavaScriptPrintWriter, name, Arrays.asList(args));
	}

	protected Method glMethod(String name, List<Variable> args) {
		return glMethod(glMember, p instanceof JavaScriptPrintWriter, name, args);
	}

	protected Method gluMethod(String name, List<Variable> args) {
		return glMethod(gluMember, p instanceof JavaScriptPrintWriter, name, args);
	}

	protected Method glutMethod(String name, Variable... args) {
		return glutMethod(name, Arrays.asList(args));
	}

	protected Method glutMethod(String name, List<Variable> args) {
		return glMethod(glutMember, p instanceof JavaScriptPrintWriter, name, args);
	}

	protected static Method glMethod(String glMember, boolean isWebGL, String name, List<Variable> args) {
		if (!isWebGL) {
			name = glMember + name.substring(0, 1).toUpperCase() + name.substring(1);
		} else {
			for (Variable v : args) {
				if (v instanceof InstanceReference) {
					if (((InstanceReference) v).getData().startsWith("GL_")) {
						((InstanceReference) v).setData(((InstanceReference) v).getData().substring(3));
					} else if (((InstanceReference) v).getData().startsWith(glMember + ".GL_")) {
						((InstanceReference) v).setData(glMember + "." +
								((InstanceReference) v).getData().substring(glMember.length() + 4));
					}
				}
			}
		}

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
