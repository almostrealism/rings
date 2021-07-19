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

package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import com.jogamp.opengl.GL2;

import org.almostrealism.algebra.Vector;

public class Gear extends RenderableGLAdapter {
	private float innerRadius, outerRadius;
	private float width, toothDepth;
	private int teeth;
	
	public Gear(float innerRadius, float outerRadius, float width, int teeth, float toothDepth) {
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.width = width;
		this.teeth = teeth;
		this.toothDepth = toothDepth;
	}

	public void display(GLDriver gl) {
		int i;
		float r0, r1, r2;
		float angle, da;
		float u, v, len;

		r0 = innerRadius;
		r1 = outerRadius - toothDepth / 2.0f;
		r2 = outerRadius + toothDepth / 2.0f;

		da = 2.0f * (float) Math.PI / teeth / 4.0f;

		gl.glShadeModel(GL2.GL_FLAT);

		gl.glNormal(new Vector(0.0, 0.0, 1.0));

		/* draw front face */
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), width * 0.5));

			if(i < teeth) {
				gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), width * 0.5));
				gl.glVertex(new Vector(r1 * Math.cos(angle + 3.0f * da), r1 * Math.sin(angle + 3.0f * da), width * 0.5));
			}
		}
		gl.glEnd();

		/* draw front sides of teeth */
		gl.glBegin(GL2.GL_QUADS);
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + da), r2 * Math.sin(angle + da), width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + 2.0f * da), r2 * Math.sin(angle + 2.0f * da), width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle + 3.0f * da), r1 * Math.sin(angle + 3.0f * da), width * 0.5));
		}
		gl.glEnd();

		/* draw back face */
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), -width * 0.5));
			gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), -width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle + 3 * da), r1 * Math.sin(angle + 3 * da), -width * 0.5));
			gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), -width * 0.5));
		}
		gl.glEnd();

		/* draw back sides of teeth */
		gl.glBegin(GL2.GL_QUADS);
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex(new Vector(r1 * Math.cos(angle + 3 * da), r1 * Math.sin(angle + 3 * da), -width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + 2 * da), r2 * Math.sin(angle + 2 * da), -width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + da), r2 * Math.sin(angle + da), -width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), -width * 0.5));
		}
		gl.glEnd();

		/* draw outward faces of teeth */
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle), r1 * Math.sin(angle), -width * 0.5));
			u = r2 * (float)Math.cos(angle + da) - r1 * (float)Math.cos(angle);
			v = r2 * (float)Math.sin(angle + da) - r1 * (float)Math.sin(angle);
			len = (float)Math.sqrt(u * u + v * v);
			u /= len;
			v /= len;
			gl.glNormal(new Vector(v, -u, 0.0));
			gl.glVertex(new Vector(r2 * Math.cos(angle + da), r2 * Math.sin(angle + da), width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + da), r2 * Math.sin(angle + da), -width * 0.5));
			gl.glNormal(new Vector(Math.cos(angle), Math.sin(angle), 0.0));
			gl.glVertex(new Vector(r2 * Math.cos(angle + 2 * da), r2 * Math.sin(angle + 2 * da), width * 0.5));
			gl.glVertex(new Vector(r2 * Math.cos(angle + 2 * da), r2 * Math.sin(angle + 2 * da), -width * 0.5));
			u = r1 * (float) Math.cos(angle + 3 * da) - r2 * (float) Math.cos(angle + 2 * da);
			v = r1 * (float) Math.sin(angle + 3 * da) - r2 * (float) Math.sin(angle + 2 * da);
			gl.glNormal(new Vector(v, -u, 0.0));
			gl.glVertex(new Vector(r1 * Math.cos(angle + 3 * da), r1 * Math.sin(angle + 3 * da), width * 0.5));
			gl.glVertex(new Vector(r1 * Math.cos(angle + 3 * da), r1 * Math.sin(angle + 3 * da), -width * 0.5));
			gl.glNormal(new Vector(Math.cos(angle), Math.sin(angle), 0.0));
		}

		gl.glVertex(new Vector(r1 * Math.cos(0), r1 * Math.sin(0), width * 0.5));
		gl.glVertex(new Vector(r1 * Math.cos(0), r1 * Math.sin(0), -width * 0.5));
		gl.glEnd();

		gl.glShadeModel(GL2.GL_SMOOTH);

		/* draw inside radius cylinder */
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glNormal(new Vector(-Math.cos(angle), -Math.sin(angle), 0.0));
			gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), -width * 0.5));
			gl.glVertex(new Vector(r0 * Math.cos(angle), r0 * Math.sin(angle), width * 0.5));
		}
		gl.glEnd();
	}

	public String toString() { return "Gear[" + outerRadius + " radius, " + teeth + " teeth]"; }
}
