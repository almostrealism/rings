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
import org.almostrealism.util.graphics.RGB;

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
