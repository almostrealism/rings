package org.almostrealism.tensorflow;

import io.almostrealism.lang.LanguageOperations;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.NameProvider;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.uml.Multiple;

import java.util.function.Supplier;

public class TensorFlowArgument<T> extends ArrayVariable<T> {
	public TensorFlowArgument(LanguageOperations lang, NameProvider np, String name,
							  Supplier<Evaluable<? extends Multiple<T>>> producer) {
		super(np, name, producer);
	}
}
