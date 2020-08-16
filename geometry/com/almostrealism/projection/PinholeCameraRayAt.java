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
import org.almostrealism.algebra.computations.PairFromScalars;
import org.almostrealism.algebra.computations.ScalarFromPair;
import org.almostrealism.algebra.computations.VectorFromScalars;
import org.almostrealism.geometry.RandomPair;
import org.almostrealism.geometry.RayFromVectors;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class PinholeCameraRayAt extends RayFromVectors {
	private PinholeCameraRayAt(Vector location, VectorProducer direction) {
		super(StaticProducer.of(location), direction);
	}

	public PinholeCameraRayAt(Producer<Pair> pos, Producer<Pair> sd, Vector location, Pair projectionDimensions,
							  double blur, double focalLength, Vector u, Vector v, Vector w) {
		this(location, direction(pos, sd, projectionDimensions, focalLength, u, v, w, new Pair(blur, blur)));
	}

	private static VectorProducer direction(Producer<Pair> pos, Producer<Pair> sd, Pair projectionDimensions, double focalLength,
											Vector u, Vector v, Vector w, Pair blur) {
		PairProducer pd = StaticProducer.of(projectionDimensions);

		ScalarProducer sdx = new ScalarFromPair(sd, ScalarFromPair.X);
		ScalarProducer sdy = new ScalarFromPair(sd, ScalarFromPair.Y);

		ScalarProducer p = pd.x().multiply(new ScalarFromPair(pos, ScalarFromPair.X))
								.multiply(sdx.add(-1.0).pow(-1.0)).add(pd.x().multiply(-0.5));
		ScalarProducer q = pd.y().multiply(new ScalarFromPair(pos, ScalarFromPair.Y))
								.multiply(sdy.add(-1.0).pow(-1.0)).add(pd.y().multiply(-0.5));
		ScalarProducer r = StaticProducer.of(new Scalar(-focalLength));

		ScalarProducer x = p.multiply(u.getX()).add(q.multiply(v.getX())).add(r.multiply(w.getX()));
		ScalarProducer y = p.multiply(u.getY()).add(q.multiply(v.getY())).add(r.multiply(w.getY()));
		ScalarProducer z = p.multiply(u.getZ()).add(q.multiply(v.getZ())).add(r.multiply(w.getZ()));

		VectorProducer pqr = new VectorFromScalars(x, y, z);
		ScalarProducer len = pqr.length();
		
		if (blur.getX() != 0.0 || blur.getY() != 0.0) {
			VectorProducer wv = pqr.normalize();
			VectorProducer uv = u(wv, t(pqr));
			VectorProducer vv = v(wv, uv);

			PairProducer random = new RandomPair();
			random = new PairFromScalars(random.x().add(-0.5), random.y().add(-0.5));

			pqr = pqr.add(uv.scalarMultiply(blur.getX()).scalarMultiply(random.x()));
			pqr = pqr.add(vv.scalarMultiply(blur.getY()).scalarMultiply(random.y()));

			pqr = pqr.scalarMultiply(len).scalarMultiply(pqr.length().pow(-1.0));
		}

		return pqr;
	}

	private static VectorProducer t(VectorProducer pqr) {
		VectorProducer t = pqr.y().lessThan(pqr.x()).and(pqr.y().lessThan(pqr.z()),
				new VectorFromScalars(pqr.x(), StaticProducer.of(new Scalar(1.0)), pqr.z()),
				new VectorFromScalars(pqr.x(), pqr.y(), StaticProducer.of(new Scalar(1.0))));

		t = pqr.x().lessThan(pqr.y()).and(pqr.y().lessThan(pqr.z()),
				new VectorFromScalars(StaticProducer.of(new Scalar(1.0)), pqr.y(), pqr.z()), t);

		return t;
	}

	private static VectorProducer u(VectorProducer w, VectorProducer t) {
		ScalarProducer x = t.y().multiply(w.z()).add(t.z().multiply(w.y()).multiply(-1.0));
		ScalarProducer y = t.z().multiply(w.x()).add(t.x().multiply(w.z()).multiply(-1.0));
		ScalarProducer z = t.x().multiply(w.y()).add(t.y().multiply(w.x()).multiply(-1.0));
		return new VectorFromScalars(x, y, z).normalize();
	}

	private static VectorProducer v(VectorProducer w, VectorProducer u) {
		ScalarProducer x = w.y().multiply(u.z()).add(w.z().multiply(u.y()).multiply(-1.0));
		ScalarProducer y = w.z().multiply(u.x()).add(w.x().multiply(u.z()).multiply(-1.0));
		ScalarProducer z = w.x().multiply(u.y()).add(w.y().multiply(u.x()).multiply(-1.0));
		return new VectorFromScalars(x, y, z);
	}
}
