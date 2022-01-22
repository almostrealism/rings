package com.almostrealism.gl.shaders;

import io.almostrealism.scope.Method;
import io.almostrealism.scope.Scope;
import io.almostrealism.scope.Variable;

public class DotProductVertexShader extends VertexShader {
	@Override
	public Scope getScope() {
		Scope<Variable> s = new Scope();
		s.getVariables().add(new Variable("test", 0.0f));
		s.getMethods().add(new Method("gl", "dotProduct"));
		return s;
	}
}
