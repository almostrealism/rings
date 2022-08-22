/*
 * Copyright 2022 Michael Murray
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

import io.almostrealism.scope.Scope;
import org.almostrealism.Ops;
import org.almostrealism.algebra.ScalarProducerBase;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.VectorProducerBase;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.RayBank;
import org.almostrealism.geometry.RayProducer;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Evaluable;

import static org.almostrealism.Ops.*;

public class ReflectedRay implements RayProducer {
	private Producer<Vector> point;
	private Producer<Vector> normal;
	private Producer<Vector> reflected;
	private double blur;

	public ReflectedRay(Producer<Vector> point, Producer<Vector> incident, Producer<Vector> normal, double blur) {
		this.point = point;
		this.normal = normal;
		this.reflected = reflect(incident, normal);
		this.blur = blur;
	}

	@Override
	public KernelizedEvaluable<Ray> get() {
		Evaluable<Vector> nor = normal.get();
		Evaluable<Vector> refl = reflected.get();

		return new KernelizedEvaluable<Ray>() {
			@Override
			public MemoryBank<Ray> createKernelDestination(int size) { return new RayBank(size); }

			@Override
			public Ray evaluate(Object[] args) {
				Vector n = nor.evaluate(args);
				Vector ref = refl.evaluate(args);

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

				return new Ray(point.get().evaluate(args), ref);
			}
		};
	}

	@Override
	public void compact() {
		this.normal.compact();
		this.reflected.compact();
	}

	@Override
	public Scope<Ray> getScope() {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Reflects the specified {@link Vector} across the normal vector represented by the
	 * second specified {@link Vector} and returns the result.
	 */
	public static VectorProducerBase reflect(Producer<Vector> vector, Producer<Vector> normal) {
		VectorProducerBase newVector = Ops.ops().minus(vector);
		ScalarProducerBase s = Ops.ops().scalar(2).multiply(newVector.dotProduct(normal).divide(Ops.ops().lengthSq(normal)));
		return newVector.subtract(Ops.ops().scalarMultiply(normal, s));
	}
}
