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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLArrayDataWrapper;
import com.jogamp.opengl.util.GLBuffers;
import org.almostrealism.space.BasicGeometry;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLSpatial extends RenderableGeometry<BasicGeometry> {
	/* Vertex array and color array are enabled for all objects, so their
		 * pointers must always be valid and non-null. Normal array is not
		 * used by the ground plane, so when its pointer is null then normal
		 * array usage is disabled.
		 *
		 * Vertex array is supposed to use GL.GL_FLOAT datatype and stride 0
		 * (i.e. tightly packed array). Color array is supposed to have 4
		 * components per color with GL.GL_UNSIGNED_BYTE datatype and stride 0.
		 * Normal array is supposed to use GL.GL_FLOAT datatype and stride 0.
		 */
	private int vboName, count;
	private int vComps, nComps;
	private ByteBuffer pBuffer = null;
	public FloatBuffer vertexArray = null;
	public FloatBuffer colorArray = null;
	public FloatBuffer normalArray = null;
	protected GLArrayDataWrapper vArrayData, cArrayData, nArrayData = null;

	public GLSpatial(GLDriver gl, int vertices, int vertexComponents, boolean useNormalArray) {
		super(new BasicGeometry());

		count = vertices;
		vComps = vertexComponents;
		nComps = useNormalArray ? 3 : 0;

		int bSize = GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * (vComps + DefaultGLCanvas.cComps + nComps);
		pBuffer = GLBuffers.newDirectByteBuffer(bSize);

		int pos = 0;
		int size = GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * vComps;
		vertexArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
		int vOffset = 0;
		pos += size;

		size = GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * DefaultGLCanvas.cComps;
		colorArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
		int cOffset = pos;
		pos += size;

		int nOffset = 0;
		if (useNormalArray) {
			size = GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * nComps;
			normalArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
			nOffset = pos;
			pos += size;
		}
		pBuffer.position(pos);
		pBuffer.flip();

		int[] tmp = new int[1];
		gl.glGenBuffers(1, tmp, 0);
		vboName = tmp[0];

		vArrayData = GLArrayDataWrapper.createFixed(GL2.GL_VERTEX_ARRAY, vComps, GL.GL_FLOAT, false,
				0, pBuffer, vboName, vOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
		cArrayData = GLArrayDataWrapper.createFixed(GL2.GL_COLOR_ARRAY, DefaultGLCanvas.cComps, GL.GL_FLOAT, false,
				0, pBuffer, vboName, cOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);

		if (useNormalArray) {
			nArrayData = GLArrayDataWrapper.createFixed(GL2.GL_NORMAL_ARRAY, nComps, GL.GL_FLOAT, false,
					0, pBuffer, vboName, nOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
		}
	}

	public void setCount(int c) {
		if (count != c) {
			throw new RuntimeException("diff count: " + count + " -> " + c);
		}

		count = c;
	}

	private boolean sealed = false;

	public void init(GLDriver gl) {

	}

	public void seal(GLDriver gl) {
		if (sealed) return;
		sealed = true;

		vertexArray.position(count);
		vertexArray.flip();
		colorArray.position(count);
		colorArray.flip();
		if (nComps > 0) {
			normalArray.position(count);
			normalArray.flip();
		}

		if (nComps > 0) {
			gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		}

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, pBuffer.limit(), pBuffer, GL.GL_STATIC_DRAW);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

		if (nComps > 0) {
			gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		}
	}

	private boolean first = true;

	public void render(GLDriver gl) {
		if (true) {
			if (first) {
				System.out.println("GLSpatial[WARN]: Need to upgrade to avoid vertex buffers.");
				first = false;
			}

			return;
		}

		seal(gl);

		if (nComps > 0) {
			gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		}

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName);

//		gl.glVertexPointer(vArrayData);  TODO
//		gl.glColorPointer(cArrayData);  TODO

		if (nComps > 0) {
//			gl.glNormalPointer(nArrayData); TODO
		}

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, count);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

		if (nComps > 0) {
			gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		}

		throw new NotImplementedException("Need a replacement for glNormalPointer");
	}
}