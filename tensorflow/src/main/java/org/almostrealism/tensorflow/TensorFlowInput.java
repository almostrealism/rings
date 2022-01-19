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

import org.tensorflow.op.core.Placeholder;
import org.tensorflow.types.TFloat64;

public class TensorFlowInput {
	private String name;
	private Placeholder<TFloat64> inputOperand, outputOperand;
	private TFloat64 value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Placeholder<TFloat64> getInputOperand() {
		return inputOperand;
	}

	public void setInputOperand(Placeholder<TFloat64> inputOperand) {
		this.inputOperand = inputOperand;
	}

	public Placeholder<TFloat64> getOutputOperand() {
		return outputOperand;
	}

	public void setOutputOperand(Placeholder<TFloat64> outputOperand) {
		this.outputOperand = outputOperand;
	}

	public TFloat64 getValue() {
		return value;
	}

	public void setValue(TFloat64 value) {
		this.value = value;
	}
}
