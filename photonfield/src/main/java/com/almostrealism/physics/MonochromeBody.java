/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.physics;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorEvaluable;
import org.almostrealism.algebra.ZeroVector;

/**
 * A {@link MonochromeBody} absorbs all radiation and only emits photons of one energy level.
 * 
 * @author  Michael Murray
 */
public class MonochromeBody extends BlackBody {
	private double chrome;

	@Override
	public Producer<Vector> emit() {
		return null;
	}

	@Override
	public double getEmitEnergy() { return 0; }

	@Override
	public Producer<Vector> getEmitPosition() { return ZeroVector.getInstance(); }

	@Override
	public double getNextEmit() {
		if (super.energy >= this.chrome)
			return 0.0;
		else
			return Integer.MAX_VALUE;
	}
}
