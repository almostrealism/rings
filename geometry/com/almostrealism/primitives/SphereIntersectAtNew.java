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

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairProducer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.computations.DirectionDotDirection;
import org.almostrealism.algebra.computations.OriginDotDirection;
import org.almostrealism.algebra.computations.OriginDotOrigin;
import org.almostrealism.algebra.computations.PairFromScalars;
import org.almostrealism.geometry.Ray;
import org.almostrealism.math.bool.LessThan;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class SphereIntersectAtNew extends LessThan<Scalar> {
	private SphereIntersectAtNew(Producer<Ray> r, OriginDotDirection oDotD,
								OriginDotOrigin oDotO, DirectionDotDirection dDotD) {
		super(2, Scalar.blank(),
				discriminant(oDotD, oDotO, dDotD),
				StaticProducer.of(new Scalar(0.0)),
				StaticProducer.of(new Scalar(-1.0)),
				new SphereIntersectAt(r, t(oDotD, oDotO, dDotD)));
	}

	public SphereIntersectAtNew(Producer<Ray> r) {
		this(r, new OriginDotDirection(r), new OriginDotOrigin(r), new DirectionDotDirection(r));
	}

	private static PairProducer t(OriginDotDirection oDotD,
								  OriginDotOrigin oDotO,
								  DirectionDotDirection dDotD) {
		ScalarProducer dS = discriminantSqrt(oDotD, oDotO, dDotD);
		ScalarProducer minusODotD = oDotD.multiply(-1.0);
		ScalarProducer dDotDInv = dDotD.pow(-1.0);
		return new PairFromScalars(minusODotD.add(dS).multiply(dDotDInv),
								minusODotD.add(dS.multiply(-1.0)).multiply(dDotDInv));
	}

	private static ScalarProducer discriminant(OriginDotDirection oDotD,
											  OriginDotOrigin oDotO,
											  DirectionDotDirection dDotD) {
		return oDotD.pow(2.0).add(dDotD.multiply(oDotO.add(-1.0)).multiply(-1));
	}

	private static ScalarProducer discriminantSqrt(OriginDotDirection oDotD,
												  OriginDotOrigin oDotO,
												  DirectionDotDirection dDotD) {
		return discriminant(oDotD, oDotO, dDotD).pow(0.5);
	}
}
