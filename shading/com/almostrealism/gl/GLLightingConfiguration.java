package com.almostrealism.gl;

import com.jogamp.opengl.math.FixedPoint;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;

public class GLLightingConfiguration {
	public FloatBuffer light0Position;
	public FloatBuffer light0Diffuse;
	public FloatBuffer light1Position;
	public FloatBuffer light1Diffuse;
	public FloatBuffer light2Position;
	public FloatBuffer light2Diffuse;

	public GLLightingConfiguration() {
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
