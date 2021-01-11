package com.almostrealism.gl.shaders;

import io.almostrealism.code.Method;
import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import io.almostrealism.code.NameProvider;

public class DotProductVertexShader extends VertexShader {
	@Override
	public Scope getScope() {
		Scope<Variable> s = new Scope();
		s.getVariables().add(new Variable<Float>("test", 0.0f));
		s.getMethods().add(new Method("gl", "dotProduct"));
		return s;
	}
}
