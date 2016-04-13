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

package net.sf.j3d.raytracer.engine;

/**
  An Intersection object stores data for the intersections between a ray and a surface.
*/

public class Intersection {
  private Surface surface;
  private Ray ray;
  
  private int closest = -1;
  private double intersections[];

	/**
	 * Constructs a new Intersection object that represents an intersection between the specified
	 * Ray and Surface objects at the specified points along the ray represented by the Ray object.
	 */
	public Intersection(Ray ray, Surface surface, double intersections[]) {
		this.ray = ray;
		this.surface = surface;
		
		this.intersections = intersections;
	}
	
	/**
	 * @return  The Ray object stored by this Intersection object.
	 */
	public Ray getRay() { return this.ray; }
	
	/**
	 * @return  The Surface object stored by this Intersection object.
	 */
	public Surface getSurface() { return this.surface; }
	
	/**
	 * @return  The intersections stored by this Intersection object.
	 */
	public double[] getIntersections() { return this.intersections; }
	
	public double getClosestIntersection() {
		if (this.closest >= 0) {
			return this.intersections[this.closest];
		} else if (this.intersections.length <= 0) {
			return -1.0;
		} else if (this.intersections.length == 1 && this.intersections[0] >= RayTracingEngine.e) {
			this.closest = 0;
			return this.intersections[0];
		} else {
			double closestIntersection = -1.0;
			
			for(int i = 0; i < intersections.length; i++) {
				if (intersections[i] >= RayTracingEngine.e) {
					if (closestIntersection == -1.0 || intersections[i] < closestIntersection) {
						closestIntersection = intersections[i];
						this.closest = i;
					}
				}
			}
			
			return closestIntersection;
		}
	}
	
	/**
	 * @return  A String representation of this Intersection object.
	 */
	public String toString() {
		String value = "[";
		
		for(int i = 0; i < this.intersections.length; i++) {
			if (i == 0)
				value = value + intersections[i];
			else
				value = value + ", " + intersections[i];
		}
		
		value = value + "]";
		
		return value;
	}
}
