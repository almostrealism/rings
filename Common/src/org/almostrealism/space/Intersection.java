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

package org.almostrealism.space;

import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.ShadableSurface;

/**
 * An Intersection object stores data for the intersections between a ray and a surface.
 */
public class Intersection {
  private ShadableSurface surface;
  private Ray ray;
  
  private int closest = -1;
  private double intersections[];

	/**
	 * Constructs a new Intersection object that represents an intersection between the specified
	 * Ray and Surface objects at the specified points along the ray represented by the Ray object.
	 */
	public Intersection(Ray ray, ShadableSurface surface, double intersections[]) {
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
	public ShadableSurface getSurface() { return this.surface; }
	
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
