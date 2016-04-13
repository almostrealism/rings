/*
 * Copyright (C) 2004  Mike Murray
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

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

/**
 * A Light implmentation provides lighting information used for rendering.
 * The intensity and color of the Light may by specified.
 * 
 * @author Mike Murray.
 */
public interface Light {
	public boolean castShadows = true;
	
	/**
	 * Sets the intensity of this Light object.
	 */
	public void setIntensity(double intensity);
	
	/**
	 * Sets the color of this Light object to the color represented by the specified RGB object.
	 */
	public void setColor(RGB color);
	
	/**
	 * Returns the intensity of this Light object as a double value.
	 */
	public double getIntensity();
	
	/**
	 * Returns the color of this Light object as an RGB object.
	 */
	public RGB getColor();
	
	/**
	 * Returns the color of the light represented by this Light object at the specified point
	 * as an RGB object.
	 */
	public RGB getColorAt(Vector point);
}
