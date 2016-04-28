/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.distribution;

import net.sf.j3d.physics.pfield.util.Length;
import net.sf.j3d.physics.pfield.util.VectorMath;

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
