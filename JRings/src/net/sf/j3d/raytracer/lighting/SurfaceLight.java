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

package net.sf.j3d.raytracer.lighting;

/**
 * A SurfaceLight implementation represents a light that can be represented
 * by some (possible infinite) set of other Light objects. A correct implementation
 * should be able to return any number of samples (Light objects) that are well
 * distributed and representitive of the surface. Factoring in number of samples
 * to create an intensity value for each sample will be handled by the ray tracing engine.
 * 
 * @author Mike Murray
 */
public interface SurfaceLight extends Light {
	/**
	 * @param total  Total number of samples to return.
	 * @return  An array containing samples for this SurfaceLight instance.
	 */
	public Light[] getSamples(int total);
	
	/**
	 * @return  An array containing samples for this SurfaceLight instance.
	 */
	public Light[] getSamples();
}
