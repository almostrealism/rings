package com.almostrealism.raytracer.engine;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Triple;
import org.almostrealism.algebra.Vector;
import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.util.ParameterizedFactory;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class DistanceEstimationLightingEngine extends LightingEngine {
	public static final int MAX_RAY_STEPS = 30;

	private DistanceEstimator estimator;

	public DistanceEstimationLightingEngine(DistanceEstimator estimator) {
		super(new ParameterizedFactory<Ray, ContinuousField>() {
			private Ray r;

			@Override
			public <T extends Ray> void setParameter(Class<T> aClass, T a) {
				this.r = a;
			}

			@Override
			public ContinuousField construct() {
				double totalDistance = 0.0;

				int steps;

				Vector from = r.getOrigin();
				Vector direction = r.getDirection();

				steps: for (steps = 0; steps < MAX_RAY_STEPS; steps++) {
					Vector p = from.add(direction.multiply(totalDistance));
					r = new Ray(p, direction);
					double distance = estimator.estimateDistance(r);
					totalDistance += distance;
					if (distance < 0.0001) break steps;
				}

				if (totalDistance > 0.1 && totalDistance < Math.pow(10, 6))
	 				System.out.println("Total distance = " + totalDistance);

				return new Locus(r.getOrigin(), r.getDirection());
			}
		});

		this.estimator = estimator;
	}

	private static class Locus extends ArrayList<Callable<Ray>> implements ContinuousField {
		public Locus(Vector location, Vector normal) {
			this.add(() -> { return new Ray(location, normal); });
		}

		@Override
		public Vector getNormalAt(Vector vector) {
			try {
				return get(0).call().getDirection();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public Vector operate(Triple triple) {
			try {
				return get(0).call().getOrigin();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public String toString() {
			try {
				return String.valueOf(get(0).call());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "null";
		}
	}
}
