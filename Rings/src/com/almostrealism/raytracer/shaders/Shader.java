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

package com.almostrealism.raytracer.shaders;

import net.sf.j3d.util.graphics.ColorProducer;
import net.sf.j3d.util.graphics.RGB;

/**
 * The Shader interface is implemented by classes that provide a method for shading a surface.
 */
public interface Shader extends ColorProducer {
	/**
	 * Returns an RGB object that represents the shaded color calculated using the values
	 * of the specified ShaderParameters object.
	 */
	public RGB shade(ShaderParameters parameters);
}
