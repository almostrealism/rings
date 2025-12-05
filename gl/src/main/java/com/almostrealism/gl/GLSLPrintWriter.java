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

package com.almostrealism.gl;

import io.almostrealism.lang.CodePrintWriter;
import io.almostrealism.lang.LanguageOperations;
import org.almostrealism.c.CPrintWriter;
import org.almostrealism.io.PrintWriter;

import java.io.OutputStream;

/**
 * {@link GLSLPrintWriter} is a {@link CodePrintWriter} implementation for writing GLSL,
 * a shading language similar to C.
 */
public class GLSLPrintWriter extends CPrintWriter {
	public GLSLPrintWriter(OutputStream out, LanguageOperations lang) { super(out, null, lang.getPrecision()); }

	/**
	 * Constructs a new {@link GLSLPrintWriter} for writing GLSL to the specified {@link PrintWriter}.
	 */
	public GLSLPrintWriter(PrintWriter p, LanguageOperations lang) { super(p, null, lang.getPrecision()); }
}
