/*
 * Copyright (C) 2005-06  Mike Murray
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

import com.almostrealism.util.graphics.RGB;

/**
 * A RenderParameters object stores parameters for the RayTracingEngine.
 * 
 * @author Mike Murray
 */
public class RenderParameters {
	public RenderParameters() { }
	
	public RenderParameters(int x, int y, int dx, int dy, int w, int h, int ssw, int ssh) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.width = w;
		this.height = h;
		this.ssWidth = ssw;
		this.ssHeight = ssh;
	}
	
	/**  Full image dimensions. */
	public int width, height;
	
	/** Super sample dimensions. */
	public int ssWidth, ssHeight;
	
	/** Coordinates of upper left corner of image. */
	public int x, y;
	
	/** Viewable image dimensions. */
	public int dx, dy;
	
	public RGB fogColor;
	public double fogDensity = 0.0, fogRatio = 0.5;
}
