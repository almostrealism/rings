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

package com.almostrealism.projection;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairProducer;
import org.almostrealism.algebra.PairSupplier;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.ScalarSupplier;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.VectorSupplier;
import org.almostrealism.geometry.RandomPair;
import org.almostrealism.geometry.RayFromVectors;
import org.almostrealism.relation.Maker;
import org.almostrealism.util.Producer;

import static org.almostrealism.util.Ops.*;

public class PinholeCameraRayAt extends RayFromVectors {
	private PinholeCameraRayAt(Vector location, VectorSupplier direction) {
		super(ops().v(location), direction);
	}

	public PinholeCameraRayAt(Producer<Pair> pos, Producer<Pair> sd, Vector location, Pair projectionDimensions,
							  double blur, double focalLength, Vector u, Vector v, Vector w) {
		this(location, direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	private static VectorSupplier direction(Producer<Pair> pos, Producer<Pair> sd, Pair projectionDimensions, double focalLength,
											Vector u, Vector v, Vector w, Pair blur) {
		PairSupplier pd = ops().v(projectionDimensions);

		ScalarSupplier sdx = ops().l(() -> sd);
		ScalarSupplier sdy = ops().r(() -> sd);

		ScalarSupplier p = pd.x().multiply(ops().l(pos))
								.multiply(sdx.add(-1.0).pow(-1.0)).add(pd.x().multiply(-0.5));
		ScalarSupplier q = pd.y().multiply(ops().r(pos))
								.multiply(sdy.add(-1.0).pow(-1.0)).add(pd.y().multiply(-0.5));
		ScalarSupplier r = ops().scalar(-focalLength);

		ScalarSupplier x = p.multiply(u.getX()).add(q.multiply(v.getX())).add(r.multiply(w.getX()));
		ScalarSupplier y = p.multiply(u.getY()).add(q.multiply(v.getY())).add(r.multiply(w.getY()));
		ScalarSupplier z = p.multiply(u.getZ()).add(q.multiply(v.getZ())).add(r.multiply(w.getZ()));

		VectorSupplier pqr = ops().fromScalars(x, y, z);
		ScalarSupplier len = pqr.length();
		
		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			VectorSupplier wv = pqr.normalize();
			VectorSupplier uv = u(wv, t(pqr));
			VectorSupplier vv = v(wv, uv);

			PairProducer random = new RandomPair();
			PairProducer frandom = ops().fromScalars(random.x().add(-0.5), random.y().add(-0.5));

			pqr = pqr.add(uv.scalarMultiply(blur.getX()).scalarMultiply(() -> frandom.x()));
			pqr = pqr.add(vv.scalarMultiply(blur.getY()).scalarMultiply(() -> frandom.y()));

			pqr = pqr.scalarMultiply(len).scalarMultiply(pqr.length().pow(-1.0));
		}

		return pqr;
	}

	private static Maker<Vector> t(VectorSupplier pqr) {
		Maker<Vector> ft = () -> pqr.y().lessThan(pqr.x()).and(pqr.y().lessThan(pqr.z()),
				ops().fromScalars(pqr.x(), ops().scalar(1.0), pqr.z()),
				ops().fromScalars(pqr.x(), pqr.y(), ops().scalar(1.0)));

		Maker<Vector> t = () -> pqr.x().lessThan(pqr.y()).and(pqr.y().lessThan(pqr.z()),
				ops().fromScalars(ops().scalar(1.0), pqr.y(), pqr.z()), ft);

		return t;
	}

	private static VectorSupplier u(VectorSupplier w, Maker<Vector> t) {
		ScalarSupplier x = ops().y(t).multiply(w.z()).add(ops().z(t).multiply(w.y()).multiply(-1.0));
		ScalarSupplier y = ops().z(t).multiply(w.x()).add(ops().x(t).multiply(w.z()).multiply(-1.0));
		ScalarSupplier z = ops().x(t).multiply(w.y()).add(ops().y(t).multiply(w.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z).normalize();
	}

	private static VectorSupplier v(VectorSupplier w, VectorSupplier u) {
		ScalarSupplier x = w.y().multiply(u.z()).add(w.z().multiply(u.y()).multiply(-1.0));
		ScalarSupplier y = w.z().multiply(u.x()).add(w.x().multiply(u.z()).multiply(-1.0));
		ScalarSupplier z = w.x().multiply(u.y()).add(w.y().multiply(u.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z);
	}
}
