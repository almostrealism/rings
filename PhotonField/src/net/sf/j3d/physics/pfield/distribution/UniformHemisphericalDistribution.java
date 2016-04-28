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
