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

		Producer<Scalar> halfN = scalarsMultiply(nProducer, scalar(0.5));
		Producer<Scalar> quarterN = scalarsMultiply(nProducer, scalar(0.25));
		Producer<Scalar> tripleQuarterN = scalarsMultiply(nProducer, scalar(0.75));

		Producer<Scalar> kPlusTripleQuarterN = scalarAdd(kProducer, tripleQuarterN);
		Producer<Scalar> kPlusHalfN = scalarAdd(kProducer, halfN);
		Producer<Scalar> kPlusQuarterN = scalarAdd(kProducer, quarterN);

		Producer<Pair<?>> a = pairFromBank(bank, c(kProducer, 0));
		Producer<Pair<?>> b = pairFromBank(bank, c(kPlusQuarterN, 0));
		Producer<Pair<?>> c = pairFromBank(bank, c(kPlusHalfN, 0));
		Producer<Pair<?>> d = pairFromBank(bank, c(kPlusTripleQuarterN, 0));

		Producer<Pair<?>> bMinusD = subtract(b, d);
		Producer<Pair<?>> aMinusC = subtract(a, c);

		Producer<Pair<?>> imaginaryTimesSub;

		if (pos) {
			imaginaryTimesSub = pair(r(bMinusD).minus(), l(bMinusD));
		} else {
			imaginaryTimesSub = pair(r(bMinusD), l(bMinusD).minus());
		}

		Producer<Scalar> angleK = scalarsMultiply(angleProducer, kProducer);
		Producer<Scalar> angleK3 = scalarsMultiply(angleK, scalar(3));
		Producer<Pair<?>> omega = complexFromAngle(angleK);
		Producer<Pair<?>> omegaToPowerOf3 = complexFromAngle(angleK3);

		if (part == 0) {
			return multiplyComplex(subtract(aMinusC, imaginaryTimesSub), omega);
		} else {
			return multiplyComplex(add(aMinusC, imaginaryTimesSub), omegaToPowerOf3);
		}
	}
}
