/*
 * Copyright 2021 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.optimize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.almostrealism.organs.Organ;

public class AverageHealthComputationSet<T> extends HashSet<HealthComputation<T>> implements HealthComputation<T> {
	private final List<BiConsumer<HealthComputation<T>, Organ<T>>> listeners;

	public AverageHealthComputationSet() {
		listeners = new ArrayList<>();
	}

	public void addListener(BiConsumer<HealthComputation<T>, Organ<T>> listener) {
		listeners.add(listener);
	}

	@Override
	public double computeHealth(Organ<T> organ) {
		double total = 0;

		for (HealthComputation<T> hc : this) {
			listeners.forEach(l -> l.accept(hc, organ));
			total += hc.computeHealth(organ);
		}
		
		return total / size();
	}
}
