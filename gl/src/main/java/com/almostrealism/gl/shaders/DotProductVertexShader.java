/*
 * Copyright 2024 Michael Murray
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

package com.almostrealism.gl.shaders;

import io.almostrealism.code.ExpressionFeatures;
import io.almostrealism.kernel.KernelStructureContext;
import io.almostrealism.scope.Method;
import io.almostrealism.scope.Scope;
import io.almostrealism.scope.Variable;

public class DotProductVertexShader extends VertexShader implements ExpressionFeatures {
	@Override
	public Scope getScope(KernelStructureContext context) {
		Scope<Variable> s = new Scope();
		s.getVariables().add(declare("test", e(0.0f)));
		s.getMethods().add(new Method("gl", "dotProduct"));
		return s;
	}
}
