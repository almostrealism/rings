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

import io.almostrealism.code.ComputeContext;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Pair;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.AcceleratedEvaluable;
import org.almostrealism.hardware.MemoryData;
import org.almostrealism.hardware.cl.CLComputeContext;

import java.util.function.Supplier;

public class ComplexFFT extends AcceleratedEvaluable<PackedCollection<Pair<?>>, PackedCollection<Pair<?>>> implements Evaluable<PackedCollection<Pair<?>>> {
	public ComplexFFT(ComputeContext<MemoryData> context, int count, boolean forward, Supplier<Evaluable<? extends PackedCollection<Pair<?>>>> input) {
		super((CLComputeContext) context, "transform", () -> args -> Pair.bank(count),
				input, Ops.o().p(new Pair(count, forward ? 0 : 1)));
		int powerOfTwo = 31 - Integer.numberOfLeadingZeros(count);

		if (1 << powerOfTwo != count) {
			throw new IllegalArgumentException("ComplexFFT not supported for " +
								count + " bins (use " + (1 << powerOfTwo) + ")");
		}

		if (!forward) {
			// TODO  Support backward
			throw new UnsupportedOperationException();
		}
	}
}
