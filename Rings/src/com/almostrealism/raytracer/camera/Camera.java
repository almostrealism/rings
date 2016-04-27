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

package com.almostrealism.raytracer.camera;

import com.almostrealism.raytracer.engine.Ray;

/**
 * Implementations of the Camera interface provide a method for calculating viewing rays.
 * 
 * @author Mike Murray
 */
public interface Camera {
	/**
	 * @param i  X coordinate of pixel.
	 * @param j  Y coordinate of pixel.
	 * @param screenWidth  Width of image.
	 * @param screenHeight  Height of image.
	 * @return  A Ray object that represents the viewing ray at (i, j)
	 */
	public Ray rayAt(double i, double j, int screenWidth, int screenHeight);
}
