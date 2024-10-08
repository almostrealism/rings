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

import io.almostrealism.code.ExpressionAssignment;
import io.almostrealism.expression.ConstantValue;
import io.almostrealism.expression.IntegerConstant;
import io.almostrealism.expression.StaticReference;
import org.almostrealism.CodeFeatures;
import org.almostrealism.projection.OrthographicCamera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytrace.FogParameters;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import io.almostrealism.lang.CodePrintWriter;
import io.almostrealism.expression.Expression;
import io.almostrealism.expression.InstanceReference;
import io.almostrealism.scope.Method;
import io.almostrealism.scope.Scope;
import io.almostrealism.scope.Variable;
import org.almostrealism.algebra.Pair;
import org.almostrealism.geometry.Camera;
import org.almostrealism.geometry.TransformMatrix;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBA;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.function.Predicate;

/**
 * {@link GLPrintWriter} allows for convenient encoding of GL variables and methods using
 * the same methods that {@link com.almostrealism.renderable.Renderable#display(GLDriver)} uses
 * to display GL content. In many cases this means that passing a {@link GLPrintWriter} to
 * the {@link com.almostrealism.renderable.Renderable#display(GLDriver)} method will correctly
 * encode the GL content to the {@link CodePrintWriter} that is wrapped by this
 * {@link GLPrintWriter}.
 */
public class GLPrintWriter extends GLDriver implements CodeFeatures {
	// TODO  this is not ideal
	public static Predicate<Object> isJs = p -> p.getClass().getSimpleName().equals("JavaScriptPrintWriter");

	private String glMember;

	private String matrixMember="mat4";
	
	private String name; // TODO Use name
	private CodePrintWriter p;

	private int varIndex = 0;

	// TODO Kristen added for an experiment - should probably remove it
	public CodePrintWriter getPrintWriter() {
		return p;
	}
	
	public GLPrintWriter(String glMember, String matMember, String name, CodePrintWriter p) {
		super(p.getLanguage(), null);
		this.glMember = glMember;
		if (matMember !=null && matMember.length()>0) {
			this.matrixMember = matMember;
		}
		this.name = name;
		this.p = p;
	}

	@Override
	public boolean isDoublePrecision() {
		return isJs.test(p);
	}

	@Override
	public void setLighting(GLLightingConfiguration lighting) {
		if (gl != null) super.setLighting(lighting);
		// TODO
	}
	

	
	//TODO
	@Override
	protected void glProjection(Camera c) {
		//DO NOT DO THIS...instead follow advice at https://webglfundamentals.org/webgl/lessons/webgl-3d-camera.html
//		projection_joml = new Matrix4d();
//
//		if (c instanceof PinholeCamera) {
//			PinholeCamera camera = (PinholeCamera) c;
//
//			Vector eye = camera.getLocation();
//			Vector center = eye.add(camera.getViewingDirection());
//			float width = (float) camera.getProjectionWidth();
//			float height = (float) camera.getProjectionHeight();
//
//			projection_joml = new Matrix4d()
//					.perspective(camera.getHorizontalFOV(), width / height, 0.1, 1e9)
//					.lookAt(new Vector3d(eye.getX(), eye.getY(), eye.getZ()),
//							new Vector3d(center.getX(), center.getY(), center.getZ()),
//							new Vector3d(0, 1, 0));
//		}
//		else if (c instanceof OrthographicCamera) {
//			OrthographicCamera camera = (OrthographicCamera) c;
//
//			// TODO: Orthographic projection matrix.
//		}

		if (c == null) return; // TODO  Set to a "default" projection
		PinholeCamera pinCam = (PinholeCamera) c;
		
		ExpressionAssignment aspect = declare("aspect", e(pinCam.getAspectRatio()));
		p.println(aspect);
	}

