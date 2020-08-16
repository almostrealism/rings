package com.almostrealism.primitives.test;

import com.almostrealism.primitives.SphereIntersectAt;
import com.almostrealism.projection.PinholeCameraRayAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import org.almostrealism.util.PassThroughProducer;
import org.almostrealism.util.StaticProducer;

public class AbstractIntersectionTest {
	protected SphereIntersectAt combined() {
		Vector viewDirection = new Vector(0.0, 0.0,  -1.0);
		Vector upDirection = new Vector(0.0, 1.0, 0.0);

		Vector w = (viewDirection.divide(viewDirection.length())).minus();

		Vector u = upDirection.crossProduct(w);
		u.divideBy(u.length());

		Vector v = w.crossProduct(u);

		return
				new SphereIntersectAt(new PinholeCameraRayAt(new PassThroughProducer<>(0),
						new StaticProducer<>(new Pair(100, 100)),
						new Vector(0.0, 0.0, 100.0),
						new Pair(1.0, 1.0),
						0.0, 1.0,
						u, v, w));
	}
}
