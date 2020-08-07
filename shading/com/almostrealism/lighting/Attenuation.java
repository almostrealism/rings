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

package com.almostrealism.lighting;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.computations.ScalarPow;
import org.almostrealism.algebra.computations.ScalarProduct;
import org.almostrealism.algebra.computations.ScalarSum;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.ColorProduct;
import org.almostrealism.color.computations.RGBProducer;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class Attenuation extends ColorProduct {
	public Attenuation(double da, double db, double dc, RGB color, Producer<Scalar> distanceSq) {
		super(color, RGBProducer.fromScalar(
				new ScalarSum(
						new ScalarProduct(StaticProducer.of(da), distanceSq),
						new ScalarProduct(StaticProducer.of(db),
								new ScalarPow(distanceSq, StaticProducer.of(new Scalar(0.5)))),
						StaticProducer.of(dc))));
	}
}
