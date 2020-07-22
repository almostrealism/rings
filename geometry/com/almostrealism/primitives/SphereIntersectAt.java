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
import org.almostrealism.algebra.computations.ScalarFromPair;
import org.almostrealism.geometry.Ray;
import org.almostrealism.math.AcceleratedProducer;
import org.almostrealism.math.DynamicAcceleratedProducerAdapter;
import org.almostrealism.math.bool.AcceleratedConjunctionAdapter;
import org.almostrealism.math.bool.GreaterThan;
import org.almostrealism.math.bool.LessThan;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class SphereIntersectAt extends AcceleratedConjunctionAdapter<Scalar> {
	protected SphereIntersectAt(Producer<Ray> r, PairProducer t) {
		super(2, Scalar.blank(),
				new LessThan(2, Scalar.blank(), t.x(), t.y(), t.x(), t.y()),
				new GreaterThan(2, Scalar.blank(), t.x(),
						StaticProducer.of(new Scalar(0.0)),
						t.x(), new GreaterThan(2, Scalar.blank(), t.y(),
									StaticProducer.of(new Scalar(0.0)), t.y(),
									StaticProducer.of(new Scalar(-1.0)))),
				new GreaterThan(2, new ScalarFromPair(t, ScalarFromPair.X),
								StaticProducer.of(new Scalar(0))),
				new GreaterThan(2, new ScalarFromPair(t, ScalarFromPair.Y),
								StaticProducer.of(new Scalar(0))));
	}
}