	@Override
	public void glColor(RGB color) {
		if (gl != null) super.glColor(color);

//		TODO Replace these with something supported by WebGL
//		if (enableDoublePrecision) {
//			p.println(glMethod("color3d",
//								new Variable<>("r", color.getRed()),
//								new Variable<>("g", color.getGreen()),
//								new Variable<>("b", color.getBlue())));
//		} else {
//			p.println(glMethod("color3f",
//								new Variable<>("r", (float) color.getRed()),
//								new Variable<>("g", (float) color.getGreen()),
//								new Variable<>("b", (float) color.getBlue())));
//		}
	}

	@Override
	public void glColor(RGBA color) {
		if (gl != null) super.glColor(color);

		String method = isDoublePrecision() ? "color4d" : "color4f";
		p.println(glMethod(method, e(color.getRed()), e(color.getGreen()), e(color.getBlue()), e(color.getAlpha())));
	}

	@Override
	public void glMaterial(GLMaterial mat) {
		if (gl != null) super.glMaterial(mat);

//		TODO  Replace these with methods that work for WebGL
//		p.println(glMethod("materialfv",
//				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
//								new Variable<>("GL_AMBIENT", GL2.GL_AMBIENT),
//								new Variable<>("ambient", Scalar.toFloat(mat.ambient.toArray())),
//								new Variable<>("zero", 0))));
//		p.println(glMethod("materialfv",
//				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
//								new Variable<>("GL_DIFFUSE", GL2.GL_DIFFUSE),
//								new Variable<>("diffuse", Scalar.toFloat(mat.diffuse.toArray())),
//								new Variable<>("zero", 0))));
//		p.println(glMethod("materialfv",
//				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
//								new Variable<>("GL_SPECULAR", GL2.GL_SPECULAR),
//								new Variable("specular", Scalar.toFloat(mat.specular.toArray())),
//								new Variable("zero", 0))));
//		p.println(glMethod("materialfv",
//				Arrays.asList(new Variable<>("GL_FRONT", GL.GL_FRONT),
//								new Variable("GL_SHININESS", GL2.GL_SHININESS),
//								new Variable("shininess", new float[] { (float) mat.shininess.getValue() }),
//								new Variable("zero", 0))));
	}

	@Override
	public void glInitNames() {
		if (gl != null) super.glInitNames();
		throw new RuntimeException("glInitNames");
	}

	@Override
	public void glLoadName(int name) {
		if (gl != null) super.glLoadName(name);
		throw new RuntimeException("glLoadName");
	}

	@Override
	public void glPushName(int name) {
		if (gl != null) super.glPushName(name);
		throw new RuntimeException("glPushName");
	}

	@Override
	public void bindTexture(Texture t) {
		if (gl != null) super.bindTexture(t);
		throw new RuntimeException("bindTextures");
	}

	@Override
	public void bindTexture(String code, int tex) {
		if (gl != null) super.bindTexture(code, tex);
//		p.println(glMethod("bindTexture",
//						new InstanceReference(glMember + "." + code),
//						new Variable<>("tex", tex)));
		System.out.println("GLPrintWriter[WARN]: bindTexture is not supported by some versions of OpenGL. Use Texture type instead.");
		// TODO  This should be an exception
	}

