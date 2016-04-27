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

package com.almostrealism.raytracer.engine;

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.ColorProducer;
import net.sf.j3d.util.graphics.RGB;

/**
 * The Texture interface is implemented by classes that can be used to texture a surface.
 * 
 * @author Mike Murray
 */
public interface Texture extends ColorProducer {
	/**
	 * Returns the color of the texture represented by this Texture object at the specified point as an RGB object
	 * using the arguments stored by this Texture object.
	 */
	public RGB getColorAt(Vector point);
	
	/**
	 * Returns the color of the texture represented by this Texture object at the specified point as an RGB object
	 * using the specified arguments.
	 */
	public RGB getColorAt(Vector point, Object args[]);
}
