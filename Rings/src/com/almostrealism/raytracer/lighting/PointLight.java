/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package com.almostrealism.raytracer.lighting;

import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;

/**
  An PointLight object represents a light which has its source at a point in the scene.
  The color and intensity of the light may by specified, but by default it is white light.
  Also, coefficients for distance attenuation may be specified also,
  but by default are 0.0, 0.0, and 1.0 (no attenuation).
*/

public class PointLight implements Light {
  private double intensity;
  private RGB color;
  
  private Vector location;
  
  private double da, db, dc;

	/**
	  Constructs a PointLight object with the default intensity and color at the origin.
	*/
	
	public PointLight() {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	  Constructs a PointLight object with the specified location and default intensity and color.
	*/
	
	public PointLight(Vector location) {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	  Constructs a PointLight object with the specified intensity and default color at the origin.
	*/
	
	public PointLight(double intensity) {
		this.setIntensity(intensity);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	  Constructs a PointLight object with the specified intensity and color at the origin.
	*/
	
	public PointLight(double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	  Constructs a PointLight object with the specified location, intensity, and color.
	*/
	
	public PointLight(Vector location, double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
		
		this.setLocation(location);
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	  Sets the intensity of this PointLight object to the specified double value.
	*/
	
	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	/**
	  Sets the color of this PointLight object to the color represented by the specified RGB object.
	*/
	
	public void setColor(RGB color) {
		this.color = color;
	}
	
	/**
	  Sets the location of this PointLight object to the location represented by the specified Vector object.
	*/
	
	public void setLocation(Vector location) {
		this.location = location;
	}
	
	/**
	  Sets the coefficients a, b, and c for the quadratic function used for distance attenuation
	  of the light represented by this PointLight object to the specified double values.
	*/
	
	public void setAttenuationCoefficients(double a, double b, double c) {
		this.da = a;
		this.db = b;
		this.dc = c;
	}
	
	/**
	 * Returns the intensity of this PointLight object as a double value.
	 */
	public double getIntensity() { return this.intensity; }
	
	/**
	 * Returns the color of this PointLight object as an RGB object.
	 */
	public RGB getColor() { return this.color; }
	
	/**
	 * Returns the color of the light represented by this PointLight object at the
	 * specified point as an RGB object.
	 */
	public RGB getColorAt(Vector point) {
		double d = point.subtract(this.location).lengthSq();
		
		RGB color = this.getColor().multiply(this.getIntensity());
		color.divideBy(da * d + db * Math.sqrt(d) + dc);
		
		return color;
	}
	
	/**
	 * Returns the location of this PointLight object as a Vector object.
	 */
	public Vector getLocation() { return this.location; }
	
	/**
	 * Returns the coefficients a, b, and c for the quadratic function used for distance
	 * attenuation of the light represented by this PointLight object as an array of
	 * double values.
	 */
	public double[] getAttenuationCoefficients() {
		double d[] = {this.da, this.db, this.dc};
		
		return d;
	}
	
	/**
	 * Returns "Point Light".
	 */
	public String toString() { return "Point Light"; }
}
