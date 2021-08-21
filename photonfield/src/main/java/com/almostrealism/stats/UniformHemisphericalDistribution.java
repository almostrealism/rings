/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.stats;

import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.UniformSphericalRandom;
import org.almostrealism.space.Length;
import org.almostrealism.stats.SphericalProbabilityDistribution;
import org.almostrealism.util.CodeFeatures;
import io.almostrealism.relation.Evaluable;

public class UniformHemisphericalDistribution implements SphericalProbabilityDistribution, Length, CodeFeatures {
	private double m = 1.0;

	@Override
	public Evaluable<Vector> getSample(double in[], double orient[]) {
		Vector r = UniformSphericalRandom.getInstance().evaluate(new Object[0]);
		if (new Vector(orient).dotProduct(r) < 0) r.multiplyBy(-1.0);
		if (m != 1.0) r.multiplyBy(m);
		return v(r).get();
	}

	@Override
	public double getMultiplier() { return this.m; }

	@Override
	public void setMultiplier(double m) { this.m = m; }

	@Override
	public String toString() { return "Hemispherical Distribution"; }
}
