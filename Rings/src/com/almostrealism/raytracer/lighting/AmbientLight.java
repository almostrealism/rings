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

package com.almostrealism.raytracer.lighting;

import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;

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
