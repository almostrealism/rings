package com.almostrealism.primitives.test;

import com.almostrealism.primitives.SphereIntersectAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.math.MemoryBank;
import org.almostrealism.util.StaticProducer;
import org.junit.Test;

public class KernelizedIntersectionTest extends AbstractIntersectionTest {
	private int w = 2, h = 2;

	public PairBank getInput() {
		PairBank pixelLocations = new PairBank(w * h);

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				Pair p = pixelLocations.get(j * w + i);
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
		PairBank dim = PairBank.fromProducer(StaticProducer.of(new Pair(w, h)), w * h);
		ScalarBank output = new ScalarBank(input.getCount());

		System.out.println(combined.getFunctionDefinition());

		combined.kernelEvaluate(output, new MemoryBank[] { input, dim, input, dim });

		for (int i = 0; i < output.getCount(); i++) {
			System.out.println(output.get(i));
		}

//		System.out.println(output.get(0));
//		System.out.println(output.get(w * 50 + 50));
	}
}
