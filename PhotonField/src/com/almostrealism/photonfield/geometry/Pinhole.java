/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.geometry;

import com.almostrealism.photonfield.Absorber;
import com.almostrealism.photonfield.Clock;
import com.almostrealism.photonfield.util.Fast;
import com.almostrealism.photonfield.util.VectorMath;

/**
 * A Pinhole is similar to an AbsorptionPlane except a hole with a specified radius
 * is present in the middle of the plane through which photons may pass without being
 * absorbed. It is useful to place a Pinhole in front of an AbsorptionPlane to create
 * a simple perpective camera.
 * 
 * @author  Mike Murray
 */
public class Pinhole extends Plane implements Absorber, Fast {
	public static double verbose = Math.pow(10.0, -7.0);
	
	private double radius;
	private Clock clock;
	
	/**
	 * @param r  The radius of the pinhole (usually measured in micrometers).
	 */
	public void setRadius(double r) { this.radius = r; }
	
	/**
	 * Returns the radius of the pinhole (usually measured in micrometers).
	 */
	public double getRadius() { return this.radius; }
	
	public boolean absorb(double[] x, double[] p, double energy) {
		double d = Math.abs(VectorMath.dot(x, this.normal));
		if (d > this.thick) return false;
		
		double y = Math.abs(VectorMath.dot(x, this.up));
		
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		double z = Math.abs(VectorMath.dot(x, this.across));
		
		if (Math.sqrt(y * y + z * z) > this.radius)
			return true;
		else
			return false;
	}
	
	public void setAbsorbDelay(double t) { }
	public void setOrigPosition(double[] x) { }
	
	public double[] emit() { return null; }
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
	public double getEmitEnergy() { return 0.0; }
	public double[] getEmitPosition() { return null; }
	public double getNextEmit() { return Integer.MAX_VALUE; }
}
