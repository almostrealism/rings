package org.almostrealism.audio.computations.test;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarBankProducerBase;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;


public class DitherAndRemoveDcOffsetTest implements TestFeatures {
	// TODO @Test
	public void ditherAndRemoveDcOffset() {
		ScalarBank bank = new ScalarBank(160);
		IntStream.range(0, 160).forEach(i -> bank.set(i, 100 * Math.random()));

		ScalarBankProducerBase dither = ditherAndRemoveDcOffset(160, v(320, 0), v(Scalar.class, 1));
		ScalarBank result = dither.get().evaluate(bank, new Scalar(1.0));
		System.out.println(Arrays.toString(IntStream.range(0, 160).mapToDouble(i -> result.get(i).getValue()).toArray()));
		assertNotEquals(0.0, result.get(20));
	}
}
