/*
 * Copyright 2021 Michael Murray
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

import com.almostrealism.primitives.SphereIntersectAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import org.almostrealism.hardware.MemoryBank;
import org.junit.Assert;
import org.junit.Test;

public class KernelizedIntersectionTest extends AbstractIntersectionTest {
	public PairBank getInput() {
		PairBank pixelLocations = new PairBank(width * height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Pair p = pixelLocations.get(j * width + i);
				p.setMem(new double[] { i, j });
			}
		}

		return pixelLocations;
	}

	@Test
	public void kernel() {
		SphereIntersectAt combined = combined();
		AcceleratedComputationEvaluable<Scalar> ev = (AcceleratedComputationEvaluable<Scalar>) combined.get();

		PairBank input = getInput();
		PairBank dim = PairBank.fromProducer(pair(width, height).get(), width * height);
		ScalarBank output = new ScalarBank(input.getCount());

		System.out.println(ev.getFunctionDefinition());

		System.out.println("KernelizedIntersectionTest: Invoking kernel...");
		ev.kernelEvaluate(output, new MemoryBank[] { input, dim });

		System.out.println("KernelizedIntersectionTest: Comparing...");
		for (int i = 0; i < output.getCount(); i++) {
			Scalar value = ev.evaluate(input.get(i), dim.get(i));
			Assert.assertEquals(value.getValue(), output.get(i).getValue(), Math.pow(10, -10));
		}
	}
}
