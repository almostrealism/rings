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

import io.almostrealism.code.OperationAdapter;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.computations.PairBankFromPairsBuilder;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

public class SplitRadixFFT implements Evaluable<PackedCollection<Pair<?>>>, CodeFeatures {
	public static final double SQRT_2 = Math.sqrt(2.0);

	private final RadixComputationFactory radix4Part1Pos;
	private final RadixComputationFactory radix4Part1Neg;
	private final RadixComputationFactory radix4Part2Pos;
	private final RadixComputationFactory radix4Part2Neg;
	private final RadixComputationFactory radix2A;
	private final RadixComputationFactory radix2B;
	private final RadixComputationFactory radix2Even;
	private final RadixComputationFactory radix2Odd;

	private Evaluable<PackedCollection<Pair<?>>> result;

	public SplitRadixFFT(int bins, boolean forward) {
		radix2A = new Radix2(Radix2.A);
		radix2B = new Radix2(Radix2.B);
		radix2Even = new Radix2(Radix2.EVEN);
		radix2Odd = new Radix2(Radix2.ODD);
		radix4Part1Pos = new Radix4(0, true);
		radix4Part1Neg = new Radix4(0, false);
		radix4Part2Pos = new Radix4(1, true);
		radix4Part2Neg = new Radix4(1, false);

		result = transform(v(2 * bins, 0), bins, forward).get();
		((OperationAdapter) result).compile();
	}

	@Override
	public PackedCollection<Pair<?>> evaluate(Object... args) {
		return result.evaluate(args);
	}

	protected PairBankFromPairsBuilder transform(Producer<PackedCollection<Pair<?>>> values, int length, boolean forward) {
		return calculateTransform(values, length, !forward, !forward);
	}

	private PairBankFromPairsBuilder calculateTransform(Producer<PackedCollection<Pair<?>>> input, int length, boolean inverseTransform, boolean isFirstSplit) {
		int powerOfTwo = 31 - Integer.numberOfLeadingZeros(length);

		if (1 << powerOfTwo != length) {
			throw new IllegalArgumentException("Length of the <input> must be a power of 2 (i.e. 2^N, N = 1, 2, ...), actual: " + length);
		}

		if (length >= 4) {
			int halfN = length / 2;
			int quarterN = halfN / 2;

			PairBankFromPairsBuilder radix2 = new PairBankFromPairsBuilder(halfN);
			PairBankFromPairsBuilder radix4Part1 = new PairBankFromPairsBuilder(quarterN);
			PairBankFromPairsBuilder radix4Part2 = new PairBankFromPairsBuilder(quarterN);
			Scalar angle = new Scalar(2 * Math.PI / length);

			RadixComputationFactory radix4Part1P, radix4Part2P;

			if (!inverseTransform) {
				angle.setValue(angle.getValue() * -1);
				radix4Part1P = radix4Part1Pos;
				radix4Part2P = radix4Part2Pos;
			} else {
				radix4Part1P = radix4Part1Neg;
				radix4Part2P = radix4Part2Neg;
			}

			Scalar ks = new Scalar(0);
			Scalar ns = new Scalar(length);
			for (int k = 0; k < quarterN; k++) {
				ks.setValue(k);

				//radix-2 part
				radix2.set(k, radix2A.build(angle, ks, ns, input, length));
				radix2.set(k + quarterN, radix2B.build(angle, ks, ns, input, length));

				//radix-4 part
				radix4Part1.set(k, radix4Part1P.build(angle, ks, ns, input, length));
				radix4Part2.set(k, radix4Part2P.build(angle, ks, ns, input, length));
			}

			PairBankFromPairsBuilder radix2FFT = calculateTransform(radix2, radix2.getCount(), inverseTransform, false);
			PairBankFromPairsBuilder radix4Part1FFT = calculateTransform(radix4Part1, radix2.getCount(), inverseTransform, false);
			PairBankFromPairsBuilder radix4Part2FFT = calculateTransform(radix4Part2, radix2.getCount(), inverseTransform, false);

			PairBankFromPairsBuilder transformed = new PairBankFromPairsBuilder(length);

			for (int k = 0; k < quarterN; k++) {
				int doubleK = 2 * k;
				int quadrupleK = 2 * doubleK;

				Pair<?> len = new Pair<>(length, length);

				if (inverseTransform && isFirstSplit) {
					transformed.set(doubleK, divide(radix2FFT.get(k), v(len)));
					transformed.set(quadrupleK + 1, divide(radix4Part1FFT.get(k), v(len)));
					transformed.set(doubleK + halfN, divide(radix2FFT.get(k + quarterN), v(len)));
					transformed.set(quadrupleK + 3, divide(radix4Part2FFT.get(k), v(len)));
				} else {
					transformed.set(doubleK, radix2FFT.get(k));
					transformed.set(quadrupleK + 1, radix4Part1FFT.get(k));
					transformed.set(doubleK + halfN, radix2FFT.get(k + quarterN));
					transformed.set(quadrupleK + 3, radix4Part2FFT.get(k));
				}
			}

			return transformed;
		}

		if (length >= 2) {
			return calculateRadix2Transform(input, length, inverseTransform, isFirstSplit);
		}

		return (PairBankFromPairsBuilder) input; // TODO  Will not work for edge case
	}

	private PairBankFromPairsBuilder calculateRadix2Transform(Producer<PackedCollection<Pair<?>>> input, int length, boolean inverseTransform, boolean isFirstSplit) {
		Scalar ns = new Scalar(length);

		if (length >= 2) {
			int halfN = length / 2;
			PairBankFromPairsBuilder even = new PairBankFromPairsBuilder(halfN);
			PairBankFromPairsBuilder odd = new PairBankFromPairsBuilder(halfN);
			Scalar angle = new Scalar(2 * Math.PI / length);

			if (!inverseTransform) {
				angle.setValue(angle.getValue() * -1);
			}

			Scalar ks = new Scalar();
			for (int k = 0; k < halfN; k++) {
				even.set(k, radix2Even.build(angle, ks, ns, input, length));
				odd.set(k, radix2Odd.build(angle, ks, ns, input, length));
			}

			PairBankFromPairsBuilder evenFFT = calculateRadix2Transform(even, even.getCount(), inverseTransform, false);
			PairBankFromPairsBuilder oddFFT = calculateRadix2Transform(odd, odd.getCount(), inverseTransform, false);

			PairBankFromPairsBuilder transformed = new PairBankFromPairsBuilder(length);

			Pair<?> len = new Pair<>(length, length);

			for (int k = 0; k < halfN; k++) {
				int doubleK = k * 2;
				if (inverseTransform && isFirstSplit) {
					transformed.set(doubleK, divide(evenFFT.get(k), v(len)));
					transformed.set(doubleK + 1, divide(oddFFT.get(k), v(len)));
				} else {
					transformed.set(doubleK, evenFFT.get(k));
					transformed.set(doubleK + 1, oddFFT.get(k));
				}
			}

			return transformed;
		}

		return (PairBankFromPairsBuilder) input; // TODO  Will not work for edge case
	}
}
