package com.almostrealism.primitives.test;

import com.almostrealism.primitives.SphereIntersectAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.junit.Test;

public class IntersectionCompactTest extends AbstractIntersectionTest {
	@Test
	public void compact() {
		SphereIntersectAt combined = combined();
		combined.compact();

		Scalar r = combined.evaluate(new Object[] { new Pair(50, 50) });
		System.out.println(r.getX());
		assert r.getX() > 0;

		r = combined.evaluate(new Object[] { new Pair(0, 0) });
		System.out.println(r.getX());
		assert r.getX() < 0;
	}
}