	@Override
	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, byte buf[]) {
		if (gl != null) super.glTexImage2D(a, b, c, d, e, f, g, h, buf);
//		p.println(glMethod("texImage2D",
//				Arrays.asList(new Variable<>("a", a),
//								new Variable<>("b", b),
//								new Variable<>("c", c),
//								new Variable<>("d", d),
//								new Variable<>("e", e),
//								new Variable<>("f", f),
//								new Variable<>("g", g),
//								new Variable<>("h", h),
//								new Variable<>("buf", buf))));
		System.out.println("GLPrintWriter[WARN]: glTexImage2D is not supported by some versions of OpenGL. Use Texture type instead.");
		// TODO  This should be an exception
	}

	@Override
	public void glTexEnvf(int a, int b, float f) {
		if (gl != null) super.glTexEnvf(a, b, f);
		throw new RuntimeException("texEnvf");
	}

	@Override
	public void glTexParameter(int code, int param, int value) {
		if (gl != null) super.glTexParameter(code, param, value);
		p.println(glMethod("texParameteri", List.of(e(code), e(param), e(value))));
	}

	@Override
	public void glLineWidth(double width) {
		if (gl != null) super.glLineWidth(width);
		throw new RuntimeException("lineWidth");
	}

	@Override
	public void glPointSize(double size) {
		if (gl != null) super.glPointSize(size);
		throw new RuntimeException("pointSize");
	}

	@Override
	public void glutBitmapCharacter(int font, char c) {
		if (glut != null) super.glutBitmapCharacter(font, c);
		throw new RuntimeException("bitmapCharacter");
	}

	@Override
	public void enableTexture(Texture t) {
		if (gl != null) super.enableTexture(t);
		throw new RuntimeException("enableTexture");
	}

	@Override
	public void disableTexture(Texture t) {
		if (gl != null) super.disableTexture(t);
		throw new RuntimeException("disableTexture");
	}

	@Override
	public void glActiveTexture(int code) {
		if (gl != null) super.glActiveTexture(code);
		throw new RuntimeException("activeTexture");
	}

	@Override
	public void glVertex(Vector v) {
		if (gl != null) super.glVertex(v);

		v = transformPosition(v);

		String method = isDoublePrecision() ? "vertex3d" : "vertex3f";

		p.println(glMethod(method, e(v.getX()), e(v.getY()), e(v.getZ())));
	}

	@Override
	public void glVertex(Pair p) {
		if (gl != null) super.glVertex(p);

		String method = isDoublePrecision() ? "vertex2d" : "vertex2f";

		this.p.println(glMethod(method, e(p.getX()), e(p.getY())));
	}

	@Override
	public void glNormal(Vector n) {
		if (gl != null) super.glNormal(n);

		n = transformDirection(n);

		String method = isDoublePrecision() ? "normal3d" : "normal3f";
		p.println(glMethod(method, e(n.getX()), e(n.getY()), e(n.getZ())));
	}

	/** It is recommended to use {@link org.almostrealism.color.Light}s instead. */
	@Override
	@Deprecated public void glLightModel(int code, RGBA color) {
		if (gl != null) super.glLightModel(code, color);
		throw new RuntimeException("glLightModel is deprecated in OpenGL");
	}

	@Override
	public void glAccum(int param, double value) {
		if (gl != null) super.glAccum(param, value);
		throw new RuntimeException("accum");
	}

	@Override
	public void glColorMask(boolean r, boolean g, boolean b, boolean a) {
		if (gl != null) super.glColorMask(r, g, b, a);
		p.println(glMethod("colorMask", e(r), e(g), e(b), e(a)));
	}


	@Override
	public void clearColorBuffer() {
		if (gl != null) super.clearColorBuffer();
		//throw new RuntimeException("clearColorBuffer");
		p.println(glMethod("clear", glParam("COLOR_BUFFER_BIT")));
		
	}
	
	@Override
	public void glClearColorAndDepth() {
		if (gl != null) super.glClearColorAndDepth();
		p.println(glMethod("clear", glParam("COLOR_BUFFER_BIT", "DEPTH_BUFFER_BIT")));
	}

	@Override
	public void glClearColor(RGBA c) {
		if (gl != null) super.glClearColor(c);
		p.println(glMethod("clearColor", e(c.r()), e(c.g()), e(c.b()), e(c.a())));
	}

	@Override
	public void glClearAccum(RGBA c) {
		if (gl != null) super.glClearAccum(c);
		throw new RuntimeException("clearAccum");
	}

	@Override
	public void glDepthFunc(String code) {
		if (gl != null) super.glDepthFunc(code);
		p.println(glMethod("depthFunc", glParam(code)));
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		if (gl != null) super.glStencilFunc(func, ref, mask);
		p.println(glMethod("stencilFunc", e(func), e(ref), e(mask)));
	}

	@Override
	public void glStencilOp(int sfail, int dpfail, int dppass) {
		if (gl != null) super.glStencilOp(sfail, dpfail, dppass);
		p.println(glMethod("sStencilOp", e(sfail), e(dpfail), e(dppass)));
	}

	@Override
	public void clearDepth(double d) {
		if (gl != null) super.clearDepth(d);

		String method = isDoublePrecision() ? "clearDepth" : "clearDepthf";
		p.println(glMethod(method, e(d)));
	}

	@Override
	public void glClearStencil(int param) {
		if (gl != null) super.glClearStencil(param);
		p.println(glMethod("clearStencil", e(param)));
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

		throw new RuntimeException("setFog");
	}

	@Override
	public void glQuads() {
		if (gl != null) super.glQuads();
		throw new RuntimeException("quads");
	}

	@Override
	public void glBegin(int code) {
		if (gl != null) {
			super.glBegin(code);
		} else {
			begins.push(code);
		}
		
		//p.println(glMethod("begin", new Variable<>("code", code)));
	}

	@Override
	@Deprecated
	public void enable(int code) {
		if (gl != null) super.enable(code);
		throw new RuntimeException("enable");
	}

	@Override
	public void enable(String code) {
		if (gl != null) super.enable(code);
		p.println(glMethod("enable", glParam(code)));
	}

	@Override
	public void glEnableClientState(int code) {
		if (gl != null) super.glEnableClientState(code);
		throw new RuntimeException("enableClientState");
	}

	@Override
	public void glPolygonMode(int param, int value) {
		if (gl != null) super.glPolygonMode(param, value);
		throw new RuntimeException("polygonMode");
	}

	@Override
	public void blendFunc(String sfactor, String dfactor) {
		if (gl != null) super.blendFunc(sfactor, dfactor);
		p.println(glMethod("blendFunc",
							glParam(sfactor),
							glParam(dfactor)));
	}

	@Override
	public void glShadeModel(int model) {
		if (gl != null) super.glShadeModel(model);
		throw new RuntimeException("shadeModel");
	}

	@Override
	public int glRenderMode(int mode) {
		if (gl != null) super.glRenderMode(mode);
		throw new RuntimeException("renderMode");
	}

	@Override
	public void glPushAttrib(int attrib) {
		if (gl != null) super.glPushAttrib(attrib);
		throw new RuntimeException("pushAttrib");
	}

	@Override
	public void glPopAttrib() {
		if (gl != null) super.glPopAttrib();
		throw new RuntimeException("popAttrib");
	}

	@Override
	public Expression createProgram() {
		String name = "program" + (varIndex++);
		ExpressionAssignment v = declare(name, new Method<>(String.class, glMember, "createProgram",
				new ArrayList<>()));
		p.println(v);
		return v.getDestination();
	}

	@Override
	public void linkProgram(Expression program) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(program);
		p.println(new Method(glMember, "linkProgram", args));
	}

	@Override
	public void useProgram(Expression program) {
		p.println(new Method(glMember, "useProgram", program));
	}

	public void mapProgramAttributes(Variable program) {
		ExpressionAssignment pos = assign(program.getName() + ".positionAttribute",
									new Method<String>(glMember, "getAttribLocation",
														new InstanceReference<>(program),
														new StaticReference<>(null, "pos")));
		p.println(pos);

		p.println(new Method(glMember, "enableVertexAttribArray",
							new StaticReference(String.class, program.getName() + ".positionAttribute")));

		ExpressionAssignment norm = assign(program.getName() + ".normalAttribute",
				new Method<String>(glMember, "getAttribLocation",
						new InstanceReference<>(program), new StaticReference<>(Vector.class, "normal")));
		p.println(norm);

		p.println(new Method(glMember, "enableVertexAttribArray",
				new StaticReference<>(String.class, program.getName() + ".normalAttribute")));
	}

	@Override
	public Expression createShader(String type) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(glParam(type));

		String name = "shader" + (varIndex++);
		ExpressionAssignment v = declare(name, new Method<String>(glMember, "createShader", args));
		p.println(v);
		return v.getDestination();
	}

	@Override
	public void shaderSource(Expression shader, String source) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(shader);
		args.add(new StaticReference<>(String.class, source));
		p.println(new Method(glMember, "shaderSource", args));
	}

	@Override
	public void compileShader(Expression shader) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(shader);
		p.println(new Method(glMember, "compileShader", args));
	}

	@Override
	public void attachShader(Expression program, Expression shader) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(program);
		args.add(shader);
		p.println(new Method(glMember, "attachShader", args));
	}

	@Override
	public void deleteShader(Expression shader) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(shader);
		p.println(new Method(glMember, "deleteShader", args));
	}

	@Override
	public Expression<String> createBuffer() {
		String name = "buffer" + (varIndex++);
		ExpressionAssignment v = declare(name, new Method<>(glMember, "createBuffer"));
		p.println(v);
		return v.getDestination();
	}

	public void bindBuffer(String code, Variable v) {
		List<Expression<?>> args = new ArrayList<>();
		args.add(glParam(code));
		args.add(new InstanceReference(v));
		p.println(new Method(glMember, "bindBuffer", args));
	}

	public void bufferData(Variable buffer, List<Double> data) {
		ExpressionAssignment v = declare("vertices" + (varIndex++), new ConstantValue<>(List.class, data));

		p.println(v);

		List<Expression<?>> nargs = new ArrayList<>();
		nargs.add(v.getDestination());

		Method n = new Method("new Float32Array", nargs);

		List<Expression<?>> args = new ArrayList<>();
		args.add(glParam("ARRAY_BUFFER"));
		args.add(n);
		args.add(glParam("STATIC_DRAW"));
		p.println(new Method(glMember, "bufferData", args));
	}

	@Override
	public void glGenBuffers(int a, int b[], int c) {
		if (gl != null) super.glGenBuffers(a, b, c);
		throw new RuntimeException("genBuffers");
	}

	@Override
	public void glBindBuffer(int code, int v) {
		if (gl != null) super.glBindBuffer(code, v);
		throw new RuntimeException("bindBuffer");
	}

	@Override
	public void glBufferData(int code, int l, ByteBuffer buf, int d) {
		if (gl != null) super.glBufferData(code, l, buf, d);
		throw new RuntimeException("bufferData");
	}

	@Override
	public void glSelectBuffer(int size, IntBuffer buf) {
		if (gl != null) super.glSelectBuffer(size, buf);
		throw new RuntimeException("selectBuffer");
	}

	@Override
	public void uv(Pair texCoord) {
		String method = isDoublePrecision() ? "texCoord2d" : "texCoord2f";
		p.println(glMethod(method, e(texCoord.getA()), e(texCoord.getB())));
	}

	@Override
	public void glRasterPos(Vector pos) {
		if (gl != null) super.glRasterPos(pos);
		throw new RuntimeException("rasterPos");
	}

	/** It is recommended to use a {@link org.almostrealism.geometry.Camera} instead. */
	@Override
	@Deprecated
	public void setViewport(int x, int y, int w, int h) {
		if (gl != null) super.setViewport(x, y, w, h);
		throw new RuntimeException("setViewport");
	}

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Override
	@Deprecated
	public void gluOrtho2D(double left, double right, double bottom, double top) {
		if (glu != null) super.gluOrtho2D(left, right, bottom, top);
		throw new RuntimeException("ortho2D");
	}

	/** It is recommended to use a {@link org.almostrealism.geometry.Camera} instead. */
