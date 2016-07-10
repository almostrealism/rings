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

/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.distribution;

import com.almostrealism.photonfield.util.Length;
import com.almostrealism.photonfield.util.VectorMath;

public class UniformHemisphericalDistribution implements SphericalProbabilityDistribution, Length {
	private double m = 1.0;
	
	public double[] getSample(double in[], double orient[]) {
		double r[] = VectorMath.uniformSphericalRandom();
		if (VectorMath.dot(orient, r) < 0) VectorMath.multiply(r, -1.0);
		if (m != 1.0) VectorMath.multiply(r, m);
		return r;
	}
	
	public double getMultiplier() { return this.m; }
	public void setMultiplier(double m) { this.m = m; }
	public String toString() { return "Hemispherical Distribution"; }
}
