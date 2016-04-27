/*
 * Copyright (C) 2004-05  Mike Murray
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

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

/**
 * An AmbientLight object represents a light that is applied to all objects in the scene.
 * The color and intensity of the light may by specified, but by default it is white light.
 * 
 * @author Mike Murray
 */
public class AmbientLight implements Light {
  private double intensity;
  private RGB color;

	/**
	 * Constructs an AmbientLight object with the default intensity and color.
	 */
	public AmbientLight() {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs an AmbientLight object with the specified intensity and default color.
	 */
	public AmbientLight(double intensity) {
		this.setIntensity(intensity);
		this.setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs an AmbientLight object with the specified intensity and color.
	 */
	public AmbientLight(double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
	}
	
	/**
	 * Sets the intensity of this AmbientLight object.
	 */
	public void setIntensity(double intensity) { this.intensity = intensity; }
	
	/**
	 * Sets the color of this AmbientLight object to the color represented by the specified RGB object.
	 */
	public void setColor(RGB color) { this.color = color; }
	
	/**
	 * Returns the intensity of this AmbientLight object as a double value.
	 */
	public double getIntensity() { return this.intensity; }
	
	/**
	 * Returns the color of this AmbientLight object as an RGB object.
	 */
	public RGB getColor() { return this.color; }
	
	/**
	 * Returns the color of this AmbientLight object as an RGB object.
	 */
	public RGB getColorAt(Vector point) { return this.color.multiply(this.intensity); }
	
	/**
	 * Returns "Ambient Light".
	 */
	public String toString() { return "Ambient Light"; }
}
