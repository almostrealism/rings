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
import org.almostrealism.algebra.PairEvaluable;
import org.almostrealism.algebra.PairProducer;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.ScalarProducerBase;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.computations.RandomPair;
import io.almostrealism.relation.Producer;
import org.almostrealism.geometry.computations.RayFromVectors;

import static org.almostrealism.Ops.*;

public class PinholeCameraRayAt extends RayFromVectors {
	private PinholeCameraRayAt(Vector location, VectorProducer direction) {
		super(ops().v(location), direction);
	}

	public PinholeCameraRayAt(Producer<Pair> pos, Producer<Pair> sd, Vector location, Pair projectionDimensions,
							  double blur, double focalLength, Vector u, Vector v, Vector w) {
		this(location, direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	private static VectorProducer direction(Producer<Pair> pos, Producer<Pair> sd, Pair projectionDimensions, double focalLength,
											Vector u, Vector v, Vector w, Pair blur) {
		PairProducer pd = ops().v(projectionDimensions);

		ScalarProducer sdx = ops().l(sd);
		ScalarProducer sdy = ops().r(sd);

		ScalarProducer p = pd.x().multiply(ops().l(pos))
								.multiply(sdx.add(-1.0).pow(-1.0)).add(pd.x().multiply(-0.5));
		ScalarProducer q = pd.y().multiply(ops().r(pos))
								.multiply(sdy.add(-1.0).pow(-1.0)).add(pd.y().multiply(-0.5));
		ScalarProducer r = ops().scalar(-focalLength);

		ScalarProducer x = p.multiply(u.getX()).add(q.multiply(v.getX())).add(r.multiply(w.getX()));
		ScalarProducer y = p.multiply(u.getY()).add(q.multiply(v.getY())).add(r.multiply(w.getY()));
		ScalarProducer z = p.multiply(u.getZ()).add(q.multiply(v.getZ())).add(r.multiply(w.getZ()));

		VectorProducer pqr = Ops.ops().fromScalars(x, y, z);
		ScalarProducerBase len = pqr.length();
		
		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			VectorProducer wv = pqr.normalize();
			VectorProducer uv = u(wv, t(pqr));
			VectorProducer vv = v(wv, uv);

			PairEvaluable random = new RandomPair();
			PairEvaluable frandom = ops().fromScalars(random.x().add(-0.5), random.y().add(-0.5));

			pqr = pqr.add(uv.scalarMultiply(blur.getX()).scalarMultiply(() -> frandom.x()));
			pqr = pqr.add(vv.scalarMultiply(blur.getY()).scalarMultiply(() -> frandom.y()));

			pqr = pqr.scalarMultiply(len).scalarMultiply(pqr.length().pow(-1.0));
		}

		return pqr;
	}

	private static Producer<Vector> t(VectorProducer pqr) {
		Producer<Vector> ft = pqr.y().lessThanv(pqr.x()).and(pqr.y().lessThanv(pqr.z()),
				ops().fromScalars(pqr.x(), ops().scalar(1.0), pqr.z()),
				ops().fromScalars(pqr.x(), pqr.y(), ops().scalar(1.0)));

		Producer<Vector> t = pqr.x().lessThanv(pqr.y()).and(pqr.y().lessThanv(pqr.z()),
				ops().fromScalars(ops().scalar(1.0), pqr.y(), pqr.z()), ft);

		return t;
	}

	private static VectorProducer u(VectorProducer w, Producer<Vector> t) {
		ScalarProducer x = ops().y(t).multiply(w.z()).add(ops().z(t).multiply(w.y()).multiply(-1.0));
		ScalarProducer y = ops().z(t).multiply(w.x()).add(ops().x(t).multiply(w.z()).multiply(-1.0));
		ScalarProducer z = ops().x(t).multiply(w.y()).add(ops().y(t).multiply(w.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z).normalize();
	}

	private static VectorProducer v(VectorProducer w, VectorProducer u) {
		ScalarProducer x = w.y().multiply(u.z()).add(w.z().multiply(u.y()).multiply(-1.0));
		ScalarProducer y = w.z().multiply(u.x()).add(w.x().multiply(u.z()).multiply(-1.0));
		ScalarProducer z = w.x().multiply(u.y()).add(w.y().multiply(u.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z);
	}
}
