package com.almostrealism.renderable;

import com.almostrealism.gl.GLMaterial;
import com.almostrealism.projection.OrthographicCamera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.primitives.Pinhole;
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

public class GLDriver {
	public static final boolean enableDoublePrecision = false;

	private GL2 gl;
	private GLUT glut = new GLUT();
	private GLU glu = new GLU();

	private Stack<Integer> begins;

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
			gl.glColor4d(color.getRGB().getRed(), color.getRGB().getGreen(), color.getRGB().getBlue(), color.a);
		} else {
			gl.glColor4f((float) color.getRGB().getRed(),
						(float) color.getRGB().getGreen(),
						(float) color.getRGB().getBlue(),
						(float) color.a);
		}
	}

	public void glColor(double r, double g, double b, double a) {
		gl.glColor4d(r, g, b, a);
	}

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

	public void glGenTextures(int code, IntBuffer buf) { gl.glGenTextures(code, buf); }
	public void bindTexture(Texture t) { t.bind(gl); }
	public void glBindTexture(int code, int tex) { gl.glBindTexture(code, tex); }
	public void glTexImage2D(int a, int b, int c, int d, int e, int f, int g, int h, ByteBuffer buf) {
		gl.glTexImage2D(a, b, c, d, e, f, g, h, buf);
	}

	public void glTexGeni(int a, int b, int c) { gl.glTexGeni(a, b, c); }
	public void glTexEnvi(int a, int b, int c) { gl.glTexEnvi(a, b, c); }

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

	public void glRotate(double a, double b, double c, double d) {
		if (enableDoublePrecision) {
			gl.glRotated(a, b, c, d);
		} else {
			gl.glRotatef((float) a, (float) b, (float) c, (float) d);
		}
	}

	public void clearColorBuffer() { gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
	public void glClearColor(RGBA c) { gl.glClearColor(c.r(), c.g(), c.b(), (float) c.a); }
	public void glClearDepth(double d) {
		if (enableDoublePrecision) {
			gl.glClearDepth(d);
		} else {
			gl.glClearDepthf((float) d);
		}
	}

	public void glClear(int bits) { gl.glClear(bits); }

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

	public void glGenBuffers(int a, int b[], int c) { gl.glGenBuffers(a, b, c); }
	public void glBindBuffer(int code, int v) { gl.glBindBuffer(code, v); }
	public void glBufferData(int code, int l, ByteBuffer buf, int d) { gl.glBufferData(code, l, buf, d); }

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
	@Deprecated public void gluPerspective(double d1, double d2, double d3, double d4) { glu.gluPerspective(d1, d2, d3, d4); }
	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
		glu.gluLookAt(e.getX(), e.getY(), e.getZ(), c.getX(), c.getY(), c.getZ(), var13, var15, var17);
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

	public void glCullFace(int param) { gl.glCullFace(param); }

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

	protected GLException exceptionHelper(GLException gle) {
		if (gle.getMessage() == null) return gle;
		if (gle.getMessage().contains("May not call this between glBegin and glEnd")) {
			return new GLException(gle.getMessage() + " (begin = " + this.begins.peek() + ")");
		}

		return gle;
	}
}
