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

package com.almostrealism.lighting;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorMultiplier;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;

import com.almostrealism.raytracer.engine.ShadableSurface;

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
	public ColorProducer getColorAt(Vector point) { return this.color.multiply(this.intensity); }
	
	/**
	 * Returns "Ambient Light".
	 */
	public String toString() { return "Ambient Light"; }

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * interesection on that surface using the lighting data from the specified AmbientLight
	 * object and returns an RGB object that represents the color of the point. A list of all
	 * other surfaces in the scene must be specified for reflection/shadowing. This list does
	 * not include the specified surface for which the lighting calculations are to be done.
	 */
	public static ColorProducer ambientLightingCalculation(Vector point, Vector rayDirection, ShadableSurface surface, AmbientLight light) {
		ColorProducer color = new ColorMultiplier(light.getColor(), light.getIntensity());
		color = new ColorMultiplier(color, surface.getColorAt(point));
		
		return color;
	}
}
