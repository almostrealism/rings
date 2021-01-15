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

package com.almostrealism.raytracer;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBBank;
import org.almostrealism.graph.PathElement;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;

import java.util.ArrayList;

// TODO Convert to subclass of ColorSum
public class SuperSampler implements Producer<RGB>, PathElement<RGB, RGB> {
	protected Producer<RGB> samples[][];
	private double scale;

	public SuperSampler(Producer<RGB> samples[][]) {
		this.samples = samples;
		scale = 1.0 / (this.samples.length * this.samples[0].length);
	}

	@Override
	public Evaluable<RGB> get() {
		return new KernelizedEvaluable<RGB>() {

			@Override
			public MemoryBank<RGB> createKernelDestination(int size) {
				return new RGBBank(size);
			}

			@Override
			public RGB evaluate(Object... args) {
				Pair pos = (Pair) args[0];

				RGB c = new RGB(0.0, 0.0, 0.0);

				for (int i = 0; i < samples.length; i++) {
					j:
					for (int j = 0; j < samples[i].length; j++) {
						double r = pos.getX() + ((double) i / (double) samples.length);
						double q = pos.getY() + ((double) j / (double) samples[i].length);

						RGB rgb = samples[i][j].get().evaluate(new Object[]{new Pair(r, q)});
						if (rgb == null) continue j;

						rgb.multiplyBy(scale);
						c.addTo(rgb);
					}
				}

				return c;
			}

			@Override
			public void kernelEvaluate(MemoryBank destination, MemoryBank[] args) {
				int w = samples.length;
				int h = samples[0].length;

				PairBank allSamples = new PairBank(args[0].getCount());
				RGBBank out[][] = new RGBBank[w][h];

				System.out.println("SuperSampler: Evaluating sample kernels...");
				for (int i = 0; i < samples.length; i++) {
					j: for (int j = 0; j < samples[i].length; j++) {
						for (int k = 0; k < args[0].getCount(); k++) {
							Pair pos = (Pair) args[0].get(k);
							double r = pos.getX() + ((double) i / (double) samples.length);
							double q = pos.getY() + ((double) j / (double) samples[i].length);
							allSamples.set(k, r, q);
						}

						out[i][j] = new RGBBank(args[0].getCount());
						((KernelizedEvaluable) samples[i][j].get()).kernelEvaluate(out[i][j], new MemoryBank[] { allSamples } );
					}
				}

				System.out.println("SuperSampler: Combining samples...");
				for (int k = 0; k < destination.getCount(); k++) {
					for (int i = 0; i < samples.length; i++) {
						j: for (int j = 0; j < samples[i].length; j++) {
							((RGB) destination.get(k)).addTo(out[i][j].get(k).multiply(scale));
						}
					}
				}
			}
		};
	}

	@Override
	public void compact() {
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
