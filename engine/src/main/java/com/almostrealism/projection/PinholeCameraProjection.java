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
import org.almostrealism.algebra.PairProducer;
import org.almostrealism.algebra.PairProducerBase;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducerBase;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducerBase;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.geometry.computations.RayExpressionComputation;

public class PinholeCameraProjection implements CodeFeatures {
	public static RayExpressionComputation rayAt(Producer<Pair<?>> pos, Producer<Pair<?>> sd, Vector location, Pair projectionDimensions,
												 double blur, double focalLength, Vector u, Vector v, Vector w) {
		return Ops.ops().ray(Ops.ops().v(location),
				direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	private static VectorProducerBase direction(Producer<Pair<?>> pos, Producer<Pair<?>> sd, Pair projectionDimensions, double focalLength,
											Vector u, Vector v, Vector w, Pair blur) {
		PairProducerBase pd = Ops.ops().v(projectionDimensions);

		ScalarProducerBase sdx = Ops.ops().l(sd);
		ScalarProducerBase sdy = Ops.ops().r(sd);

		ScalarProducerBase p = pd.x().multiply(Ops.ops().l(pos))
								.multiply(sdx.add(-1.0).pow(-1.0)).add(pd.x().multiply(-0.5));
		ScalarProducerBase q = pd.y().multiply(Ops.ops().r(pos))
								.multiply(sdy.add(-1.0).pow(-1.0)).add(pd.y().multiply(-0.5));
		ScalarProducerBase r = Ops.ops().scalar(-focalLength);

		ScalarProducerBase x = p.multiply(u.getX()).add(q.multiply(v.getX())).add(r.multiply(w.getX()));
		ScalarProducerBase y = p.multiply(u.getY()).add(q.multiply(v.getY())).add(r.multiply(w.getY()));
		ScalarProducerBase z = p.multiply(u.getZ()).add(q.multiply(v.getZ())).add(r.multiply(w.getZ()));

		VectorProducerBase pqr = Ops.ops().vector(x, y, z);
		ScalarProducerBase len = pqr.length();
		
		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			VectorProducerBase wv = pqr.normalize();
			VectorProducerBase uv = u(wv, t(pqr));
			VectorProducerBase vv = v(wv, uv);

			Producer<PackedCollection<?>> random = Ops.ops().rand(2);
			Producer<Scalar> rx = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 0)), 0));
			Producer<Scalar> ry = Ops.op(o -> o.scalar(o.shape(1), o.add(o.c(-0.5), o.c(random, 1)), 0));

			pqr = pqr.add(uv.scalarMultiply(blur.getX()).scalarMultiply(rx));
			pqr = pqr.add(vv.scalarMultiply(blur.getY()).scalarMultiply(ry));

			pqr = pqr.scalarMultiply(len).scalarMultiply(pqr.length().pow(-1.0));
		}

		return pqr;
	}

	private static Producer<Vector> t(VectorProducerBase pqr) {
		Producer<Vector> ft = pqr.y().lessThanv(pqr.x()).and(pqr.y().lessThanv(pqr.z()),
				Ops.ops().vector(pqr.x(), Ops.ops().scalar(1.0), pqr.z()),
				Ops.ops().vector(pqr.x(), pqr.y(), Ops.ops().scalar(1.0)));

		Producer<Vector> t = pqr.x().lessThanv(pqr.y()).and(pqr.y().lessThanv(pqr.z()),
				Ops.ops().vector(Ops.ops().scalar(1.0), pqr.y(), pqr.z()), ft);

		return t;
	}

	private static VectorProducerBase u(VectorProducerBase w, Producer<Vector> t) {
		ScalarProducerBase x = Ops.ops().y(t).multiply(w.z()).add(Ops.ops().z(t).multiply(w.y()).multiply(-1.0));
		ScalarProducerBase y = Ops.ops().z(t).multiply(w.x()).add(Ops.ops().x(t).multiply(w.z()).multiply(-1.0));
		ScalarProducerBase z = Ops.ops().x(t).multiply(w.y()).add(Ops.ops().y(t).multiply(w.x()).multiply(-1.0));
		return Ops.ops().vector(x, y, z).normalize();
	}

	private static VectorProducerBase v(VectorProducerBase w, VectorProducerBase u) {
		ScalarProducerBase x = w.y().multiply(u.z()).add(w.z().multiply(u.y()).multiply(-1.0));
		ScalarProducerBase y = w.z().multiply(u.x()).add(w.x().multiply(u.z()).multiply(-1.0));
		ScalarProducerBase z = w.x().multiply(u.y()).add(w.y().multiply(u.x()).multiply(-1.0));
		return Ops.ops().vector(x, y, z);
	}
}
