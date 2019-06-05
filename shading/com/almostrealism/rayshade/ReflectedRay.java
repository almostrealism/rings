/*
 * Copyright 2019 Michael Murray
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

package com.almostrealism.rayshade;

import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.Ray;
import org.almostrealism.util.Producer;

public class ReflectedRay implements Producer<Ray> {
	private Producer<Vector> point;
	private Producer<Vector> incident;
	private Producer<Vector> normal;
	private double blur;

	public ReflectedRay(Producer<Vector> point, Producer<Vector> incident, Producer<Vector> normal, double blur) {
		this.point = point;
		this.incident = incident;
		this.normal = normal;
		this.blur = blur;
	}

	@Override
	public Ray evaluate(Object[] args) {
		Vector in = incident.evaluate(args);
		Vector n = normal.evaluate(args);

		Vector ref = reflect(in, n);

		if (blur != 0.0) {
			double a = blur * (-0.5 + Math.random());
			double b = blur * (-0.5 + Math.random());

			Vector u, v, w = (Vector) n.clone();

			Vector t = (Vector) n.clone();

			if (t.getX() < t.getY() && t.getY() < t.getZ()) {
				t.setX(1.0);
			} else if (t.getY() < t.getX() && t.getY() < t.getZ()) {
				t.setY(1.0);
			} else {
				t.setZ(1.0);
			}

			w.divideBy(w.length());

			u = t.crossProduct(w);
			u.divideBy(u.length());

			v = w.crossProduct(u);

			ref.addTo(u.multiply(a));
			ref.addTo(v.multiply(b));
			ref.divideBy(ref.length());
		}

		return new Ray(point.evaluate(args), ref);
	}

	@Override
	public void compact() {
		this.incident.compact();
		this.normal.compact();
	}

	/**
	 * Reflects the specified {@link Vector} across the normal vector represented by the
	 * second specified {@link Vector} and returns the result.
	 */
	public static Vector reflect(Vector vector, Vector normal) { // TODO  Should return Producer
		vector = vector.minus();
		return vector.subtract(normal.multiply(2 * (vector.dotProduct(normal) / normal.lengthSq())));
	}
}
