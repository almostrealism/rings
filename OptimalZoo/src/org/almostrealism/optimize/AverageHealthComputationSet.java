/*
 * Copyright 2016 Michael Murray
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

import java.util.HashSet;
import java.util.Iterator;

import org.almostrealism.organs.Organ;

public class AverageHealthComputationSet<T> extends HashSet<HealthComputation<T>> implements HealthComputation<T> {
	public double computeHealth(Organ<T> organ) {
		double total = 0;
		
		Iterator<HealthComputation<T>> itr = iterator();
		
		while (itr.hasNext()) {
			total += itr.next().computeHealth(organ);
		}
		
		return total / size();
	}
}
