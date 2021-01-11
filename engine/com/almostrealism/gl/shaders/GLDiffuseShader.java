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

package com.almostrealism.gl.shaders;

import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import io.almostrealism.code.NameProvider;

public class GLDiffuseShader extends VertexShader {
	@Override
	public Scope<? extends Variable> getScope() {
		Scope<Variable> s = new Scope<>();

		Variable pos = new Variable("position", Vector.class, (Vector) null);
		pos.setAnnotation("attribute");
		Variable tex = new Variable("texcoord", Pair.class, (Vector) null);
		pos.setAnnotation("varying");
		s.getVariables().add(tex);

		Scope<Variable> main = new Scope();
		s.add(main);

//		main.add(new Variable("texcoord", null, new VectorProduct(pos))); TODO

		return s;
	}
}
