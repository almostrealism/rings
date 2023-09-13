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

package org.almostrealism.audio.feature;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class WaveMath implements CodeFeatures {
	private final Map<Integer, Evaluable<? extends Scalar>> dotEvals = new HashMap<>();

	public WaveMath() {
		IntStream.range(0, 64).forEach(i -> getDot(i + 1));
		getDot(160);
		getDot(320);
		getDot(400);
	}

	public static int gcd(int m, int n) {
		if (m == 0 || n == 0) {
			if (m == 0 && n == 0) {  // gcd not defined, as all integers are divisors.
				System.err.println("Undefined GCD since m = 0, n = 0.");
			}

			return m == 0 ? n > 0 ? n : -n : m > 0 ? m : -m;
			// return absolute value of whichever is nonzero
		}

		while (true) {
			m %= n;
			if (m == 0) return n > 0 ? n : -n;
			n %= m;
			if (n == 0) return m > 0 ? m : -m;
		}
	}

	public static int lcm(int m, int n) {
		assert m > 0 && n > 0;
		int gcd = gcd(m, n);
		return gcd * (m / gcd) * (n / gcd);
	}

	public Scalar dot(PackedCollection<Scalar> a, PackedCollection<Scalar> b) {
		assert a.getCount() == b.getCount();
		return getDot(a.getCount()).evaluate(a, b);
	}

	public synchronized Evaluable<? extends Scalar> getDot(int count) {
		if (!dotEvals.containsKey(count)) {
			Scalar output = new Scalar();
			PackedCollection<Scalar> temp = Scalar.scalarBank(count);

			Producer<PackedCollection<?>> a = subset(shape(count, 1), v(shape(count, 2), 0), 0);
			Producer<PackedCollection<?>> b = subset(shape(count, 1), v(shape(count, 2), 1), 0);
			dotEvals.put(count, scalar(multiply(a, b).sum()).get());
		}

		return dotEvals.get(count);
	}
}
