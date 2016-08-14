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

package com.almostrealism.photon.distribution;

import com.almostrealism.photon.util.Length;
import com.almostrealism.photon.util.VectorMath;

public class ReflectiveProbabilityDistribution implements SphericalProbabilityDistribution, Length {
	private double m = 1.0;
	
	public double[] getSample(double[] in, double[] orient) {
		orient = VectorMath.multiply(orient, VectorMath.dot(in, orient) * 2.0, true);
		double r[] = VectorMath.subtract(orient, in);
		VectorMath.normalize(r);
		if (this.m != 1.0) VectorMath.multiply(r, this.m);
		return r;
	}
	
	public double getMultiplier() { return this.m; }
	public void setMultiplier(double m) { this.m = m; }
	public String toString() { return "Reflective Distribution"; }
}
