/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.distribution;

import com.almostrealism.photonfield.util.VectorMath;

public class RefractiveProbabilityDistribution implements SphericalProbabilityDistribution {
	private double rIndex = 1.0, n2 = 1.0, m = 1.0;
	
	public double[] getSample(double[] in, double[] orient) {
		double alpha = Math.sqrt(1 - (1.0  / (this.n2)) *
								(1 - Math.pow(VectorMath.dot(in, orient), 2)));
		double c[] = VectorMath.cross(orient, VectorMath.cross(orient, in));
		c = VectorMath.multiply(c, Math.sqrt(1 - ((alpha * alpha))));
		orient = VectorMath.multiply(orient, alpha, true);
		double r[] = VectorMath.multiply(VectorMath.add(orient, c), -1);
		VectorMath.normalize(r);
		if (this.m != 1.0) VectorMath.multiply(r, this.m);
		return r;
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
