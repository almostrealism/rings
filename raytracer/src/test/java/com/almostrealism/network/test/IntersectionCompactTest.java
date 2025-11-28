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
import org.junit.Test;

public class IntersectionCompactTest extends AbstractIntersectionTest {
	@Test
	public void compact() {
		Producer<PackedCollection> combined = combined();
		Evaluable<PackedCollection> ev = combined.get();

		Pair r = new Pair(ev.evaluate(new Pair(50, 50)));
		System.out.println(r.getX());
		assert r.getX() > 0;

		r = new Pair(ev.evaluate(new Pair(0, 0)));
		System.out.println(r.getX());
		assert r.getX() < 0;
	}
}
