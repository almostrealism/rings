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

import com.jogamp.opengl.math.FixedPoint;
import com.jogamp.opengl.util.GLBuffers;
import org.almostrealism.color.Light;

import java.nio.FloatBuffer;

public class GLLightingConfiguration {
	public FloatBuffer light0Position;
	public FloatBuffer light0Diffuse;
	public FloatBuffer light1Position;
	public FloatBuffer light1Diffuse;
	public FloatBuffer light2Position;
	public FloatBuffer light2Diffuse;

	public GLLightingConfiguration(Light l[]) {
		// TODO  For every point light, specify a light here
		light0Position = GLBuffers.newDirectFloatBuffer(4);
		light0Diffuse = GLBuffers.newDirectFloatBuffer(4);
		light1Position = GLBuffers.newDirectFloatBuffer(4);
		light1Diffuse = GLBuffers.newDirectFloatBuffer(4);
		light2Position = GLBuffers.newDirectFloatBuffer(4);
		light2Diffuse = GLBuffers.newDirectFloatBuffer(4);

		light0Position.put(new float[]{FixedPoint.toFloat(-0x40000), 1.0f, 1.0f, 0.0f});
		light0Diffuse.put(new float[]{1.0f, FixedPoint.toFloat(0x6666), 0.0f, 1.0f});
		light1Position.put(new float[]{1.0f, FixedPoint.toFloat(-0x20000), -1.0f, 0.0f});
		light1Diffuse.put(new float[]{FixedPoint.toFloat(0x11eb), FixedPoint.toFloat(0x23d7), FixedPoint.toFloat(0x5999), 1.0f});
		light2Position.put(new float[]{-1.0f, 0.0f, FixedPoint.toFloat(-0x40000), 0.0f});
		light2Diffuse.put(new float[]{FixedPoint.toFloat(0x11eb), FixedPoint.toFloat(0x2b85), FixedPoint.toFloat(0x23d7), 1.0f});

		light0Position.flip();
		light0Diffuse.flip();
		light1Position.flip();
		light1Diffuse.flip();
		light2Position.flip();
		light2Diffuse.flip();
	}
}
