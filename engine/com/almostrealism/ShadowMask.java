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

import com.almostrealism.lighting.DirectionalAmbientLight;
import com.almostrealism.lighting.PointLight;
import com.almostrealism.raytracer.Settings;
import org.almostrealism.algebra.ClosestIntersection;
import org.almostrealism.algebra.Intersectable;
import org.almostrealism.algebra.Intersection;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.Producer;

public class ShadowMask implements Producer<RGB> {
	private Light light;
	private Iterable<Intersectable> surfaces;
	private Producer<Vector> point;

	public ShadowMask(Light light, Iterable<Intersectable> surfaces, Producer<Vector> point) {
		this.light = light;
		this.surfaces = surfaces;
		this.point = point;
	}

	@Override
	public RGB evaluate(Object[] args) {
		Vector p = point.evaluate(args);

		double maxDistance = -1.0;
		Vector direction = null;

		if (light instanceof PointLight) {
			direction = ((PointLight) light).getLocation().subtract(p);
			direction = direction.divide(direction.length());
			maxDistance = direction.length();
		} else if (light instanceof DirectionalAmbientLight) {
			direction = ((DirectionalAmbientLight) light).getDirection().minus();
		} else {
			return new RGB(1.0, 1.0, 1.0);
		}

		final Vector fdirection = direction;

		Producer<Ray> shadowRay = new Producer() {
			@Override
			public Ray evaluate(Object[] args) {
				return new Ray(p, fdirection);
			}

			@Override
			public void compact() { }
		};

		ClosestIntersection<ShadableIntersection> intersection = new ClosestIntersection(shadowRay, surfaces);
		Ray r = intersection.get(0).evaluate(args);
		double intersect = 0.0;
		if (r != null)
			intersect = r.getOrigin().subtract(p).length();

		if (r == null || intersect <= Intersection.e || (maxDistance >= 0.0 && intersect > maxDistance)) {
			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
				Settings.rayEngineOut.print(" False }");
			}

			return new RGB(1.0, 1.0, 1.0);
		} else {
			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
				Settings.rayEngineOut.print(" True }");
			}

			return new RGB(0.0, 0.0, 0.0);
		}
	}

	@Override
	public void compact() {

	}
}