/*
 * Copyright 2023 Michael Murray
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

import io.almostrealism.code.PhysicalScope;
import io.almostrealism.code.ProducerComputationBase;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.HybridScope;
import io.almostrealism.scope.Scope;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.hardware.ComputerFeatures;
import org.almostrealism.hardware.DestinationSupport;
import org.almostrealism.hardware.MemoryData;
import org.almostrealism.hardware.MemoryDataComputation;
import org.almostrealism.hardware.mem.MemoryDataDestination;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultEnvelopeComputation extends ProducerComputationBase<MemoryData, Scalar>
		implements ScalarProducer, MemoryDataComputation<Scalar>, DestinationSupport<Scalar>, ComputerFeatures {

	private Supplier<Scalar> destination;

	public DefaultEnvelopeComputation(Producer<Scalar> notePosition) {
		this.destination = () -> Scalar.blank().get().evaluate();
		this.setInputs(List.of(new MemoryDataDestination(this, ScalarBank::new), (Supplier) notePosition));
		init();
	}

	@Override
	public int getMemLength() { return 2; }

	@Override
	public void setDestination(Supplier<Scalar> destination) { this.destination = destination; }

	@Override
	public Supplier<Scalar> getDestination() { return destination; }

	/**
	 * @return  PhysicalScope#GLOBAL
	 */
	@Override
	public PhysicalScope getDefaultPhysicalScope() { return PhysicalScope.GLOBAL; }

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
}
