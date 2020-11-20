package com.almostrealism.raytracer;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.ImmutableVector;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.relation.Operator;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.Producer;
import org.almostrealism.util.Provider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Thing extends AbstractSurface {
	private Plane p = new Plane(Plane.XY);

	@Override
	public ContinuousField intersectAt(Producer ray) {
		if (Math.random() > 0.5) {
			return this.p.intersectAt(ray);
		} else {
			return new ShadableIntersection(this, () -> ray, scalar(-1));
		}
	}

	@Override
	public Operator<Scalar> expect() {
		return p.expect();
	}

	@Override
	public Operator<Scalar> get() {
		return p.get();
	}

	@Override
	public Operator<Scalar> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return p.get(timeout, unit);
	}

	@Override
	public Producer<Vector> getNormalAt(Producer<Vector> point) {
		return compileProducer(new ImmutableVector(0.0, 0.0, 1.0));
	}
}
