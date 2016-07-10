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
