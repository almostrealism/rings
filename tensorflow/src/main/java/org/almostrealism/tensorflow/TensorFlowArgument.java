package org.almostrealism.tensorflow;

import io.almostrealism.lang.LanguageOperations;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.NameProvider;
import io.almostrealism.relation.Evaluable;

import java.util.function.Supplier;

public class TensorFlowArgument<T> extends ArrayVariable<T> {
	public TensorFlowArgument(LanguageOperations lang, NameProvider np, String name, Supplier<Evaluable<? extends T>> producer) {
		super(np, name, producer);
	}
}
