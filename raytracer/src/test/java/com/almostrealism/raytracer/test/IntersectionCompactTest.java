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

package com.almostrealism.raytracer.test;

import com.almostrealism.primitives.SphereIntersectAt;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import org.junit.Test;

public class IntersectionCompactTest extends AbstractIntersectionTest {
	@Test
	public void compact() {
		SphereIntersectAt combined = combined();
		AcceleratedComputationEvaluable<Scalar> ev = (AcceleratedComputationEvaluable<Scalar>) combined.get();
		ev.compile();

		Scalar r = ev.evaluate(new Pair(50, 50));
		System.out.println(r.getX());
		assert r.getX() > 0;

		r = ev.evaluate(new Pair(0, 0));
		System.out.println(r.getX());
		assert r.getX() < 0;
	}
}