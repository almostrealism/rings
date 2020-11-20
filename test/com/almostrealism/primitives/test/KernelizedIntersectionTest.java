package com.almostrealism.primitives.test;

import com.almostrealism.primitives.SphereIntersectAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.util.Provider;
import org.junit.Assert;
import org.junit.Test;

public class KernelizedIntersectionTest extends AbstractIntersectionTest {
	public PairBank getInput() {
		PairBank pixelLocations = new PairBank(width * height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Pair p = pixelLocations.get(j * width + i);
				p.setMem(new double[] { i, j });
			}
		}

		return pixelLocations;
	}

	@Test
	public void kernel() {
		SphereIntersectAt combined = combined();
		combined.compact();

		PairBank input = getInput();
		PairBank dim = PairBank.fromProducer(pair(width, height).get(), width * height);
		ScalarBank output = new ScalarBank(input.getCount());

		System.out.println(combined.getFunctionDefinition());

		System.out.println("KernelizedIntersectionTest: Invoking kernel...");
		combined.kernelEvaluate(output, new MemoryBank[] { input, dim });

		System.out.println("KernelizedIntersectionTest: Comparing...");
		for (int i = 0; i < output.getCount(); i++) {
			Scalar value = combined.evaluate(new Object[] { input.get(i), dim.get(i) });
			Assert.assertEquals(value.getValue(), output.get(i).getValue(), Math.pow(10, -10));
		}
	}
}
