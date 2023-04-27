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
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGB;
import org.almostrealism.graph.PathElement;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.KernelizedProducer;
import org.almostrealism.hardware.MemoryBank;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.MemoryData;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class SuperSampler implements Producer<RGB>, PathElement<RGB, RGB> {
	protected KernelizedProducer<RGB> samples[][];
	private double scale;

	public SuperSampler(KernelizedProducer<RGB> samples[][]) {
		this.samples = samples;
		scale = 1.0 / (this.samples.length * this.samples[0].length);
	}

	@Override
	public Evaluable<RGB> get() {
		KernelizedEvaluable<RGB> ev[][] = new KernelizedEvaluable[samples.length][samples[0].length];
		IntStream.range(0, samples.length).forEach(i ->
			IntStream.range(0, samples[i].length).forEach(j -> {
				ev[i][j] = samples[i][j].get();
			}));

		return new KernelizedEvaluable<>() {

			@Override
			public MemoryBank<RGB> createKernelDestination(int size) {
				return RGB.bank(size);
			}

			@Override
			public RGB evaluate(Object... args) {
				Pair pos = (Pair) args[0];

				RGB c = new RGB(0.0, 0.0, 0.0);

				for (int i = 0; i < ev.length; i++) {
					j:
					for (int j = 0; j < ev[i].length; j++) {
						double r = pos.getX() + ((double) i / (double) ev.length);
						double q = pos.getY() + ((double) j / (double) ev[i].length);

						RGB rgb = ev[i][j].evaluate(new Pair(r, q));
						if (rgb == null) continue j;

						rgb.multiplyBy(scale);
						c.addTo(rgb);
					}
				}

				return c;
			}

			@Override
			public Evaluable withDestination(MemoryBank<RGB> destination) {
				return args -> {
					int w = ev.length;
					int h = ev[0].length;

					PackedCollection<Pair<?>> allSamples = Pair.bank(((MemoryBank) args[0]).getCount());
					PackedCollection<RGB> out[][] = new PackedCollection[w][h];

					System.out.println("SuperSampler: Evaluating sample kernels...");
					for (int i = 0; i < ev.length; i++) {
						j: for (int j = 0; j < ev[i].length; j++) {
							for (int k = 0; k < ((MemoryBank) args[0]).getCount(); k++) {
								Pair pos = (Pair) ((MemoryBank) args[0]).get(k);
								double r = pos.getX() + ((double) i / (double) ev.length);
								double q = pos.getY() + ((double) j / (double) ev[i].length);
								allSamples.set(k, r, q);
							}

							out[i][j] = RGB.bank(((MemoryBank) args[0]).getCount());
							ev[i][j].into(out[i][j]).evaluate(allSamples);
						}
					}

					System.out.println("SuperSampler: Combining samples...");
					for (int k = 0; k < destination.getCount(); k++) {
						for (int i = 0; i < ev.length; i++) {
							j: for (int j = 0; j < ev[i].length; j++) {
								((RGB) destination.get(k)).addTo(out[i][j].get(k).multiply(scale));
							}
						}
					}

					return destination;
				};
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
