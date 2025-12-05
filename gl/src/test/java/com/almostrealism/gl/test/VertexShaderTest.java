/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.gl.test;

import com.almostrealism.gl.GLSLPrintWriter;
import com.almostrealism.gl.shaders.DotProductVertexShader;
import io.almostrealism.code.Precision;
import org.almostrealism.c.CLanguageOperations;
import org.junit.Test;

public class VertexShaderTest {
	@Test
	public void test() {
		DotProductVertexShader s = new DotProductVertexShader();
		GLSLPrintWriter shaderOutput =
				new GLSLPrintWriter(System.out,
					new CLanguageOperations(Precision.FP32, false, false));
		shaderOutput.println(s.getScope(null));
	}
}
