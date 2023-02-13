/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.audio.computations;

import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.HybridScope;
import io.almostrealism.scope.Scope;
import io.almostrealism.expression.Expression;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.hardware.DynamicProducerComputationAdapter;
import org.almostrealism.hardware.MemoryData;

import java.util.function.Consumer;
import java.util.function.IntFunction;

public class DefaultEnvelopeComputation extends DynamicProducerComputationAdapter<MemoryData, Scalar> implements ScalarProducer {
	public DefaultEnvelopeComputation(Producer<Scalar> notePosition) {
		super(2, Scalar.blank(), ScalarBank::new, new Producer[] { notePosition });
	}

	@Override
	public Scope<Scalar> getScope() {
		HybridScope<Scalar> scope = new HybridScope<>(this);

		String position = getArgument(1).valueAt(0).getExpression();
		String result = ((ArrayVariable) getOutputVariable()).valueAt(0).getExpression();

		Consumer<String> code = scope.code();
		code.accept("if (" + position + " > 1.0) {\n");
		code.accept("	" + result + " = 0.0;\n");
		code.accept("} else if (" + position + " < 0.1) {\n");
		code.accept("	" + result + " = " + position + " / 0.1;\n");
		code.accept("} else {\n");
		code.accept("	" + result + " = cos(" + position + " * M_PI_2);\n");
		code.accept("}\n");

		return scope;
	}

	@Override
	public IntFunction<Expression<Double>> getValueFunction() {
		return null;
	}
}