//	@Override
//	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
//		if (glu != null) super.gluLookAt(e, c, var13, var15, var17);
//		p.println(gluMethod("lookAt",
//							Arrays.asList(new Variable<>("ex", (float) e.getX()),
//										new Variable<>("ey", (float) e.getY()),
//										new Variable("ez", (float) e.getZ()),
//										new Variable("cx", (float) c.getX()),
//										new Variable("cy", (float) c.getY()),
//										new Variable("cz", (float) c.getZ()),
//										new Variable("var13", (float) var13),
//										new Variable("var15", (float) var15),
//										new Variable("var17", (float) var17))));
//	}

	@Override
	public boolean gluUnProject(Vector w, double modelview[], double projection[], int viewport[], Vector worldpos) {
		if (glu != null) super.gluUnProject(w, modelview, projection, viewport, worldpos);
		throw new RuntimeException("unProject");
	}

	@Override
	public boolean gluUnProject(Vector w, Vector worldpos) {
		if (glu != null) super.gluUnProject(w, worldpos);
		throw new RuntimeException("unProject");
	}

	@Override
	public void gluPickMatrix(float x, float y, float w, float h, int viewport[]) {
		if (glu != null) super.gluPickMatrix(x, y, w, h, viewport);
		throw new RuntimeException("pickMatrix");
	}

	@Override
	public void glClipPlane(int plane, DoubleBuffer eqn) {
		if (gl != null) super.glClipPlane(plane, eqn);
		System.out.println("GLPrintWriter[WARN]: glClipPlane is deprecated and will not be included");
	}

	@Override
	public void glCullFace(int param) {
		if (gl != null) super.glCullFace(param);
		p.println(glMethod("cullFace", e(param)));
	}

	@Override
	public void glFrontFace(int param) {
		if (gl != null) super.glFrontFace(param);
		p.println(glMethod("frontFace", e(param)));
	}

	@Override
	public void glFlush() {
		if (gl != null) super.glFlush();
		throw new RuntimeException("flush");
	}

	@Override
	public int glEnd() {
		int v = begins.peek();

		if (gl != null) {
			super.glEnd();
		} else {
			begins.pop();
		}

		//p.println(glMethod("begin"));
		return v;
	}

	@Override
	public void endList() {
		if (gl != null) super.endList();
		throw new RuntimeException("endList");
	}

	@Override
	@Deprecated public void glDisable(int code) {
		if (gl != null) super.glDisable(code);
		p.println(glMethod("disable", new IntegerConstant(code)));
	}

	@Override
	@Deprecated public void glDisableClientState(int code) {
		if (gl != null) super.glDisableClientState(code);
		throw new RuntimeException("disableClientState");
	}

	@Override
	@Deprecated
	public void hint(int param, int value) {
		if (gl != null) super.hint(param, value);
		throw new RuntimeException("hint");
	}

	@Override
	public void hint(String param, String value) throws NoSuchFieldException, IllegalAccessException {
		if (gl != null) super.hint(param, value);
		p.println(glMethod("hint", glParam(param), glParam(value)));
	}
	
	//TODO: revise to correctly render a sphere.
	public void glutSolidSphere(double radius, int slices, int stacks) {
		super.glutSolidSphere(radius, slices, stacks);
	}
	
	//This was an experiment - does not render sphere obviously
	public void glutSolidSphere_experiment(double radius, int slices, int stacks) {
		glClearColor(new RGBA(0.0, 0.0, 0.0, 1.0));
		clearDepth(1.0);
		enable("DEPTH_TEST");
		glDepthFunc("LEQUAL");
		glClearColorAndDepth();
		ExpressionAssignment projectionMatrix = declare("projectionMatrix", new StaticReference<>(String.class, matrixMember + ".create()"));
		p.println(projectionMatrix);
		List<Expression<?>> arguments = new ArrayList<>();
		StaticReference<TransformMatrix> projMatrix = new StaticReference<>(TransformMatrix.class, "projectionMatrix");
		Expression<Double> fieldOfView = e(45 * Math.PI / 180);
		ExpressionAssignment clientWidth = declare("clientWidth", new StaticReference<>(String.class,"gl.canvas.clientWidth"));
		ExpressionAssignment clientHeight = declare("clientHeight", new StaticReference<>(String.class,"gl.canvas.clientHeight"));
		p.println(clientWidth);
		p.println(clientHeight);
		Expression<Double> aspect = new StaticReference<>(Double.class, "clientWidth / clientHeight");
		ExpressionAssignment zNear = declare("zNear", e(0.1));
		ExpressionAssignment zFar = declare("zFar", e(100.0));
		arguments.add(projMatrix);
		arguments.add(fieldOfView);
		arguments.add(aspect);
		arguments.add(zNear.getDestination());
		arguments.add(zFar.getDestination());

		Method persp = new Method("mat4","perspective", arguments);
		p.println(persp);
		
		ExpressionAssignment modelViewMatrix = declare("modelViewMatrix", new StaticReference<>(String.class, matrixMember + ".create()"));
		p.println(modelViewMatrix);
		ExpressionAssignment amtToTranslate = declare("amountToTranslate", new StaticReference<>(String.class, "[-0.0,0.0,-6.0]"));
		p.println(amtToTranslate);
		
		List<Expression<?>> tranArgs = new ArrayList<>();
		tranArgs.add(new StaticReference<>(TransformMatrix.class, "modelViewMatrix"));
		tranArgs.add(new StaticReference<>(TransformMatrix.class, "modelViewMatrix"));
		tranArgs.add(new StaticReference<>(TransformMatrix.class, "amountToTranslate"));
		Method transl = new Method("mat4","translate", tranArgs);
		
		p.println(transl);
//		mat4.translate(modelViewMatrix,     // destination matrix
//	               modelViewMatrix,     // matrix to translate
//	               [-0.0, 0.0, -6.0]);  // amount to translate
		
		List<String> emptyList = new ArrayList<String>();
		Map<String,Variable> emptyMap = new HashMap<String,Variable>();
		
		ExpressionAssignment positionBuffer = declare("positionBuffer", new Method(glMember,"createBuffer"));
		
		p.println(positionBuffer);
		
		List<Expression<?>> bindArgs = new ArrayList<>();
		bindArgs.add(glParam("ARRAY_BUFFER"));
		bindArgs.add(new StaticReference<>(null, "positionBuffer"));
		
		Method bindBuf = glMethod("bindBuffer", bindArgs);
		
		p.println(bindBuf);

		
		ExpressionAssignment positions = declare("positions", new StaticReference<>(String.class, "[1.0,1.0,-1.0,1.0,1.0,-1.0,-1.0,-1.0]"));
		p.println(positions);


		p.println(glMethod("bufferData", glParam("ARRAY_BUFFER"),
				new StaticReference<>(null, "new Float32Array(positions)"),
				glParam("STATIC_DRAW")));
		
		//var buffers = { position: positionBuffer, };

		ExpressionAssignment buffers = declare("buffers", new StaticReference<>(String.class, "{ position: positionBuffer, }"));
		p.println(buffers);
		Scope<Variable> bufferBinding = new Scope<>();
		List<ExpressionAssignment<?>> vars = bufferBinding.getVariables();
		ExpressionAssignment numC = declare("numComponents", e(2));
		vars.add(numC);

		ExpressionAssignment norm = declare("normalize", e(false));
		vars.add(norm);
		ExpressionAssignment strd = declare("stride", e(0));
		vars.add(strd);
		ExpressionAssignment offs = declare("offset", e(0));
		vars.add(offs);
		List<Method> methods = bufferBinding.getMethods();
		methods.add(glMethod("bindBuffer",
				glParam("ARRAY_BUFFER"),
				new StaticReference<>(null, "buffers.position")));
		methods.add(glMethod("vertexAttribPointer",
				new StaticReference<>(Vector.class, "programInfo.attribLocations.vertexPosition"),
				numC.getDestination(),
				glParam("FLOAT"), norm.getDestination(),
				strd.getDestination(), offs.getDestination()));
		
		//add
//		gl.enableVertexAttribArray(
//		        programInfo.attribLocations.vertexPosition);
		methods.add(glMethod("enableVertexAttribArray",
				new StaticReference<>(Vector.class, "programInfo.attribLocations.vertexPosition")));
		
		for (Iterator<ExpressionAssignment<?>> it = vars.iterator(); it.hasNext();) {
			ExpressionAssignment v = it.next();
			p.println(v);
		}

		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
			Method method = (Method) iterator.next();
			p.println(method);
		}
		
		Method useP = glMethod("useProgram", new StaticReference<>(null, "programInfo.program"));
		p.println(useP);
		
		Method m4fv = glMethod("uniformMatrix4fv",
				new StaticReference<>(TransformMatrix.class, "programInfo.uniformLocations.projectionMatrix"),
				norm.getDestination(), new StaticReference<>(TransformMatrix.class, "projectionMatrix"));
		Method m4fvModel = glMethod("uniformMatrix4fv",
				new StaticReference<>(TransformMatrix.class, "programInfo.uniformLocations.modelViewMatrix"),
				norm.getDestination(),
				new StaticReference<>(TransformMatrix.class, "modelViewMatrix"));
		
		p.println(m4fv);
		p.println(m4fvModel);
		
		ExpressionAssignment vCount = declare("vertexCount", e(4));
		Method drawIt = glMethod("drawArrays", glParam("TRIANGLE_STRIP"),
				offs.getDestination(), vCount.getDestination());
		p.println(vCount);
		p.println(drawIt);
		
		//NEXT
