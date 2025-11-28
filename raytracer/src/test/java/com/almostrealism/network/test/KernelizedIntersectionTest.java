/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.network.test;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Pair;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import org.junit.Assert;
import org.junit.Test;

public class KernelizedIntersectionTest extends AbstractIntersectionTest {
	public PackedCollection getInput() {
		PackedCollection pixelLocations = Pair.bank(width * height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Pair p = new Pair(pixelLocations.get(j * width + i), 0);
				p.setMem(new double[] { i, j });
			}
		}

		return pixelLocations;
	}

	@Test
	public void intersectionKernel() {
		Producer<PackedCollection> combined = combined();
		Evaluable<PackedCollection> ev = combined.get();

		PackedCollection input = getInput();
		PackedCollection dim = bank(width * height, pair(width, height).get());
		PackedCollection output = new PackedCollection(input.getCount()).traverse(1);

		System.out.println("KernelizedIntersectionTest: Invoking kernel...");
		ev.into(output).evaluate(input, dim);

		System.out.println("KernelizedIntersectionTest: Comparing...");
		for (int i = 0; i < output.getCount(); i++) {
			double value = ev.evaluate(input.get(i), dim.get(i)).toDouble();
			Assert.assertEquals(value, output.toDouble(i), Math.pow(10, -10));
		}
	}

	@Deprecated
	protected static PackedCollection bank(int count, Evaluable<PackedCollection> source) {
		PackedCollection bank = Pair.bank(count);
		for (int i = 0; i < bank.getCount(); i++) {
			bank.set(i, source.evaluate());
		}

		return bank;
	}
}
