/*
 * Copyright 2020 Michael Murray
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

import org.almostrealism.algebra.ScalarSupplier;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorSupplier;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.RayBank;
import org.almostrealism.geometry.RayEvaluable;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.relation.Maker;
import org.almostrealism.util.Evaluable;

import static org.almostrealism.util.Ops.*;

public class ReflectedRay implements KernelizedEvaluable<Ray>, RayEvaluable {
	private Evaluable<Vector> point;
	private Evaluable<Vector> normal;
	private Evaluable<Vector> reflected;
	private double blur;

	public ReflectedRay(Evaluable<Vector> point, Evaluable<Vector> incident, Evaluable<Vector> normal, double blur) {
		this.point = point;
		this.normal = normal;
		this.reflected = reflect(() -> incident, () -> normal).get();
		this.blur = blur;
	}

	@Override
	public Ray evaluate(Object[] args) {
		Vector n = normal.evaluate(args);
		Vector ref = reflected.evaluate(args);

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
		this.normal.compact();
		this.reflected.compact();
	}

	@Override
	public MemoryBank<Ray> createKernelDestination(int size) { return new RayBank(size); }

	/**
	 * Reflects the specified {@link Vector} across the normal vector represented by the
	 * second specified {@link Vector} and returns the result.
	 */
	public static VectorSupplier reflect(Maker<Vector> vector, Maker<Vector> normal) {
		VectorSupplier newVector = ops().minus(vector);
		ScalarSupplier s = ops().scalar(2).multiply(newVector.dotProduct(normal).divide(ops().lengthSq(normal)));
		return newVector.subtract(ops().scalarMultiply(normal, s));
	}
}