//		  gl.useProgram(programInfo.program);
//
//		  // Set the shader uniforms
//
//		  gl.uniformMatrix4fv(
//		      programInfo.uniformLocations.projectionMatrix,
//		      false,
//		      projectionMatrix);
//		  gl.uniformMatrix4fv(
//		      programInfo.uniformLocations.modelViewMatrix,
//		      false,
//		      modelViewMatrix);
//
//		  {
//		    const offset = 0;
//		    const vertexCount = 4;
//		    gl.drawArrays(gl.TRIANGLE_STRIP, offset, vertexCount);
//		  }
		
	}

	@Override
	public void wireCube(double size) {
		if (glut != null) super.wireCube(size);
		throw new RuntimeException("wireCube");
	}

	@Override
	public void glDrawArrays(int code, int a, int b) {
		if (gl != null) super.glDrawArrays(code, a, b);
		throw new RuntimeException("drawArrays");
	}

	public Method<?> glMethod(String name, Expression<?>... args) {
		return glMethod(glMember, isJs.test(p), name, Arrays.asList(args));
	}

	//should be protected - Kristen changed for experiment
	public Method<?> glMethod(String name, List<Expression<?>> args) {
		return glMethod(glMember, isJs.test(p), name, args);
	}

//	protected Method gluMethod(String name, List<Variable> args) {
//		return glMethod(gluMember, p instanceof JavaScriptPrintWriter, name, args);
//	}

