package com.almostrealism.renderable;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;

import java.util.Stack;

public class GLDriver {
	public static final boolean enableDoublePrecision = false;

	private GL2 gl;
	private GLUT glut = new GLUT();

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

	public void glVertex(Vector v) {
		if (enableDoublePrecision) {
			gl.glVertex3d(v.getX(), v.getY(), v.getZ());
		} else {
			gl.glVertex3f((float) v.getX(),
						(float) v.getY(),
						(float) v.getZ());
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

	public void uv(Pair texCoord) {
		if (enableDoublePrecision) {
			gl.glTexCoord2d(texCoord.getA(), texCoord.getB());
		} else {
			gl.glTexCoord2f((float) texCoord.getA(), (float) texCoord.getB());
		}
	}

	public void glLoadIdentity() { gl.glLoadIdentity(); }

	public void glFlush() { gl.glFlush(); }
	public int glEnd() { gl.glEnd(); return begins.pop(); }

	public void glutWireCube(double size) {
		// TODO  Replace with our own cube code
		glut.glutWireCube((float) size);
	}

	public void glutSolidSphere(double radius, int slices, int stacks) {
		// TODO  Replace with our own sphere code
		glut.glutSolidSphere(radius, slices, stacks);
	}
}
