/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.lighting;

import com.almostrealism.raytracer.primitives.Sphere;
import com.almostrealism.util.TransformMatrix;
import com.almostrealism.util.Vector;

/**
 * A SphericalLight object provides PointLight samples that are randomly distributed
 * across the surface of a sphere.
 * 
 * @author Mike Murray
 */
public class SphericalLight extends Sphere implements SurfaceLight {
  private double intensity, atta, attb, attc;
  
  private int samples;

	/**
	 * Constructs a new SphericalLight object.
	 */
	public SphericalLight() {
		super(new Vector(0.0, 0.0, 0.0), 0.0);
		
		this.intensity = 1.0;
		this.samples = 1;
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	 * Constructs a new SphericalLight object.
	 * 
	 * @param location  Location for sphere.
	 * @param radius  Radius of sphere.
	 */
	public SphericalLight(Vector location, double radius) {
		super(location, radius);
		
		this.intensity = 1.0;
		this.samples = 1;
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	 * Sets the number of samples to use for this SphericalLight object.
	 * 
	 * @param samples
	 */
	public void setSampleCount(int samples) { this.samples = samples; }
	
	/**
	 * @return  The number of samples to use for this SphericalLight object.
	 */
	public int getSampleCount() { return this.samples; }
	
	/**
	 * @see com.almostrealism.raytracer.lighting.SurfaceLight#getSamples(int)
	 */
	public Light[] getSamples(int total) {
		PointLight l[] = new PointLight[total];
		
		double in = this.intensity / total;
		
		for (int i = 0; i < total; i++) {
			double r = super.getSize();
			double u = Math.random() * 2.0 * Math.PI;
			double v = Math.random() * 2.0 * Math.PI;
			
			double x = r * Math.sin(u) * Math.cos(v);
			double y = r * Math.sin(u) * Math.sin(v);
			double z = r * Math.cos(u);
			
			Vector p = new Vector(x, y, z);
			
			super.getTransform(true).transform(p, TransformMatrix.TRANSFORM_AS_LOCATION);
			
			l[i] = new PointLight(p, in, super.getColorAt(p));
			l[i].setAttenuationCoefficients(this.atta, this.attb, this.attc);
		}
		
		return l;
	}
	
	/**
	 * @see com.almostrealism.raytracer.lighting.SurfaceLight#getSamples()
	 */
	public Light[] getSamples() { return this.getSamples(this.samples); }

	/**
	 * @see com.almostrealism.raytracer.lighting.Light#setIntensity(double)
	 */
	public void setIntensity(double intensity) { this.intensity = intensity; }

	/**
	 * @see com.almostrealism.raytracer.lighting.Light#getIntensity()
	 */
	public double getIntensity() { return this.intensity; }
	
	/**
	 * Sets the attenuation coefficients to be used when light samples are created.
	 */
	public void setAttenuationCoefficients(double a, double b, double c) {
		this.atta = a;
		this.attb = b;
		this.attc = c;
	}
	
	/**
	 * @return  An array containing the attenuation coefficients used when light samples are created.
	 */
	public double[] getAttenuationCoefficients() { return new double[] { this.atta, this.attb, this.attc }; }
	
	/**
	 * @see com.almostrealism.raytracer.engine.ParticleGroup#getParticleVertices()
	 */
	public double[][] getParticleVertices() { return new double[0][0]; }
	
	/**
	 * @return  "Spherical Light".
	 */
	public String toString() { return "Spherical Light"; }
}
