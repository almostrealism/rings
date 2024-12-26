package org.almostrealism.tree.test;

import org.almostrealism.graph.PathElement;
import io.almostrealism.relation.Evaluable;

import java.util.ArrayList;
import java.util.List;

public class TestPathElement implements PathElement, Evaluable<Object> {
	private List<Evaluable> children;

	public TestPathElement() { this(new ArrayList<>()); }

	public TestPathElement(List<Evaluable> children) {
		this.children = children;
	}

	@Override
	public Iterable<Evaluable> getDependencies() {
		return children;
	}

	@Override
	public Object evaluate(Object[] objects) {
		return null;
	}
}
