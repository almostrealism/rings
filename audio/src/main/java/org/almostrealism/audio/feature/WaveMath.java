package org.almostrealism.audio.feature;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.computations.ScalarBankDotProduct;
import org.almostrealism.CodeFeatures;

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

	public Scalar dot(ScalarBank a, ScalarBank b) {
		assert a.getCount() == b.getCount();
		return getDot(a.getCount()).evaluate(a, b);
	}

	public synchronized Evaluable<? extends Scalar> getDot(int count) {
		if (!dotEvals.containsKey(count)) {
			Scalar output = new Scalar();
			ScalarBank temp = new ScalarBank(count);
			dotEvals.put(count, new ScalarBankDotProduct(count, v(Scalar.shape(), 0), v(Scalar.shape(), 1)).get());
		}

		return dotEvals.get(count);
	}
}
