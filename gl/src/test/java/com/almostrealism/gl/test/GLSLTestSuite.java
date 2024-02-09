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

package com.almostrealism.gl.test;

import com.almostrealism.gl.GLSLPrintWriter;
import io.almostrealism.code.Precision;
import org.almostrealism.algebra.Pair;
import io.almostrealism.code.Computation;
import org.almostrealism.c.CLanguageOperations;
import org.junit.Test;

public class GLSLTestSuite {
	@Test
	public void encodeComputations() {
		/*
		try {
			RayTracedScene s = RayTracingTest.generateScene();
//			encodePairFunction(s); TODO
		} catch (IOException e) {
			e.printStackTrace();
		}
		 */
	}

	public void encodePairFunction(Computation<Pair> f) {
		if (f instanceof Computation == false) {
			throw new IllegalArgumentException(f + " is not a Computation, a requirement for encoding to GLSL");
		}

		GLSLPrintWriter p = new GLSLPrintWriter(System.out, new CLanguageOperations(Precision.FP32, false, false));
		p.println(f.getScope());
	}

	public static void main(String args[]) {
		new GLSLTestSuite().encodeComputations();
//		System.exit(0); TODO
	}
}
