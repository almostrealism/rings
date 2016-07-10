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
