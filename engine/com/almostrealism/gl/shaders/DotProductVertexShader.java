package com.almostrealism.gl.shaders;

import java.util.ArrayList;
import java.util.HashMap;

import io.almostrealism.code.Method;
import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;

public class DotProductVertexShader extends VertexShader {
	@Override
	public Scope getScope(String prefix) {
		Scope<Variable> s = new Scope();
		s.getVariables().add(new Variable<Float>("test", 0.0f));
		s.getMethods().add(new Method("gl", "dotProduct", new ArrayList<>(), new HashMap<>()));
		return s;
	}
}
