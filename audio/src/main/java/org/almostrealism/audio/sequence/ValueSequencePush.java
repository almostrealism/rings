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

package org.almostrealism.audio.sequence;

import io.almostrealism.code.HybridScope;
import io.almostrealism.code.ProducerComputation;
import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.computations.Switch;
import org.almostrealism.audio.data.ValueSequenceData;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

public class ValueSequencePush extends ValueSequenceComputation implements CodeFeatures {
	private Switch choice;

	public ValueSequencePush(ValueSequenceData data, Producer<Scalar> durationFrames, PackedCollection<?> output, Producer<PackedCollection<?>>... choices) {
		this(data, durationFrames, output, true, choices);
	}

	public ValueSequencePush(ValueSequenceData data, Producer<Scalar> durationFrames, PackedCollection<?> output, boolean repeat, Producer<PackedCollection<?>>... choices) {
		super(data, durationFrames, output, repeat, choices);
		choice = new Switch((ProducerComputation) divide(wavePosition(), durationFrames()),
						choices(in -> a(1, output(), in)));
	}

	@Override
	public void prepareScope(ScopeInputManager manager) {
		super.prepareScope(manager);
		choice.prepareScope(manager);

		scope = new HybridScope(this);
		scope.add(choice.getScope());
	}
}
