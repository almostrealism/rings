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

import org.almostrealism.util.Vector;
import org.almostrealism.util.graphics.RGB;

/**
  A DirectionAmbientLight object represents an ambient light source that always comes from a particular direction.
  The direction is a vector that represents the direction from which the light enters the scene.
  By default the light comes from the top, parallel to the yz plane.
*/

public class DirectionalAmbientLight extends AmbientLight {
  private Vector direction;

	/**
	  Constructs a DirectionalAmbientLight object with the default direction, intensity, and color.
	*/
	
	public DirectionalAmbientLight() {
		super(1.0, new RGB(1.0, 1.0, 1.0));
		this.setDirection(new Vector(0.0, -1.0, 0.0));
	}
	
	/**
	  Constructs a DirectionalAmbientLight object with default intensity and color and the direction represented by the specified Vector object.
	*/
	
	public DirectionalAmbientLight(Vector direction) {
		super(1.0, new RGB(1.0, 1.0, 1.0));
		this.setDirection(direction);
	}
	
	/**
	  Constructs a DirectionalAmbientLight object with the direction, intensity, and color represented by the specified values.
	*/
	
	public DirectionalAmbientLight(double intensity, RGB color, Vector direction) {
		super(intensity, color);
		this.setDirection(direction);
	}
	
	/**
	  Sets the direction of this DirectionalAmbientLight object to the direction represented by the specified Vector object.
	*/
	
	public void setDirection(Vector direction) {
		this.direction = direction;
	}
	
	/**
	  Returns the direction of this DirectionalAmbientLight object as a Vector object.
	*/
	
	public Vector getDirection() {
		return this.direction;
	}
	
	/**
	  Returns "Directional Ambient Light".
	*/
	
	public String toString() {
		return "Directional Ambient Light";
	}
}
