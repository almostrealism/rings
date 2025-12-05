package org.almostrealism.tensorflow;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.uml.Multiple;

import java.util.function.Supplier;

public class TensorFlowArgument<T> extends ArrayVariable<T> {
	public TensorFlowArgument(String name,
							  Supplier<Evaluable<? extends Multiple<T>>> producer) {
		super(name, producer);
	}
}
