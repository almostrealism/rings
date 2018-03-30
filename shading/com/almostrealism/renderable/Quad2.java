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
import com.jogamp.opengl.util.GLBuffers;
import io.almostrealism.code.CodePrintWriter;
import org.almostrealism.algebra.Pair;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.FloatBuffer;

public class Quad2 implements Renderable {
	public static final int UPPER_LEFT = 0;
	public static final int LOWER_LEFT = 1;
	public static final int LOWER_RIGHT = 2;
	public static final int UPPER_RIGHT = 3;

	private static final int NUM_VECS = 4;

	private Pair vecs[];
	private FloatBuffer vertBuf;

	@Override
	public void init(GLDriver gl) {
	}

	@Override
	public void display(GLDriver gl) {
		gl.glQuads();
		gl.glVertex(vecs[0]);
		gl.glVertex(vecs[3]);
		gl.glVertex(vecs[1]);
		gl.glVertex(vecs[2]);
		gl.glEnd();
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		throw new NotImplementedException("TODO");
	}

	public FloatBuffer getVertexBuffer() {
		if (vertBuf == null) {
			vertBuf = GLBuffers.newDirectFloatBuffer(12);
			vertBuf.put(
					new float[] {
							(float) vecs[0].getX(), (float) vecs[0].getY(),
							(float) vecs[1].getX(), (float) vecs[1].getY(),
							(float) vecs[2].getX(), (float) vecs[2].getY(),
							(float) vecs[3].getX(), (float) vecs[3].getY()
					});
		}

		return vertBuf;
	}
}
