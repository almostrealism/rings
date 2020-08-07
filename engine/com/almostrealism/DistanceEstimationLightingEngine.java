/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism;

import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.*;
import org.almostrealism.algebra.computations.RayDirection;
import org.almostrealism.algebra.computations.VectorFutureAdapter;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shadable;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.color.ShaderSet;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public class DistanceEstimationLightingEngine extends LightingEngine {
	public static final int MAX_RAY_STEPS = 30;

	private DistanceEstimator estimator;
	private ShaderSet shaders;

	public DistanceEstimationLightingEngine(Producer<Ray> ray, Curve<RGB> surface,
											Collection<? extends Curve<RGB>> otherSurfaces,
											Light light, Iterable<Light> otherLights,
											ShaderContext p, DistanceEstimator estimator, ShaderSet shaders) {
		// TODO
		super(
				/*
				new Producer<ContinuousField>() {
			@Override
			public ContinuousField evaluate(Object args[]) {
				Ray r = ray.evaluate(args);

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

//				if (totalDistance > 0.1 && totalDistance < Math.pow(10, 6))
//	 				System.out.println("Total distance = " + totalDistance);

				return new Locus(r.getOrigin(), r.getDirection(), shaders,
						new ShaderContext(estimator instanceof Producer ?
											((Producer) estimator) : null, p.getLight()));
			}

			@Override
			public void compact() {
				// TODO  Hardware acceleration
			}
		},*/
				null,
				surface, otherSurfaces, light, otherLights, p);

		this.estimator = estimator;
		this.shaders = shaders;
	}

	public static class Locus extends ArrayList<Producer<Ray>> implements ContinuousField, Callable<Producer<RGB>>, Shadable {
		private ShaderSet shaders;
		private ShaderContext params;

		public Locus(Vector location, Vector normal, ShaderSet s, ShaderContext p) {
			this.add(new StaticProducer<>(new Ray(location, normal)));
			shaders = s;
			params = p;
		}

		@Override
		public VectorProducer getNormalAt(Producer<Vector> vector) {
			return new RayDirection(get(0));
		}

		@Override
		public Vector operate(Triple triple) {
			try {
				return get(0).evaluate(new Object[] { triple }).getOrigin();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public Scope getScope(String prefix) { throw new RuntimeException("getScope not implemented"); } // TODO

		public ShaderSet getShaders() { return shaders; }

		public String toString() {
			try {
				return String.valueOf(get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "null";
		}

		@Override
		public Producer<RGB> shade(ShaderContext parameters) {
			try {
				Producer<RGB> color = null;

				if (shaders != null)
					color = shaders.shade(parameters, this);

				return color;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Producer<RGB> call() throws Exception {
			return shade(params);
		}
	}
}
