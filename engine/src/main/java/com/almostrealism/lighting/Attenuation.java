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
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.ColorProduct;
import io.almostrealism.relation.Evaluable;

import java.util.function.Supplier;

import static org.almostrealism.util.Ops.*;

public class Attenuation extends ColorProduct {
	public Attenuation(double da, double db, double dc, Supplier<Evaluable<? extends RGB>> color, Supplier<Evaluable<? extends Scalar>> distanceSq) {
		super(color, ops().cfromScalar(
				ops().v(da).multiply(distanceSq)
						.add(ops().v(db).multiply(ops().pow(distanceSq, ops().scalar(0.5))))
								.add(ops().v(dc))));
	}
}