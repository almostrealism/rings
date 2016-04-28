/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.texture;


public class CosineIntensityMap implements IntensityMap {
	private double alpha, beta, tau;
	private IntensityMap map;
	
	public CosineIntensityMap() {
		this(null);
	}
	
	public CosineIntensityMap(IntensityMap map) {
		this(3.5, 2.5, 2.0, map);
	}
	
	public CosineIntensityMap(double alpha, double beta, double tau, IntensityMap map) {
		this.alpha = alpha;
		this.beta = beta;
		this.tau = tau;
		this.map = map;
	}
	
	public void setIntensityMap(IntensityMap map) { this.map = map; }
	public IntensityMap getIntensityMap() { return this.map; }
	
	public double getIntensity(double u, double v, double w) {
		double z = this.map.getIntensity(this.tau * u, this.tau * v, this.tau * w);
		double t = 1 + Math.cos(Math.min(this.alpha * v + this.beta * z, 2.0 * Math.PI));
		return t / 2.0;
	}
}
