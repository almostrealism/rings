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

import io.almostrealism.c.CPrintWriter;
import io.almostrealism.code.CodePrintWriter;

import java.io.PrintWriter;

/**
 * {@link GLSLCodePrintWriter} is a {@link CodePrintWriter} implementation for writing GLSL,
 * a shading language similar to C.
 */
public class GLSLCodePrintWriter extends CPrintWriter {
	private PrintWriter p;

	/**
	 * Constructs a new {@link GLSLCodePrintWriter} for writing GLSL to the specified {@link PrintWriter}.
	 */
	public GLSLCodePrintWriter(PrintWriter p) { super(p); }
}
