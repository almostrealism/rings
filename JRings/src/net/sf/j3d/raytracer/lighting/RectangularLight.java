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

package net.sf.j3d.raytracer.lighting;

import net.sf.j3d.raytracer.primitives.Plane;
import net.sf.j3d.raytracer.shaders.Shader;
import net.sf.j3d.util.TransformMatrix;
import net.sf.j3d.util.Vector;

/**
 * A RectangularLight object provides PointLight samples that are randomly distributed
 * across the a plane surface. The location of the plane is used for the upper left corner
 * of the area to sample.
 * 
 * @author Mike Murray
 */
public class RectangularLight extends Plane implements SurfaceLight {
  private double width, height;
  private double intensity;
  
  private int samples;
	
  	/**
  	 * Constructs a new RectangularLight object.
  	 */
	public RectangularLight() {
		this.samples = 1;
		
		super.setShaders(new Shader[0]);
	}
	
	/**
	 * Constructs a new RectangularLight object.
	 * 
	 * @param width  Width of rectangle.
	 * @param height  Hieght of rectangle.
	 */
	public RectangularLight(double width, double height) {
		this.width = width;
		this.height = height;
		
		this.samples = 1;
		
		super.setShaders(new Shader[0]);
	}
	
	/**
	 * Sets the number of samples to use for this RectangularLight object.
	 * 
	 * @param samples
	 */
	public void setSampleCount(int samples) { this.samples = samples; }
	
	/**
	 * @return  The number of samples to use for this RectangularLight object.
	 */
	public int getSampleCount() { return this.samples; }
	
	/**
	 * @see net.sf.j3d.raytracer.lighting.SurfaceLight#getSamples(int)
	 */
	public Light[] getSamples(int total) {
		Light l[] = new Light[total];
		
		double in = this.intensity / total;
		
		for (int i = 0; i < total; i++) {
			double x = 0.0, y = 0.0, z = 0.0;
			
			if (super.getType() == Plane.XY) {
				x = Math.random() * this.width;
				y = Math.random() * this.height;
				z = 0.0;
			} else if (super.getType() == Plane.XZ) {
				x = Math.random() * this.width;
				y = 0.0;
				z = Math.random() * this.height;
			} else if (super.getType() == Plane.YZ) {
				x = 0.0;
				y = Math.random() * this.width;
				z = Math.random() * this.height;
			}
			
			Vector p = new Vector(x, y, z);
			super.getTransform(true).transform(p, TransformMatrix.TRANSFORM_AS_LOCATION);
			
			l[i] = new PointLight(p, in, super.getColorAt(p));
		}
		
		return l;
	}
	
	/**
	 * @see net.sf.j3d.raytracer.lighting.SurfaceLight#getSamples()
	 */
	public Light[] getSamples() { return this.getSamples(this.samples); }
	
	/**
	 * Sets the width of the rectangular area of this RectangularLight object.
	 */
	public void setWidth(double width) { this.width = width; }
	
	/**
	 * Sets the height of the rectangular area of this RectangularLight object.
	 */
	public void setHeight(double height) { this.height = height; }
	
	/**
	 * @return  The width of the rectangular area of this RectangularLight object.
	 */
	public double getWidth() { return this.width; }
	
	/**
	 * @return  The width of the rectangular area of this RectangularLight object.
	 */
	public double getHeight() { return this.height; }
	
	/**
	 * @see net.sf.j3d.raytracer.lighting.Light#setIntensity(double)
	 */
	public void setIntensity(double intensity) { this.intensity = intensity; }
	
	/**
	 * @see net.sf.j3d.raytracer.lighting.Light#getIntensity()
	 */
	public double getIntensity() { return this.intensity; }
	
	/**
	 * @return  "Rectangular Light".
	 */
	public String toString() { return "Rectangular Light"; }
}
