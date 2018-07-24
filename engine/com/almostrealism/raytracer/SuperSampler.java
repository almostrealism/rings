/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.raytracer;

import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Triple;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.util.Producer;

public class SuperSampler implements ColorProducer {
	private Producer<RGB> samples[][];
	private double scale;

	public SuperSampler(Producer<RGB> samples[][]) {
		this.samples = samples;
		scale = 1.0 / (this.samples.length * this.samples[0].length);
	}

	@Override
	public RGB evaluate(Object[] args) {
		Pair pos = (Pair) args[0];

		RGB c = new RGB(0.0, 0.0, 0.0);

		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				double r = pos.getX() + ((double) i / (double) samples.length);
				double q = pos.getY() + ((double) j / (double) samples[i].length);
				c.addTo(samples[i][j].evaluate(new Object[] { new Pair(r, q) }).multiply(scale));
			}
		}

		return c;
	}

	@Override
	public void compact() {
		// TODO  Hardware acceleration
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				samples[i][j].compact();
			}
		}
	}

	@Override
	public RGB operate(Triple triple) {
		// TODO  This should not be a triple function, but that needs to be resolved via the type hierarchy
		return null;
	}

	@Override
	public Scope<? extends Variable> getScope(String s) {
		// TODO
		return null;
	}
}
