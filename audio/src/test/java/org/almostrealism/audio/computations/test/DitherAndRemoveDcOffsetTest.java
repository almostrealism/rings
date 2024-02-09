package org.almostrealism.audio.computations.test;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBankProducerBase;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.util.TestFeatures;

import java.util.Arrays;
import java.util.stream.IntStream;


public class DitherAndRemoveDcOffsetTest implements TestFeatures {
	// TODO @Test
	public void ditherAndRemoveDcOffset() {
		PackedCollection<Scalar> bank = Scalar.scalarBank(160);
		IntStream.range(0, 160).forEach(i -> bank.set(i, 100 * Math.random()));

		ExpressionComputation<PackedCollection<Scalar>> dither = ditherAndRemoveDcOffset(160, v(320, 0), v(Scalar.shape(), 1));
		PackedCollection<Scalar> result = dither.get().evaluate(bank, new Scalar(1.0));
		System.out.println(Arrays.toString(IntStream.range(0, 160).mapToDouble(i -> result.get(i).getValue()).toArray()));
		assertNotEquals(0.0, result.get(20));
	}
}
