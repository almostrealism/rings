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
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.geometry.RandomPair;
import org.almostrealism.geometry.RayFromVectors;
import org.almostrealism.util.Producer;
import org.almostrealism.util.Provider;
import static org.almostrealism.util.Ops.*;

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

		ScalarProducer sdx = PairProducer.x(sd);
		ScalarProducer sdy = PairProducer.y(sd);

		ScalarProducer p = pd.x().multiply(PairProducer.x(pos))
								.multiply(sdx.add(-1.0).pow(-1.0)).add(pd.x().multiply(-0.5));
		ScalarProducer q = pd.y().multiply(PairProducer.y(pos))
								.multiply(sdy.add(-1.0).pow(-1.0)).add(pd.y().multiply(-0.5));
		ScalarProducer r = ops().scalar(-focalLength);

		ScalarProducer x = p.multiply(u.getX()).add(q.multiply(v.getX())).add(r.multiply(w.getX()));
		ScalarProducer y = p.multiply(u.getY()).add(q.multiply(v.getY())).add(r.multiply(w.getY()));
		ScalarProducer z = p.multiply(u.getZ()).add(q.multiply(v.getZ())).add(r.multiply(w.getZ()));

		VectorProducer pqr = ops().fromScalars(x, y, z);
		ScalarProducer len = pqr.length();
		
		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			VectorProducer wv = pqr.normalize();
			VectorProducer uv = u(wv, t(pqr));
			VectorProducer vv = v(wv, uv);

			PairProducer random = new RandomPair();
			random = PairProducer.fromScalars(random.x().add(-0.5), random.y().add(-0.5));

			pqr = pqr.add(uv.scalarMultiply(blur.getX()).scalarMultiply(random.x()));
			pqr = pqr.add(vv.scalarMultiply(blur.getY()).scalarMultiply(random.y()));

			pqr = pqr.scalarMultiply(len).scalarMultiply(pqr.length().pow(-1.0));
		}

		return pqr;
	}

	private static VectorProducer t(VectorProducer pqr) {
		VectorProducer t = pqr.y().lessThan(pqr.x()).and(pqr.y().lessThan(pqr.z()),
				ops().fromScalars(pqr.x(), ops().scalar(1.0), pqr.z()),
				ops().fromScalars(pqr.x(), pqr.y(), ops().scalar(1.0)));

		t = pqr.x().lessThan(pqr.y()).and(pqr.y().lessThan(pqr.z()),
				ops().fromScalars(ops().scalar(1.0), pqr.y(), pqr.z()), t);

		return t;
	}

	private static VectorProducer u(VectorProducer w, VectorProducer t) {
		ScalarProducer x = t.y().multiply(w.z()).add(t.z().multiply(w.y()).multiply(-1.0));
		ScalarProducer y = t.z().multiply(w.x()).add(t.x().multiply(w.z()).multiply(-1.0));
		ScalarProducer z = t.x().multiply(w.y()).add(t.y().multiply(w.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z).normalize();
	}

	private static VectorProducer v(VectorProducer w, VectorProducer u) {
		ScalarProducer x = w.y().multiply(u.z()).add(w.z().multiply(u.y()).multiply(-1.0));
		ScalarProducer y = w.z().multiply(u.x()).add(w.x().multiply(u.z()).multiply(-1.0));
		ScalarProducer z = w.x().multiply(u.y()).add(w.y().multiply(u.x()).multiply(-1.0));
		return ops().fromScalars(x, y, z);
	}
}
