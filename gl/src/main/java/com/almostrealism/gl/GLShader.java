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

import io.almostrealism.scope.Scope;
import io.almostrealism.scope.Variable;
import org.almostrealism.algebra.PairFunction;
import org.almostrealism.color.RGB;

/**
 * {@link GLShader} is a {@link Scope} implementation which encodes a color pipeline
 * specified as a {@link PairFunction} taking screen coordinates to {@link RGB}s.
 */
public class GLShader extends Scope<Variable> {
	private PairFunction<RGB> pipeline;

	/**
	 * Create a {@link GLShader} for the specified {@link PairFunction} color pipeline.
	 */
	public GLShader(PairFunction<RGB> colorPipeline) {
		this.pipeline = colorPipeline;
	}
}
