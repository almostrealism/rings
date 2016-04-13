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
