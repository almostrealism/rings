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

package com.almostrealism.primitives;

import org.almostrealism.algebra.PairProducer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.computations.DefaultScalarProducer;
import org.almostrealism.algebra.computations.DirectionDotDirection;
import org.almostrealism.algebra.computations.OriginDotDirection;
import org.almostrealism.algebra.computations.OriginDotOrigin;
import org.almostrealism.geometry.Ray;
import org.almostrealism.math.bool.AcceleratedConjunctionAdapter;
import org.almostrealism.math.bool.GreaterThan;
import org.almostrealism.math.bool.LessThanScalar;
import org.almostrealism.util.Producer;
import org.almostrealism.util.Provider;
import static org.almostrealism.util.Ops.*;

public class SphereIntersectAt extends LessThanScalar {
	private SphereIntersectAt(Producer<Ray> r, ScalarProducer oDotD,
							  ScalarProducer oDotO, ScalarProducer dDotD) {
		super(discriminant(oDotD, oDotO, dDotD),
				ops().scalar(0.0),
				ops().scalar(-1.0),
				closest(t(oDotD, oDotO, dDotD)), false);
	}

	public SphereIntersectAt(Producer<Ray> r) {
		this(r, new DefaultScalarProducer(new OriginDotDirection(r)),
				new DefaultScalarProducer(new OriginDotOrigin(r)),
				new DefaultScalarProducer(new DirectionDotDirection(r)));
	}

	private static AcceleratedConjunctionAdapter<Scalar> closest(PairProducer t) {
		return new AcceleratedConjunctionAdapter<>(2, Scalar.blank(),
				new LessThanScalar(t.x(), t.y(), t.x(), t.y(), false),
				new GreaterThan(2, Scalar.blank(), t.x(),
						ops().scalar(0.0),
						t.x(), new GreaterThan(2, Scalar.blank(), t.y(),
						ops().scalar(0.0), t.y(),
						ops().scalar(-1.0), false), false),
				new GreaterThan(2, PairProducer.x(t),
						ops().scalar(0)),
				new GreaterThan(2, PairProducer.y(t),
						ops().scalar(0)));
	}

	private static PairProducer t(ScalarProducer oDotD,
								  ScalarProducer oDotO,
								  ScalarProducer dDotD) {
		ScalarProducer dS = discriminantSqrt(oDotD, oDotO, dDotD);
		ScalarProducer minusODotD = oDotD.multiply(-1.0);
		ScalarProducer dDotDInv = dDotD.pow(-1.0);
		return PairProducer.fromScalars(minusODotD.add(dS).multiply(dDotDInv),
								minusODotD.add(dS.multiply(-1.0)).multiply(dDotDInv));
	}

	private static ScalarProducer discriminant(ScalarProducer oDotD,
											   ScalarProducer oDotO,
											   ScalarProducer dDotD) {
		return oDotD.pow(2.0).add(dDotD.multiply(oDotO.add(-1.0)).multiply(-1));
	}

	private static ScalarProducer discriminantSqrt(ScalarProducer oDotD,
												   ScalarProducer oDotO,
												   ScalarProducer dDotD) {
		return discriminant(oDotD, oDotO, dDotD).pow(0.5);
	}
}
