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

import org.almostrealism.algebra.Pair;
import org.almostrealism.color.RGB;
import org.almostrealism.graph.PathElement;
import org.almostrealism.util.Producer;

import java.util.ArrayList;

public class SuperSampler implements Producer<RGB>, PathElement<RGB, RGB> {
	protected Producer<RGB> samples[][];
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

				RGB rgb = samples[i][j].evaluate(new Object[] { new Pair(r, q) });
				rgb.multiplyBy(scale);
				c.addTo(rgb);
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
	public Iterable<Producer<RGB>> getDependencies() {
		ArrayList<Producer<RGB>> l = new ArrayList<>();

		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				l.add(samples[i][j]);
			}
		}

		return l;
	}
}
