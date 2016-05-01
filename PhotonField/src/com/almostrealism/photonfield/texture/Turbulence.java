/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.texture;


public class Turbulence implements IntensityMap {
	private Noise noise;
	private int itr = 8;

	public Turbulence() { this(new Noise(), 8); }
	
	public Turbulence(Noise noise, int itr) { this.noise = noise; this.itr = itr; }
	
	public double getIntensity(double u, double v, double w) {
		double n = 0.0;
		
		for (int i = 0; i < this.itr; i++) {
			double m = Math.pow(2.0, i);
			n = n + this.noise.getIntensity(m * u, m * v, m * w) / m;
		}
		
		return n;
	}
}
