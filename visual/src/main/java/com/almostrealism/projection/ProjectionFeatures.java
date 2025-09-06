/*
 * Copyright 2024 Michael Murray
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

package com.almostrealism.projection;

import org.almostrealism.CodeFeatures;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.geometry.Ray;

public interface ProjectionFeatures extends CodeFeatures {
	default CollectionProducer<Ray> rayAt(Producer<Pair<?>> pos, Producer<Pair<?>> sd, Vector location, Pair projectionDimensions,
											 double blur, double focalLength, Vector u, Vector v, Vector w) {
		return ray(v(location),
				direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	default CollectionProducer<Vector> direction(Producer<Pair<?>> pos, Producer<Pair<?>> sd,
												 Pair projectionDimensions, double focalLength,
												 Vector u, Vector v, Vector w, Pair blur) {
		Producer<Pair<?>> pd = v(projectionDimensions);

		ExpressionComputation<Scalar> sdx = l(sd);
		ExpressionComputation<Scalar> sdy = r(sd);

		ExpressionComputation<Scalar> pdx = l(pd);
		ExpressionComputation<Scalar> pdy = r(pd);

		CollectionProducer<Scalar> p = pdx.multiply(l(pos))
								.multiply(sdx.add(scalar(-1.0)).pow(scalar(-1.0))).add(pdx.multiply(scalar(-0.5)));
		CollectionProducer<Scalar> q = pdy.multiply(r(pos))
								.multiply(sdy.add(scalar(-1.0)).pow(-1.0)).add(pdy.multiply(scalar(-0.5)));
		CollectionProducer<Scalar> r = scalar(-focalLength);

		CollectionProducer<Scalar> x = p.multiply(scalar(u.getX())).add(q.multiply(scalar(v.getX()))).add(r.multiply(scalar(w.getX())));
		CollectionProducer<Scalar> y = p.multiply(scalar(u.getY())).add(q.multiply(scalar(v.getY()))).add(r.multiply(scalar(w.getY())));
		CollectionProducer<Scalar> z = p.multiply(scalar(u.getZ())).add(q.multiply(scalar(v.getZ()))).add(r.multiply(scalar(w.getZ())));

		CollectionProducer<Vector> pqr = vector(x, y, z);
		CollectionProducer<Scalar> len = vlength(pqr);

		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			CollectionProducer<Vector> wv = vnormalize(pqr);
			CollectionProducer<Vector> uv = u(wv, t(pqr));
			CollectionProducer<Vector> vv = v(wv, uv);

			Producer<PackedCollection<?>> random = rand(2);
			Producer<Scalar> rx = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 0)), 0));
			Producer<Scalar> ry = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 1)), 0));

			pqr = pqr.add(scalarMultiply(scalarMultiply(uv, blur.getX()), rx));
			pqr = pqr.add(scalarMultiply(scalarMultiply(vv, blur.getY()), ry));

			pqr = scalarMultiply(pqr, len);
			pqr = scalarMultiply(pqr, vlength(pqr).pow(-1.0));
		}

		return pqr;
	}

	private Producer<Vector> t(CollectionProducer<Vector> pqr) {
		Producer<Vector> ft = scalarLessThan(y(pqr), x(pqr)).and(scalarLessThan(y(pqr), z(pqr)),
				vector(x(pqr), scalar(1.0), z(pqr)),
				vector(x(pqr), y(pqr), scalar(1.0)));

		Producer<Vector> t = scalarLessThan(x(pqr), y(pqr)).and(scalarLessThan(y(pqr), z(pqr)),
				vector(scalar(1.0), y(pqr), z(pqr)), ft);

		return t;
	}

	private CollectionProducer<Vector> u(CollectionProducer<Vector> w, Producer<Vector> t) {
		CollectionProducer<Scalar>  x = y(t).multiply(z(w)).add(z(t).multiply(y(w)).multiply(scalar(-1.0)));
		CollectionProducer<Scalar> y = z(t).multiply(x(w)).add(x(t).multiply(z(w)).multiply(scalar(-1.0)));
		CollectionProducer<Scalar>  z = x(t).multiply(y(w)).add(y(t).multiply(x(w)).multiply(scalar(-1.0)));
		return vnormalize(vector(x, y, z));
	}

	private CollectionProducer<Vector> v(CollectionProducer<Vector> w, CollectionProducer<Vector> u) {
		CollectionProducer<Scalar>  x = y(w).multiply(z(u)).add(z(w).multiply(y(u)).multiply(scalar(-1.0)));
		CollectionProducer<Scalar>  y = z(w).multiply(x(u)).add(x(w).multiply(z(u)).multiply(scalar(-1.0)));
		CollectionProducer<Scalar>  z = x(w).multiply(y(u)).add(y(w).multiply(x(u)).multiply(scalar(-1.0)));
		return vector(x, y, z);
	}
}
