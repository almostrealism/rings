/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.tensorflow.test;

import io.almostrealism.code.DefaultNameProvider;
import io.almostrealism.code.DefaultScopeInputManager;
import io.almostrealism.code.Execution;
import io.almostrealism.scope.Scope;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.tensorflow.TensorFlowArgument;
import org.almostrealism.tensorflow.TensorFlowComputeContext;
import org.almostrealism.tensorflow.TensorFlowConstant;
import org.almostrealism.tensorflow.TensorFlowInstructionSet;
import org.almostrealism.CodeFeatures;
import org.junit.Test;

public class TFScopeTest implements CodeFeatures {
	private int counter = 0;

	@Test
	public void scope() {
		DefaultNameProvider nameProvider = new DefaultNameProvider("test");
		DefaultScopeInputManager manager = new DefaultScopeInputManager((p, input) -> new TensorFlowArgument<>(p, p.getArgumentName(counter++), input));

		Scalar s = new Scalar();
		TensorFlowArgument destination = (TensorFlowArgument) manager.argumentForInput(nameProvider).apply(p(s));
		TensorFlowConstant v = new TensorFlowConstant(1.0);

		Scope<?> scope = new Scope<>();
		scope.getVariables().add(destination.valueAt(0).assign(v));
		System.out.println("Scope has " + scope.getArgumentVariables().size() + " arguments");

		TensorFlowComputeContext ctx = new TensorFlowComputeContext();
		TensorFlowInstructionSet op = ctx.deliver(scope);
		Execution consumer = op.get();
		consumer.accept(new Object[] { s });
	}
}
