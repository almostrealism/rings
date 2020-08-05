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
import org.almostrealism.color.RGBProducer;
import org.almostrealism.util.Producer;

public class Attenuation implements RGBProducer {
	private double da, db, dc;
	private RGB color;
	private Producer<Scalar> distance;

	public Attenuation(double da, double db, double dc, RGB color, Producer<Scalar> distance) {
		this.da = da;
		this.db = db;
		this.dc = dc;
		this.color = color;
		this.distance = distance;
	}

	@Override
	public RGB evaluate(Object[] args) {
		double d = distance.evaluate(args).getValue();
		return color.divide(da * d + db * Math.sqrt(d) + dc);
	}

	@Override
	public void compact() {
		distance.compact();
	}
}
