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
import io.almostrealism.uml.Nameable;
import org.almostrealism.CodeFeatures;
import org.almostrealism.algebra.Vector;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.space.Length;
import org.almostrealism.stats.SphericalProbabilityDistribution;

import java.lang.reflect.Method;

/**
 * An OverlayBRDF simply takes the sum of the samples provided by each
 * child BRDF (stored as a SphericalProbabilityDistribution[]). The result
 * will be normalized by default; however, this can be configured using the
 * setNormalizeResult method.
 *
 * @author  Michael Murray
 */
public class OverlayBRDF implements SphericalProbabilityDistribution, Nameable, Length, CodeFeatures {
	private final SphericalProbabilityDistribution[] children;
	private double m = 1.0;
	private boolean norm = true;
	public String name;

	public OverlayBRDF(SphericalProbabilityDistribution[] children) {
		this.children = children;
	}

	public double getMultiplier() { return this.m; }

	public void setMultiplier(double m) { this.m = m; }

	public void setNormalizeResult(boolean norm) { this.norm = norm; }
	public boolean getNormalizeResult() { return this.norm; }

	@Override
	public Producer<PackedCollection> getSample(double[] in, double[] orient) {
		Vector result = new Vector();

		for (int i = 0; i < this.children.length; i++)
			result.addTo(new Vector(this.children[i].getSample(in, orient).get().evaluate(), 0));

		if (this.norm) {
			result.normalize();
		}

		if (this.m != 1.0) result.multiplyBy(this.m);

		return v(result);
	}
	
	public static Method getOverlayMethod() {
		try {
			return OverlayBRDF.class.getMethod("createOverlayBRDF",
					SphericalProbabilityDistribution[].class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static OverlayBRDF createOverlayBRDF(SphericalProbabilityDistribution[] children) {
		return new OverlayBRDF(children);
	}

	@Override
	public void setName(String n) { this.name = n; }

	@Override
	public String getName() { return name; }
}
