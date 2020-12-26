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
import org.almostrealism.bool.AcceleratedConjunctionScalar;
import org.almostrealism.bool.GreaterThanScalar;
import org.almostrealism.geometry.computations.DirectionDotDirection;
import org.almostrealism.geometry.computations.OriginDotDirection;
import org.almostrealism.geometry.computations.OriginDotOrigin;
import org.almostrealism.geometry.Ray;
import org.almostrealism.bool.GreaterThan;
import org.almostrealism.bool.LessThanScalar;
import io.almostrealism.relation.Evaluable;

import java.util.function.Supplier;

import static org.almostrealism.util.Ops.*;

public class SphereIntersectAt extends LessThanScalar {
	private SphereIntersectAt(Supplier<Evaluable<? extends Ray>> r, ScalarProducer oDotD,
							  ScalarProducer oDotO, ScalarProducer dDotD) {
		super(discriminant(oDotD, oDotO, dDotD),
				ops().scalar(0.0),
				ops().scalar(-1.0),
				() -> closest(t(oDotD, oDotO, dDotD)), false);
	}

	public SphereIntersectAt(Supplier<Evaluable<? extends Ray>> r) {
		this(r, new OriginDotDirection(r), new OriginDotOrigin(r), new DirectionDotDirection(r));
	}

	private static AcceleratedConjunctionScalar closest(PairProducer t) {
		return new AcceleratedConjunctionScalar(
				() -> new LessThanScalar(t.x(), t.y(), t.x(), t.y(), false),
				() -> new GreaterThanScalar(t.x(),
						ops().scalar(0.0),
						t.x(), () -> new GreaterThan(2, () -> Scalar.blank(), t.y(),
						ops().scalar(0.0), t.y(),
						ops().scalar(-1.0), false), false),
				new GreaterThanScalar(ops().l(t),
						ops().scalar(0)),
				new GreaterThanScalar(ops().r(t),
						ops().scalar(0)));
	}

	private static PairProducer t(ScalarProducer oDotD,
								  ScalarProducer oDotO,
								  ScalarProducer dDotD) {
		ScalarProducer dS = discriminantSqrt(oDotD, oDotO, dDotD);
		ScalarProducer minusODotD = oDotD.multiply(-1.0);
		ScalarProducer dDotDInv = dDotD.pow(-1.0);
		return ops().fromScalars(minusODotD.add(dS).multiply(dDotDInv),
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
