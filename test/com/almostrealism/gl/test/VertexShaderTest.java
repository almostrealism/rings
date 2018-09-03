package com.almostrealism.gl.test;

import org.junit.Test;

import com.almostrealism.gl.GLSLPrintWriter;
import com.almostrealism.gl.shaders.DotProductVertexShader;

public class VertexShaderTest {
	@Test
	public void test() {
		DotProductVertexShader s = new DotProductVertexShader();
		GLSLPrintWriter shaderOutput = new GLSLPrintWriter(System.out);
		shaderOutput.println(s);
	}
}
