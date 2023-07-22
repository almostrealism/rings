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

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.*;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

public class Radix2 implements RadixComputationFactory, CodeFeatures {
	public static final int A = 0;
	public static final int B = 1;
	public static final int EVEN = 2;
	public static final int ODD = 3;

	private int kind;

	public Radix2(int kind) {
		this.kind = kind;
	}

	@Override
	public Producer<Pair<?>> build(Scalar angle, Scalar k, Scalar n, Producer<PackedCollection<Pair<?>>> bank, int length) {
		Producer<Scalar> angleProducer = v(angle);
		Producer<Scalar> kProducer = v(k);
		Producer<Scalar> nProducer = v(n);

		Producer<Scalar> halfN = scalarsMultiply(nProducer, v(0.5));
		Producer<Scalar> quarterN = scalarsMultiply(nProducer, v(0.25));
		Producer<Scalar> tripleQuarterN = scalarsMultiply(nProducer, v(0.75));

		Producer<Scalar> kPlusTripleQuarterN = scalarAdd(kProducer, tripleQuarterN);
		Producer<Scalar> kPlusHalfN = scalarAdd(kProducer, halfN);
		Producer<Scalar> kPlusQuarterN = scalarAdd(kProducer, quarterN);

		Producer<Pair<?>> a = pairFromBank(bank, c(kProducer, 0));
		Producer<Pair<?>> b = pairFromBank(bank, c(kPlusQuarterN, 0));
		Producer<Pair<?>> c = pairFromBank(bank, c(kPlusHalfN, 0));
		Producer<Pair<?>> d = pairFromBank(bank, c(kPlusTripleQuarterN, 0));

		if (kind == A) {
			return add(a, c);
		} else if (kind == B) {
			return add(b, d);
		}

		Producer<Scalar> angleK = scalarsMultiply(angleProducer, kProducer);
		Producer<Pair<?>> omega = complexFromAngle(angleK);

		if (kind == EVEN) {
			return add(a, c);
		} else if (kind == ODD) {
			return multiplyComplex(subtract(a, c), omega);
		}

		throw new IllegalArgumentException(String.valueOf(kind));
	}
}
