package com.almostrealism.primitives.test;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.RandomRay;
import org.almostrealism.geometry.Ray;
import org.almostrealism.math.AcceleratedProducer;
import org.almostrealism.util.Producer;

public class SphereIntersectAtPinholeCameraRayAt extends AcceleratedProducer<Ray> {
	public SphereIntersectAtPinholeCameraRayAt(Producer<Pair> pos, Producer<Pair> sd,
											   Vector location, Pair projectionDimensions,
											   double blur, double focalLength,
											   Vector u, Vector v, Vector w) {
		super("sphereIntersectAt_pinholeCameraRayAt",
				new Producer[] { Ray.blank(), pos, sd, new RandomRay() },
				new Object[] { location, projectionDimensions,
						new Pair(blur, blur),
						new Scalar(focalLength),
						u, v, w });
	}
}
