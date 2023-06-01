/*
 * Copyright 2023 Michael Murray
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
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.geometry.computations.RayExpressionComputation;

public interface ProjectionFeatures extends CodeFeatures {
	default RayExpressionComputation rayAt(Producer<Pair<?>> pos, Producer<Pair<?>> sd, Vector location, Pair projectionDimensions,
												 double blur, double focalLength, Vector u, Vector v, Vector w) {
		return ray(v(location),
				direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	default ExpressionComputation<Vector> direction(Producer<Pair<?>> pos, Producer<Pair<?>> sd, Pair projectionDimensions, double focalLength,
													Vector u, Vector v, Vector w, Pair blur) {
		Producer<Pair<?>> pd = v(projectionDimensions);

		ExpressionComputation<Scalar> sdx = l(sd);
		ExpressionComputation<Scalar> sdy = r(sd);

		ExpressionComputation<Scalar> pdx = l(pd);
		ExpressionComputation<Scalar> pdy = r(pd);

		ExpressionComputation<Scalar> p = pdx.multiply(l(pos))
								.multiply(sdx.add(v(-1.0)).pow(v(-1.0))).add(pdx.multiply(v(-0.5)));
		ExpressionComputation<Scalar> q = pdy.multiply(r(pos))
								.multiply(sdy.add(v(-1.0)).pow(-1.0)).add(pdy.multiply(v(-0.5)));
		ExpressionComputation<Scalar> r = scalar(-focalLength);

		ExpressionComputation<Scalar> x = p.multiply(v(u.getX())).add(q.multiply(v(v.getX()))).add(r.multiply(v(w.getX())));
		ExpressionComputation<Scalar> y = p.multiply(v(u.getY())).add(q.multiply(v(v.getY()))).add(r.multiply(v(w.getY())));
		ExpressionComputation<Scalar> z = p.multiply(v(u.getZ())).add(q.multiply(v(v.getZ()))).add(r.multiply(v(w.getZ())));

		ExpressionComputation<Vector> pqr = vector(x, y, z);
		ExpressionComputation<Scalar> len = length(pqr);

		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			ExpressionComputation<Vector> wv = normalize(pqr);
			ExpressionComputation<Vector> uv = u(wv, t(pqr));
			ExpressionComputation<Vector> vv = v(wv, uv);

			Producer<PackedCollection<?>> random = rand(2);
			Producer<Scalar> rx = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 0)), 0));
			Producer<Scalar> ry = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 1)), 0));

			pqr = pqr.add(scalarMultiply(scalarMultiply(uv, blur.getX()), rx));
			pqr = pqr.add(scalarMultiply(scalarMultiply(vv, blur.getY()), ry));

			pqr = scalarMultiply(pqr, len);
			pqr = scalarMultiply(pqr, length(pqr).pow(-1.0));
		}

		return pqr;
	}

	private Producer<Vector> t(ExpressionComputation<Vector> pqr) {
		Producer<Vector> ft = lessThanv(y(pqr), x(pqr)).and(lessThanv(y(pqr), z(pqr)),
				vector(x(pqr), scalar(1.0), z(pqr)),
				vector(x(pqr), y(pqr), scalar(1.0)));

		Producer<Vector> t = lessThanv(x(pqr), y(pqr)).and(lessThanv(y(pqr), z(pqr)),
				vector(scalar(1.0), y(pqr), z(pqr)), ft);

		return t;
	}

	private ExpressionComputation<Vector> u(ExpressionComputation<Vector> w, Producer<Vector> t) {
		ExpressionComputation<Scalar>  x = y(t).multiply(z(w)).add(z(t).multiply(y(w)).multiply(v(-1.0)));
		ExpressionComputation<Scalar> y = z(t).multiply(x(w)).add(x(t).multiply(z(w)).multiply(v(-1.0)));
		ExpressionComputation<Scalar>  z = x(t).multiply(y(w)).add(y(t).multiply(x(w)).multiply(v(-1.0)));
		return normalize(vector(x, y, z));
	}

	private ExpressionComputation<Vector> v(ExpressionComputation<Vector> w, ExpressionComputation<Vector> u) {
		ExpressionComputation<Scalar>  x = y(w).multiply(z(u)).add(z(w).multiply(y(u)).multiply(v(-1.0)));
		ExpressionComputation<Scalar>  y = z(w).multiply(x(u)).add(x(w).multiply(z(u)).multiply(v(-1.0)));
		ExpressionComputation<Scalar>  z = x(w).multiply(y(u)).add(y(w).multiply(x(u)).multiply(v(-1.0)));
		return vector(x, y, z);
	}
}