//	protected Method glutMethod(String name, Variable... args) {
//		return glutMethod(name, Arrays.asList(args));
//	}

//	protected Method glutMethod(String name, List<Variable> args) {
//		return glMethod(glutMember, p instanceof JavaScriptPrintWriter, name, args);
//	}

	public StaticReference<Integer> glParam(String... params) {
		StringBuffer concat = new StringBuffer();

		for (int i = 0; i < params.length; i++) {
			concat.append(glMember);
			concat.append(".");
			concat.append(p);
			if (i < params.length - 1) concat.append(" | ");
		}

		return new StaticReference<>(Integer.class, concat.toString());
	}

	protected static Method glMethod(String glMember, boolean isWebGL, String name, List<Expression<?>> args) {
		if (!isWebGL) {
			name = glMember + name.substring(0, 1).toUpperCase() + name.substring(1);
		} else {
//			TODO  Need to handle webgl constant naming conventions
//			for (Expression<?> v : args) {
//				if (v instanceof InstanceReference) {
//					if (v.getExpression().startsWith("GL_")) {
//						v.setExpression(v.getExpression().substring(3));
//					} else if (v.getExpression().startsWith(glMember + ".GL_")) {
//						v.setExpression(glMember + "." +
//								v.getExpression().substring(glMember.length() + 4));
//					}
//				}
//			}
		}

		return new Method(glMember, name, args);
	}
}
