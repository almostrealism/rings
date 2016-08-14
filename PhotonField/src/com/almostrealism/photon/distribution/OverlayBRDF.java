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

import java.lang.reflect.Method;

import org.almostrealism.util.Nameable;

import com.almostrealism.photon.util.Length;
import com.almostrealism.photon.util.VectorMath;

/**
 * An OverlayBRDF simply takes the sum of the samples provided by each
 * child BRDF (stored as a SphericalProbabilityDistribution[]). The result
 * will be normalized by default; however, this can be configured using the
 * setNormalizeResult method.
 * 
 * @author  Mike Murray
 */
public class OverlayBRDF implements SphericalProbabilityDistribution, Nameable, Length {
	private SphericalProbabilityDistribution children[];
	private double m = 1.0;
	private boolean norm = true;
	public String name;
	
	public OverlayBRDF(SphericalProbabilityDistribution children[]) {
		this.children = children;
	}
	
	public double getMultiplier() { return this.m; }
	public void setMultiplier(double m) { this.m = m; }
	public void setNormalizeResult(boolean norm) { this.norm = norm; }
	public boolean getNormalizeResult() { return this.norm; }
	
	public double[] getSample(double in[], double orient[]) {
		double result[] = new double[3];
		
		for (int i = 0; i < this.children.length; i++)
			VectorMath.addTo(result, this.children[i].getSample(in, orient));
		
		if (this.norm) VectorMath.normalize(result);
		if (this.m != 1.0) VectorMath.multiply(result, this.m);
		
		return result;
	}
	
	public static Method getOverlayMethod() {
		try {
			return OverlayBRDF.class.getMethod("createOverlayBRDF",
					new Class[] {SphericalProbabilityDistribution[].class});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static OverlayBRDF createOverlayBRDF(SphericalProbabilityDistribution children[]) {
		return new OverlayBRDF(children);
	}
	
	public void setName(String n) { this.name = n; }
	public String getName() { return name; }
}
