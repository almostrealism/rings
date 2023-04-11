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
import org.almostrealism.algebra.computations.ComplexFromAngle;
import org.almostrealism.algebra.computations.PairFromPairBank;
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
	public PairProducerBase build(Scalar angle, Scalar k, Scalar n, Producer<PackedCollection<Pair<?>>> bank, int length) {
		Producer<Scalar> angleProducer = v(angle);
		Producer<Scalar> kProducer = v(k);
		Producer<Scalar> nProducer = v(n);

		ScalarProducerBase halfN = scalarsMultiply(nProducer, v(0.5));
		ScalarProducerBase quarterN = scalarsMultiply(nProducer, v(0.25));
		ScalarProducerBase tripleQuarterN = scalarsMultiply(nProducer, v(0.75));

		ScalarProducerBase kPlusTripleQuarterN = scalarAdd(kProducer, tripleQuarterN);
		ScalarProducerBase kPlusHalfN = scalarAdd(kProducer, halfN);
		ScalarProducerBase kPlusQuarterN = scalarAdd(kProducer, quarterN);

		PairProducer a = new PairFromPairBank(bank, kProducer);
		PairProducer b = new PairFromPairBank(bank, kPlusQuarterN);
		PairProducer c = new PairFromPairBank(bank, kPlusHalfN);
		PairProducer d = new PairFromPairBank(bank, kPlusTripleQuarterN);

		if (kind == A) {
			return pairAdd(a, c);
		} else if (kind == B) {
			return pairAdd(b, d);
		}

		ScalarProducerBase angleK = scalarsMultiply(angleProducer, kProducer);
		PairProducer omega = new ComplexFromAngle(angleK);

		if (kind == EVEN) {
			return pairAdd(a, c);
		} else if (kind == ODD) {
			return pairSubtract(a, c).multiplyComplex(omega);
		}

		throw new IllegalArgumentException(String.valueOf(kind));
	}
}
