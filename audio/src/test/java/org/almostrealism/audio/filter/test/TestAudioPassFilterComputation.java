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

package org.almostrealism.audio.filter.test;

import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.AudioFilterData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.DynamicOperationComputationAdapter;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;

public class TestAudioPassFilterComputation extends DynamicOperationComputationAdapter implements CodeFeatures {
	public TestAudioPassFilterComputation(AudioFilterData data, Producer<PackedCollection<?>> input, Supplier output) {
		super(output,
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
	}

	public ArrayVariable getOutput() { return getArgument(0, 1); }
	public ArrayVariable getSampleRate() { return getArgument(1, 2); }
	public ArrayVariable getC() { return getArgument(2, 2); }
	public ArrayVariable getA1() { return getArgument(3, 2); }
	public ArrayVariable getA2() { return getArgument(4, 2); }
	public ArrayVariable getA3() { return getArgument(5, 2); }
	public ArrayVariable getB1() { return getArgument(6, 2); }
	public ArrayVariable getB2() { return getArgument(7, 2); }
	public ArrayVariable getInputHistory0() { return getArgument(8, 2); }
	public ArrayVariable getInputHistory1() { return getArgument(9, 2); }
	public ArrayVariable getOutputHistory0() { return getArgument(10, 2); }
	public ArrayVariable getOutputHistory1() { return getArgument(11, 2); }
	public ArrayVariable getOutputHistory2() { return getArgument(12, 2); }
	public ArrayVariable getInput() { return getArgument(13, 1); }

	protected String output() { return getOutput().ref(0); }
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

		addVariable(getOutput().valueAt(0).assign(getInputHistory1().valueAt(0)));
		addVariable(getInputHistory1().valueAt(0).assign(getInputHistory0().valueAt(0)));
		addVariable(getInputHistory0().valueAt(0).assign(getInput().valueAt(0)));
	}
}
