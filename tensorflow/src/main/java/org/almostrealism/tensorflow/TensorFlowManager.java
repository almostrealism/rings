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

import io.almostrealism.code.Variable;
import org.tensorflow.Operand;
import org.tensorflow.Signature;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TFloat64;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TensorFlowManager {
	private Ops tf;
	private Map<String, TensorFlowInput> variables;
	private long inputCount;
	private Signature.Builder signature;

	public TensorFlowManager(Ops ops) {
		this.tf = ops;
		this.variables = new HashMap<>();
		this.signature = Signature.builder();
	}

	public Ops tf() { return tf; }

	public Signature.Builder signature() { return signature; }

	protected Collection<TensorFlowInput> getVariables() {
		return variables.values();
	}

	public void addVariable(Variable<?, ?> v) {
		String name = v.getRootDelegate().getName();
		Operand dest = getVariable(name).getOutputOperand();
		Operand src = ((TensorFlowConstant) v.getExpression()).toOperand(this);
		System.out.println("Assigning " + dest + " to " + src);
		signature().output(name, src);
	}

	public TFloat64 valueOf(double value) {
		return TFloat64.scalarOf(value);
	}

	public TensorFlowInput getVariable(String name) {
		if (!variables.containsKey(name)) {
			variables.put(name, input(name));
		}

		return variables.get(name);
	}

	public TensorFlowInput nextInput() { return input("tf_" + inputCount++); }

	public TensorFlowInput input(String name) {
		TensorFlowInput input = new TensorFlowInput();
		input.setName(name);
		input.setInputOperand(tf.placeholder(TFloat64.class));
		input.setOutputOperand(tf.placeholder(TFloat64.class));
		System.out.println("Adding input " + input.getName());
		variables.put(name, input);
		signature().input(input.getName(), input.getInputOperand());
		return input;
	}
}
