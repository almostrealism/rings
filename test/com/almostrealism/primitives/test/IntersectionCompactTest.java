package com.almostrealism.primitives.test;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.Ray;
import org.almostrealism.util.PassThroughProducer;
import org.almostrealism.util.StaticProducer;
import org.junit.Test;

public class IntersectionCompactTest {
	@Test
	public void compact() {
		Vector viewDirection = new Vector(0.0, 0.0,  -1.0);
		Vector upDirection = new Vector(0.0, 1.0, 0.0);

		Vector w = (viewDirection.divide(viewDirection.length())).minus();

		Vector u = upDirection.crossProduct(w);
		u.divideBy(u.length());

		Vector v = w.crossProduct(u);

		SphereIntersectAtPinholeCameraRayAt combined =
			new SphereIntersectAtPinholeCameraRayAt(new PassThroughProducer<>(0),
												new StaticProducer<>(new Pair(100, 100)),
												new Vector(0.0, 0.0, 100.0),
												new Pair(1.0, 1.0),
												0.0, 1.0,
												u, v, w);

		Ray r = combined.evaluate(new Object[] { new Pair(50, 50) });
		System.out.println(r.getOrigin().getX());
		assert r.getOrigin().getX() > 0;

		r = combined.evaluate(new Object[] { new Pair(0, 0) });
		System.out.println(r.getOrigin().getX());
		assert r.getOrigin().getX() < 0;
	}
}
