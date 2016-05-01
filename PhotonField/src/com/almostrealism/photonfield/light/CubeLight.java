/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.light;

import com.almostrealism.photonfield.texture.IntensityMap;

public class CubeLight extends LightBulb {
	private IntensityMap map;
	private double width, height, depth;
	
	private double pos[];
	
	public CubeLight() {
		this(null);
	}
	
	public CubeLight(IntensityMap map) {
		this.map = map;
		this.width = 1.0;
		this.height = 1.0;
		this.depth = 1.0;
	}
	
	public void setEmitPositionMap(IntensityMap map) { this.map = map; }
	public IntensityMap getEmitPositionMap() { return this.map; }
	
	public void setWidth(double w) { this.width = w; }
	public void setHeight(double h) { this.height = h; }
	public void setDepth(double d) { this.depth = d; }
	public double getWidth() { return this.width; }
	public double getHeight() { return this.height; }
	public double getDepth() { return this.depth; }
	
	public double getEmitEnergy() {
		// TODO use another intensity map + spectra
		return super.getEmitEnergy();
	}
	
	public double[] getEmitPosition() {
		if (this.pos != null) return this.pos;
		
		double x = 0.0, y = 0.0, z = 0.0;
		double r = 1.0;
		double p = 0.0;
		
		while (r >= p) {
			x = Math.random();
			y = Math.random();
			z = Math.random();
			r = Math.random();
			p = this.map.getIntensity(x, y, z);
		}
		
		this.pos = new double[] {this.width * (x - 0.5),
								this.height * (y - 0.5),
								this.depth * (z - 0.5)};
		
		return this.pos;
	}
}
