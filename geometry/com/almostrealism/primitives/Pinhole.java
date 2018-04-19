/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.primitives;

import org.almostrealism.algebra.Vector;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.Fast;
import org.almostrealism.time.Clock;

/**
 * A Pinhole is similar to an AbsorptionPlane except a hole with a specified radius
 * is present in the middle of the plane through which photons may pass without being
 * absorbed. It is useful to place a Pinhole in front of an AbsorptionPlane to create
 * a simple perpective camera.
 * 
 * @author  Michael Murray
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
		double d = Math.abs(new Vector(x).dotProduct(new Vector(this.normal)));
		if (d > this.thick) return false;
		
		double y = Math.abs(new Vector(x).dotProduct(new Vector(this.up)));
		
		if (this.across == null)
			this.across = new Vector(this.up).crossProduct(new Vector(this.normal)).toArray();
		
		double z = Math.abs(new Vector(x).dotProduct(new Vector(this.across)));
		
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
