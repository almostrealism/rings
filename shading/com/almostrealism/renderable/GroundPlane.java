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

import com.almostrealism.gl.DefaultGLCanvas;
import com.almostrealism.gl.GLDriver;

public class GroundPlane extends GLSpatial {
	private static final int scale = 4;
	private static final int yBegin = -15, yEnd = 15;    // ends are non-inclusive
	private static final int xBegin = -15, xEnd = 15;
	private static final int triangleCount = (yEnd - yBegin) * (xEnd - xBegin) * 2;
	private static final int vertices = triangleCount * 3;

	int x, y;
	int currentVertex, currentQuad;

	private static final int vcomps = 2;

	public GroundPlane(GLDriver gl) {
		super(gl, vertices, vcomps, false);

		currentQuad = 0;
		currentVertex = 0;

		for (y = yBegin; y < yEnd; ++y) {
			for (x = xBegin; x < xEnd; ++x) {
				float color;
				int i, a;
				color = ((float) (DefaultGLCanvas.randomUInt() % 255)) / 255.0f;
				for (i = currentVertex * DefaultGLCanvas.cComps; i < (currentVertex + 6) * DefaultGLCanvas.cComps; i += DefaultGLCanvas.cComps) {
					colorArray.put(i, color);
					colorArray.put(i + 1, color);
					colorArray.put(i + 2, color);

					if (3 < DefaultGLCanvas.cComps) {
						colorArray.put(i + 3, 0);
					}
				}

				// Axis bits for quad triangles:
				// x: 011100 (0x1c), y: 110001 (0x31)  (clockwise)
				// x: 001110 (0x0e), y: 100011 (0x23)  (counter-clockwise)
				for (a = 0; a < 6; ++a) {
					final int xm = x + ((0x1c >> a) & 1);
					final int ym = y + ((0x31 >> a) & 1);
					final float m = (float) (Math.cos(xm * 2) * Math.sin(ym * 4) * 0.75f);
					vertexArray.put(currentVertex * vcomps, (xm * scale + m));
					vertexArray.put(currentVertex * vcomps + 1, (ym * scale + m));

					if (2 < vcomps) {
						vertexArray.put(currentVertex * vcomps + 2, 0f);
					}

					++currentVertex;
				}
				++currentQuad;
			}
		}

		seal(gl);
	}
}
