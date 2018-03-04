package com.almostrealism.renderable;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.gl2.GLUT;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Stack;

public class GLDriver {
	public static final boolean enableDoublePrecision = false;

	private GL2 gl;
	private GLUT glut = new GLUT();
	private GLU glu = new GLU();

	private Stack<Integer> begins;

	public GLDriver(GL2 gl) {
		this.gl = gl;
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

	public void glColor(double r, double g, double b, double a) {
		gl.glColor4d(r, g, b, a);
	}

	@Deprecated
	public void glColor4f(float r, float g, float b, float a) { gl.glColor4f(r, g, b, a); }

	/** It is recommended to use {@link #glColor(RGB)} instead. */
	public void glColorPointer(GLArrayDataWrapper data) { gl.glColorPointer(data); }

	public void glMaterial(int code, int prop, FloatBuffer buf) { gl.glMaterialfv(code, prop, buf); }
	public void glMaterial(int code, int prop, float f) { gl.glMaterialf(code, prop, f); }

	public void glVertex(Vector v) {
		if (enableDoublePrecision) {
			gl.glVertex3d(v.getX(), v.getY(), v.getZ());
		} else {
			gl.glVertex3f((float) v.getX(),
						(float) v.getY(),
						(float) v.getZ());
		}
	}

	@Deprecated public void glVertexPointer(int a, int b, int c, FloatBuffer f) { gl.glVertexPointer(a, b, c, f); }
	public void glVertexPointer(GLArrayDataWrapper data) { gl.glVertexPointer(data); }

	public void glNormalPointer(GLArrayDataWrapper data) { gl.glNormalPointer(data); }

	public void glLight(int light, int prop, FloatBuffer buf) { gl.glLightfv(light, prop, buf); }
	public void glLight(int light, int prop, float f) { gl.glLightf(light, prop, f); }

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

	public void clearColorBuffer() { gl.glClear(GL.GL_COLOR_BUFFER_BIT); }

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

	public void glGenBuffers(int a, int b[], int c) { gl.glGenBuffers(a, b, c); }
	public void glBindBuffer(int code, int v) { gl.glBindBuffer(code, v); }
	public void glBufferData(int code, int l, ByteBuffer buf, int d) { gl.glBufferData(code, l, buf, d); }

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
	public void gluPerspective(double d1, double d2, double d3, double d4) { glu.gluPerspective(d1, d2, d3, d4); }
	public void gluLookAt(Vector e, Vector c, double var13, double var15, double var17) {
		glu.gluLookAt(e.getX(), e.getY(), e.getZ(), c.getX(), c.getY(), c.getZ(), var13, var15, var17);
	}

	public void glFlush() { gl.glFlush(); }
	public int glEnd() { gl.glEnd(); return begins.pop(); }
	@Deprecated public void glDisable(int code) { gl.glDisable(code); }
	@Deprecated public void glDisableClientState(int code) { gl.glDisableClientState(code); }

	public void glutWireCube(double size) {
		// TODO  Replace with our own cube code
		glut.glutWireCube((float) size);
	}

	public void glutSolidSphere(double radius, int slices, int stacks) {
		// TODO  Replace with our own sphere code
		glut.glutSolidSphere(radius, slices, stacks);
	}

	public void glDrawArrays(int code, int a, int b) { gl.glDrawArrays(code, a, b); }
}
