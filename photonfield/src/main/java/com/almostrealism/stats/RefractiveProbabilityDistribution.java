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

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorMath;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.stats.SphericalProbabilityDistribution;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Evaluable;

public class RefractiveProbabilityDistribution implements SphericalProbabilityDistribution, CodeFeatures {
	private double rIndex = 1.0, n2 = 1.0, m = 1.0;

	@Override
	public Producer<PackedCollection> getSample(double[] in, double[] orient) {
		double alpha = Math.sqrt(1 - (1.0  / (this.n2)) *
								(1 - Math.pow(new Vector(in).dotProduct(new Vector(orient)), 2)));
		double c[] = new Vector(orient).crossProduct(new Vector(orient).crossProduct(new Vector(in))).toArray();
		c = VectorMath.multiply(c, Math.sqrt(1 - ((alpha * alpha))));
		orient = VectorMath.multiply(orient, alpha, true);
		double r[] = VectorMath.multiply(VectorMath.add(orient, c), -1);
		Vector rr = new Vector(r);
		rr.normalize();
		r = rr.toArray();
		if (this.m != 1.0) VectorMath.multiply(r, this.m);
		return (Producer) vector(r[0], r[1], r[2]);
	}
	
	public double getMultiplier() { return this.m; }
	public void setMultiplier(double m) { this.m = m; }
	
	public void setRefractiveIndex(double n) {
		this.rIndex = n;
		this.n2 = this.rIndex * this.rIndex;
	}
	
	public double getRefractiveIndex() { return this.rIndex; }
	
	public String toString() { return "Refractive Distribution"; }
}
