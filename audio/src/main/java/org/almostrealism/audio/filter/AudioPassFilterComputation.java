/*
 * Copyright 2022 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.filter;

import io.almostrealism.expression.Max;
import io.almostrealism.expression.Min;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.AudioFilterData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationComputationAdapter;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;

public class AudioPassFilterComputation extends OperationComputationAdapter<PackedCollection<?>> implements CodeFeatures {
	public static double MAX_INPUT = 0.99;

	private boolean high;

	public AudioPassFilterComputation(AudioFilterData data, Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance, Producer<PackedCollection<?>> input, boolean high) {
		super((Supplier) data.getOutput(),
				(Supplier) frequency,
				(Supplier) resonance,
				(Supplier) data.getSampleRate(),
				(Supplier) data.getC(),
				(Supplier) data.getA1(),
				(Supplier) data.getA2(),
				(Supplier) data.getA3(),
				(Supplier) data.getB1(),
				(Supplier) data.getB2(),
				(Supplier) data.getInputHistory0(),
				(Supplier) data.getInputHistory1(),
				(Supplier) data.getOutputHistory0(),
				(Supplier) data.getOutputHistory1(),
				(Supplier) data.getOutputHistory2(),
				(Supplier) input);
		this.high = high;
	}

	public ArrayVariable getOutput() { return getArgument(0, 1); }
	public ArrayVariable getFrequency() { return getArgument(1, 1); }
	public ArrayVariable getResonance() { return getArgument(2, 2); }
	public ArrayVariable getSampleRate() { return getArgument(3, 2); }
	public ArrayVariable getC() { return getArgument(4, 2); }
	public ArrayVariable getA1() { return getArgument(5, 2); }
	public ArrayVariable getA2() { return getArgument(6, 2); }
	public ArrayVariable getA3() { return getArgument(7, 2); }
	public ArrayVariable getB1() { return getArgument(8, 2); }
	public ArrayVariable getB2() { return getArgument(9, 2); }
	public ArrayVariable getInputHistory0() { return getArgument(10, 2); }
	public ArrayVariable getInputHistory1() { return getArgument(11, 2); }
	public ArrayVariable getOutputHistory0() { return getArgument(12, 2); }
	public ArrayVariable getOutputHistory1() { return getArgument(13, 2); }
	public ArrayVariable getOutputHistory2() { return getArgument(14, 2); }
	public ArrayVariable getInput() { return getArgument(15, 1); }

	protected String output() { return getOutput().ref(0); }
	protected String frequency() { return getFrequency().ref(0); }
	protected String resonance() { return getResonance().ref(0); }
	protected String sampleRate() { return getSampleRate().ref(0); }
	protected String c() { return getC().ref(0); }
	protected String a1() { return getA1().ref(0); }
	protected String a2() { return getA2().ref(0); }
	protected String a3() { return getA3().ref(0); }
	protected String b1() { return getB1().ref(0); }
	protected String b2() { return getB2().ref(0); }
	protected String inputHistory0() { return getInputHistory0().ref(0); }
	protected String inputHistory1() { return getInputHistory1().ref(0); }
	protected String outputHistory0() { return getOutputHistory0().ref(0); }
	protected String outputHistory1() { return getOutputHistory1().ref(0); }
	protected String outputHistory2() { return getOutputHistory2().ref(0); }
	protected String input() { return getInput().ref(0); }

	@Override
	public void prepareScope(ScopeInputManager manager) {
		super.prepareScope(manager);

		purgeVariables();

		String one = stringForDouble(1.0);

		if (high) {
			addVariable(getC().valueAt(0).assign(expression("tan(" + stringForDouble(Math.PI) +
									" * " + frequency() + " / " + sampleRate() + ")",
									getFrequency(), getSampleRate())));
			addVariable(getA1().valueAt(0).assign(expression(one +
							" / (" + one + " + " + resonance() + " * " + c() + " + " + c() + " * " + c() + ")",
					getResonance(), getC())));
			addVariable(getA2().valueAt(0).assign(expression(stringForDouble(-2.0) + " * " + a1(), getA1())));
			addVariable(getA3().valueAt(0).assign(getA1().valueAt(0)));
			addVariable(getB1().valueAt(0).assign(expression(stringForDouble(2.0) +
					" * (" + c() + " * " + c() + " - " + one + ") * " + a1(), getC(), getA1())));
			addVariable(getB2().valueAt(0).assign(
					expression("(" + one + " - " + resonance() + " * " + c() + " + " + c() + " * " + c() + ") * " + a1(),
							getResonance(), getC(), getA1())));
		} else {
			addVariable(getC().valueAt(0).assign(expression(
								one + " / tan(" + stringForDouble(Math.PI) +
										" * " + frequency() + " / " + sampleRate() + ")",
										getFrequency(), getSampleRate())));
			addVariable(getA1().valueAt(0).assign(expression(one +
							" / (" + one + " + " + resonance() + " * " + c() + " + " + c() + " * " + c() + ")",
							getResonance(), getC())));
			addVariable(getA2().valueAt(0).assign(expression(stringForDouble(2.0) + " * " + a1(), getA1())));
			addVariable(getA3().valueAt(0).assign(getA1().valueAt(0)));
			addVariable(getB1().valueAt(0).assign(expression(stringForDouble(2.0) +
							" * (" + one + " - " + c() + " * " + c() + ") * " + a1(), getC(), getA1())));
			addVariable(getB2().valueAt(0).assign(
					expression("(" + one + " - " + resonance() + " * " + c() + " + " + c() + " * " + c() + ") * " + a1(),
							getResonance(), getC(), getA1())));
		}

		String input = "max(min(" + input() + ", " + e(MAX_INPUT).getSimpleExpression() +
						"), " + e(-MAX_INPUT).getSimpleExpression() + ")";

		addVariable(getOutput().valueAt(0).assign(
				e(a1() + " * " + input + " + " +
						a2() + " * " + inputHistory0() + " + " +
						a3() + " * " + inputHistory1() + " - " +
						b1() + " * " + outputHistory0() + " - " +
						b2() + " * " + outputHistory1())));

		addVariable(getInputHistory1().valueAt(0).assign(getInputHistory0().valueAt(0)));
		addVariable(getInputHistory0().valueAt(0).assign(new Max(new Min(getInput().valueAt(0), e(MAX_INPUT)), e(-MAX_INPUT))));
		addVariable(getOutputHistory2().valueAt(0).assign(getOutputHistory1().valueAt(0)));
		addVariable(getOutputHistory1().valueAt(0).assign(getOutputHistory0().valueAt(0)));
		addVariable(getOutputHistory0().valueAt(0).assign(getOutput().valueAt(0)));
	}

//	@Override
//	public Scope getScope() {
//		Scope<Scalar> parentScope = super.getScope();
//		HybridScope<Scalar> scope = new HybridScope<>(this);
//		scope.getVariables().addAll(parentScope.getVariables());
//
//		Consumer<String> code = scope.code();
//		code.accept("if (fabs(" + getOutput().valueAt(0).getSimpleExpression() + ") > 0.99) {\n");
//		code.accept("    printf(\"a1 = %f, a2 = %f, a3 = %f, b1 = %f, b2 = %f\\n\", " +
//				a1() + ", " + a2() + ", " + a3() + ", " + b1() + ", " + b2()  + ");\n");
//		code.accept("}\n");
//
//		return scope;
//	}
}
