/*
 * Copyright 2021 Michael Murray
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

public class Radix4 implements RadixComputationFactory, CodeFeatures {
	private int part;
	private boolean pos;

	public Radix4(int part, boolean pos) {
		this.part = part;
		this.pos = pos;
	}

	@Override
	public Producer<Pair<?>> build(Scalar angle, Scalar k, Scalar n, Producer<PackedCollection<Pair<?>>> bank, int length) {
		Producer<Scalar> angleProducer = v(angle);
		Producer<Scalar> kProducer = v(k);
		Producer<Scalar> nProducer = v(n);

		ScalarProducerBase halfN = scalarsMultiply(nProducer, v(0.5));
		ScalarProducerBase quarterN = scalarsMultiply(nProducer, v(0.25));
		ScalarProducerBase tripleQuarterN = scalarsMultiply(nProducer, v(0.75));

		ScalarProducerBase kPlusTripleQuarterN = scalarAdd(kProducer, tripleQuarterN);
		ScalarProducerBase kPlusHalfN = scalarAdd(kProducer, halfN);
		ScalarProducerBase kPlusQuarterN = scalarAdd(kProducer, quarterN);

		PairProducerBase a = pairFromBank(bank, kProducer);
		PairProducerBase b = pairFromBank(bank, kPlusQuarterN);
		PairProducerBase c = pairFromBank(bank, kPlusHalfN);
		PairProducerBase d = pairFromBank(bank, kPlusTripleQuarterN);

		Producer<Pair<?>> bMinusD = subtract(b, d);
		Producer<Pair<?>> aMinusC = subtract(a, c);

		PairProducerBase imaginaryTimesSub;

		if (pos) {
			imaginaryTimesSub = pair(r(bMinusD).minus(), l(bMinusD));
		} else {
			imaginaryTimesSub = pair(r(bMinusD), l(bMinusD).minus());
		}

		ScalarProducerBase angleK = scalarsMultiply(angleProducer, kProducer);
		ScalarProducerBase angleK3 = scalarsMultiply(angleK, v(3));
		Producer<Pair<?>> omega = complexFromAngle(angleK);
		Producer<Pair<?>> omegaToPowerOf3 = complexFromAngle(angleK3);

		if (part == 0) {
			return multiplyComplex(subtract(aMinusC, imaginaryTimesSub), omega);
		} else {
			return multiplyComplex(add(aMinusC, imaginaryTimesSub), omegaToPowerOf3);
		}
	}
}
