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

package org.almostrealism.tensorflow;

import io.almostrealism.code.Execution;
import io.almostrealism.scope.Argument;
import io.almostrealism.code.InstructionSet;
import io.almostrealism.scope.Scope;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.impl.dense.DoubleDenseNdArray;
import org.tensorflow.types.TFloat64;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TensorFlowInstructionSet implements InstructionSet {
	private ConcreteFunction func;
	private List<Argument<?>> arguments;
	private Collection<TensorFlowInput> variables;

	public TensorFlowInstructionSet(Scope<?> scope) {
		func = ConcreteFunction.create(tf -> operation(scope).apply(new TensorFlowManager(tf)));
	}

	@Override
	public Execution get(String function, int argCount) {
		return (args, dependsOn) -> {
			if (dependsOn != null) dependsOn.waitFor();
			assignArgs(args, func.call(getArguments()));
			return null;
		};
	}

	private void assignArgs(Object args[], Map<String, Tensor> result) {
		List<Tensor> outputs = arguments.stream().map(Argument::getName).map(result::get).collect(Collectors.toList());
		IntStream.range(0, outputs.size()).forEach(i -> {
			// ((MemoryData) args[i]).reassign(new TensorFlowMemory(outputs.get(i)));
			System.out.println("Result: " + ((DoubleDenseNdArray) outputs.get(i)).get(0));
		});
	}

	private Map<String, Tensor> getArguments() {
		Map<String, Tensor> argMap = new HashMap<>();
		variables.forEach(input -> {
			argMap.put(input.getName(), input.getValue());
		});

		arguments.forEach(arg -> {
			argMap.put(arg.getName(), Tensor.of(TFloat64.class, Shape.scalar()));
		});

		return argMap;
	}

	private Function<TensorFlowManager, Signature> operation(Scope<?> scope) {
		return tf -> {
			arguments = scope.getArguments();
			variables = tf.getVariables();
			scope.getVariables().forEach(tf::addVariable);
			return tf.signature().build();
		};
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	@Override
	public void destroy() {

	}
}
