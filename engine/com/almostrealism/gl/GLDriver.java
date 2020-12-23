/*
 * Copyright 2020 Michael Murray
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

import com.almostrealism.gl.shaders.FragmentShader;
import com.almostrealism.gl.shaders.VertexShader;
import com.almostrealism.projection.OrthographicCamera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.FogParameters;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import io.almostrealism.code.DefaultNameProvider;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.*;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBA;
import org.almostrealism.geometry.Camera;
import org.almostrealism.geometry.TransformMatrix;
import org.almostrealism.space.Triangle;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.texture.ImageTexture;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class GLDriver {
	protected boolean enableDoublePrecision = Hardware.getLocalHardware().isDoublePrecision();
	protected boolean useGlMatrixStack = false;

	protected GL2 gl;
	protected GLU glu;
	protected GLUT glut;

	protected Stack<Integer> begins;

	protected OrthographicCamera camera;
	protected Stack<OrthographicCamera> cameraStack;

	protected GLLightingConfiguration lighting;
	protected Stack<GLLightingConfiguration> lightingStack;

	protected GLMaterial material;
	protected Stack<GLMaterial> materialStack;

	protected VertexShader vertexShader;
	protected Stack<VertexShader> vertexShaderStack;

	protected FragmentShader fragmentShader;
	protected Stack<FragmentShader> fragmentShaderStack;

	private int currentProgram = -1;

	protected Matrix4d projection_joml;

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
        this.projection_joml = new Matrix4d();
		this.lighting = new GLLightingConfiguration(Arrays.asList());

		this.transform = new TransformMatrix();
		this.matrixStack = new Stack<>();
		this.lightingStack = new Stack<>();
		this.materialStack = new Stack<>();

		this.vertexShaderStack = new Stack<>();
		this.fragmentShaderStack = new Stack<>();
	}

	public void pushLighting() { this.lightingStack.push(this.lighting); }
	public void popLighting() { this.setLighting(this.lightingStack.pop()); }

	public void setLighting(GLLightingConfiguration lighting) {
		this.lighting = lighting;
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight0Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight0Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight1Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight1Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight2Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight2Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight3Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight3Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT4, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight4Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT4, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight4Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT5, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight5Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT5, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight5Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT6, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight6Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT6, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight6Diffuse().toArray())));
		gl.glLightfv(GL2.GL_LIGHT7, GL2.GL_POSITION, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight7Position().toArray())));
		gl.glLightfv(GL2.GL_LIGHT7, GL2.GL_DIFFUSE, FloatBuffer.wrap(Scalar.toFloat(lighting.getLight7Diffuse().toArray())));
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

	public interface Runnable { boolean run(GLDriver gl); }

	/**
	 * {@link #pushMatrix()}
	 * {@link Runnable#run(GLDriver)}
	 * {@link #popMatrix()}
	 * @return  The value returned by {@link Runnable#run(GLDriver)}.
	 */
	public boolean run(Runnable r) {
		pushMatrix();
		boolean b = r.run(this);
		popMatrix();
		return b;
	}

	public void glInitNames() { gl.glInitNames(); }

	public void glLoadName(int name) { gl.glLoadName(name); }

	public void glPushName(int name) { gl.glPushName(name); }

	/** It is recommended to use {@link org.almostrealism.texture.Texture} instead. */
	public void bindTexture(Texture t) { t.bind(gl); }  // TODO Make protected

	public Texture bindTexture(ImageTexture t) {
		try {
			String s = t.getURL().toString();
			Texture tx = TextureIO.newTexture(t.getURL(), false, s.substring(s.lastIndexOf(".")));
			bindTexture(tx);
			return tx;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/** It is recommended to use {@link org.almostrealism.texture.Texture} instead. */
	@Deprecated public void bindTexture(String code, int tex) {
		try {
			gl.glBindTexture(GL2.class.getField(code).getInt(gl), tex);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	/** It is recommended to use {@link org.almostrealism.texture.Texture} instead. */
	@Deprecated public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, byte buf[]) {
		gl.glTexImage2D(a, b, c, d, e, f, g, h, ByteBuffer.wrap(buf));
	}

	public void glTexEnvf(int a, int b, float f) { gl.glTexEnvf(a, b, f); }

	public void glTexParameter(int code, int param, int value) { gl.glTexParameteri(code, param, value); }

	public void glLineWidth(double width) { gl.glLineWidth((float) width); }

	public void glPointSize(double size) { gl.glPointSize((float) size); }

	public void glutBitmapCharacter(int font, char c) { glut.glutBitmapCharacter(font, c); }

	@Deprecated public void enableTexture(Texture t) { t.enable(gl); }

	@Deprecated public void disableTexture(Texture t) { t.disable(gl); }

	public void glActiveTexture(int code) { gl.glActiveTexture(code); }

	public void glVertex(Vector v) {
		v = transformPosition(v);

		if (enableDoublePrecision) {
			gl.glVertex3d(v.getX(), v.getY(), v.getZ());
		} else {
			gl.glVertex3f((float) v.getX(), (float) v.getY(), (float) v.getZ());
		}
	}

	public void glVertex(Pair p) { glVertex(new Vector(p.getX(), p.getY(), 0.0)); }

	public void glNormal(Vector n) {
		n = transformNormal(n);

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

	public void glDepthFunc(String code) {
		try {
			gl.glDepthFunc(GL2.class.getField(code).getInt(gl));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

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
	public void glClearColorAndDepth() { glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT); }

	@Deprecated
	public void glClear(int bits) { gl.glClear(bits); }  // TODO  Make protected

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
	public void glBegin(int code) {  // TODO  Make protected
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

	public void setVertexShader(VertexShader s) {
		this.vertexShader = s;

		if (this.vertexShader == null) return;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GLSLPrintWriter p = new GLSLPrintWriter(out);
		this.vertexShader.getScope(new DefaultNameProvider("vshade")).write(p);
		String shader = new String(out.toByteArray());
		compileShader("GL_VERTEX_SHADER", shader);
	}

	public void setFragmentShader(FragmentShader s) {
		this.fragmentShader = s;

		if (this.fragmentShader == null) return;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GLSLPrintWriter p = new GLSLPrintWriter(out);
		this.fragmentShader.getScope(new DefaultNameProvider("fshade")).write(p);
		String shader = new String(out.toByteArray());
		compileShader("GL_FRAGMENT_SHADER", shader);
	}

	public void pushShaders() {
		this.vertexShaderStack.push(this.vertexShader);
		this.fragmentShaderStack.push(this.fragmentShader);
	}

	public void popShaders() {
		this.setVertexShader(vertexShaderStack.pop());
		this.setFragmentShader(fragmentShaderStack.pop());
	}

	/**
	 * Compile the shader, attach to the current program, use the current program.
	 */
	protected boolean compileShader(String shaderType, String shaderSource) {
		if (gl == null) return false; // TODO
		
		try {
			System.out.println(shaderType + ":");
			System.out.println(shaderSource);

			int s = gl.glCreateShader(GL2.class.getField(shaderType).getInt(gl));
			gl.glShaderSource(s, 1, new String[] { shaderSource },
								IntBuffer.wrap(new int[] { shaderSource.length() }));
			gl.glCompileShader(s);

			if (currentProgram < 0) {
				currentProgram = gl.glCreateProgram();
			}

			gl.glAttachShader(currentProgram, s);
			gl.glUseProgram(currentProgram);

			return true;
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated public void glShadeModel(int model) { gl.glShadeModel(model); }

	@Deprecated public int glRenderMode(int mode) { return gl.glRenderMode(mode); }

	public void glPushAttrib(int attrib) { gl.glPushAttrib(attrib); }

	public void glPopAttrib() { gl.glPopAttrib(); }

	public void glGenBuffers(int a, int b[], int c) { gl.glGenBuffers(a, b, c); }

	public Variable createProgram() {
		return null; // TODO
	}

	public void linkProgram(Variable program) {
		// TODO
	}

	public void useProgram(Variable program) {
		// TODO
	}

	public void mapProgramAttributes(Variable program) {
		// TODO
	}

	public Variable createShader(String type) {
		return null; // TODO
	}

	public void shaderSource(Variable shader, String source) {
		// TODO
	}

	public void compileShader(Variable shader) {
		// TODO
	}

	public void attachShader(Variable program, Variable shader) {
		// TODO
	}

	public void deleteShader(Variable shader) {
		// TODO
	}

	public Variable<String> createBuffer() {
		return null; // TODO
	}

	public void bindBuffer(String code, Variable buffer) {
		// TODO
	}

	public void bufferData(Variable buffer, List<Double> data) {
		// TODO
	}

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
			matrixStack.push(transform);
			transform = new TransformMatrix();
		}
	}

	public void popMatrix() {
		if (useGlMatrixStack) {
			gl.glPopMatrix();
		} else {
			transform = matrixStack.pop();
		}
	}

	public void glLoadIdentity() {
		if (useGlMatrixStack) {
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		} else {
			transform = new TransformMatrix();
		}
	}

	public void glMultMatrix(TransformMatrix m) { transform = transform.multiply(m); }

	public void setMatrix(TransformMatrix m) { transform = m; }

	public void glRasterPos(Vector pos) {
		if (enableDoublePrecision) {
			gl.glRasterPos3d(pos.getX(), pos.getY(), pos.getZ());
		} else {
			gl.glRasterPos3f((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
		}
	}

	/** It is recommended to use a {@link org.almostrealism.geometry.Camera} instead. */
	@Deprecated
	public void setViewport(int x, int y, int w, int h) {
		System.out.println("Setting viewport to [" + x + ", " + y + "][" + w + ", " + h + "]");
		gl.glViewport(x, y, w, h);

		// Since GLDriver maintains projection matrix internally,
		// the gl projection matrix is identity
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}

	/** It is recommended to use an {@link OrthographicCamera} instead. */
	@Deprecated
	public void gluOrtho2D(double left, double right, double bottom, double top) {
		glu.gluOrtho2D(left, right, bottom, top);
	}

	/** It is recommended to use a {@link org.almostrealism.geometry.Camera} instead. */
	@Deprecated public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
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
        Vector worldSpace = transform.transformAsLocation(in);
		Vector4d worldSpace_joml = new Vector4d(worldSpace.getX(), worldSpace.getY(), worldSpace.getZ(), 1d);
		Vector4d clipSpace_joml = projection_joml.transform(worldSpace_joml);

		clipSpace_joml.div(clipSpace_joml.w);

		return new Vector(clipSpace_joml.x, clipSpace_joml.y, clipSpace_joml.z);
	}

	protected Vector transformDirection(Vector in) {
		return transform.transformAsOffset(in);
	}

	protected Vector transformNormal(Vector in) {
		return transform.transformAsNormal(in);
	}

    private TransformMatrix jomlToTransformMatrix(Matrix4d m) {
        return new TransformMatrix(new double[][]{
                {m.m00(), m.m10(), m.m20(), m.m30()},
                {m.m01(), m.m11(), m.m21(), m.m31()},
                {m.m02(), m.m12(), m.m22(), m.m32()},
                {m.m03(), m.m13(), m.m23(), m.m33()}
        });
    }

	/**
	 * If {@link Camera} is null, load the identity matrix into the projection stack
	 * and modelview stack, otherwise delegate to {@link #glProjection(Camera)}.
	 *
	 * @param c Camera to use for projection, or null to reset matrices.
	 */
	public void setCamera(Camera c) {
		this.glProjection(c);
		this.camera = (OrthographicCamera) c;
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
		projection_joml = new Matrix4d();

		if (c instanceof PinholeCamera) {
			PinholeCamera camera = (PinholeCamera) c;

			Vector eye = camera.getLocation();
			Vector center = eye.add(camera.getViewingDirection());
			float width = (float) camera.getProjectionWidth();
			float height = (float) camera.getProjectionHeight();

			projection_joml = new Matrix4d()
					.perspective(camera.getHorizontalFOV(), width / height, 0.1, 1e9)
					.lookAt(new Vector3d(eye.getX(), eye.getY(), eye.getZ()),
							new Vector3d(center.getX(), center.getY(), center.getZ()),
							new Vector3d(0, 1, 0));
		}
		else if (c instanceof OrthographicCamera) {
			OrthographicCamera camera = (OrthographicCamera) c;

			// TODO: Orthographic projection matrix.
		}
	}

	protected TransformMatrix getPerspectiveMatrix(double fovyInDegrees, double aspectRatio, double near, double far) {
		double ymax, xmax;
		ymax = near * Math.tan(fovyInDegrees * Math.PI / 360.0);
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
						{0.0, 0.0, -((far + near) / temp4), -((temp * far) / temp4)},
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
		// TODO  Replace with code for rendering cube
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
