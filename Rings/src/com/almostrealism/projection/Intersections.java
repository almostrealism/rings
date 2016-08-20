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

package com.almostrealism.projection;

import java.util.ArrayList;
import java.util.List;

import org.almostrealism.space.Intersectable;
import org.almostrealism.space.Intersection;
import org.almostrealism.space.Ray;

/**
 * Tools for computing ray intersections.
 * 
 * @author  Michael Murray
 */
public class Intersections {

	/**
	 * Returns an Intersection object that represents the closest intersection
	 * (>= RayTracingEngine.e) between a surface in the specified array of Surface
	 * objects and the ray represented by the specified Ray object. If there are
	 * no intersections >= RayTracingEngine.e then null is returned.
	 */
	public static Intersection closestIntersection(Ray ray, Iterable<? extends Intersectable> surfaces) {
		List<Intersection> intersections = new ArrayList<Intersection>();
		
		for (Intersectable s : surfaces) {
			intersections.add(s.intersectAt((Ray) ray.clone()));
		}
		
		double closestIntersection = -1.0;
		int closestIntersectionIndex = -1;
		
		i: for (int i = 0; i < intersections.size(); i++) {
			if (intersections.get(i) == null)
				continue i;
			
			for (int j = 0; j < intersections.get(i).getIntersections().length; j++) {
				if (intersections.get(i).getIntersections()[j] >= Intersection.e) {
					if (closestIntersectionIndex == -1 || intersections.get(i).getIntersections()[j] < closestIntersection) {
						closestIntersection = intersections.get(i).getIntersections()[j];
						closestIntersectionIndex = i;
					}
				}
			}
		}
		
		if (closestIntersectionIndex < 0)
			return null;
		else
			return intersections.get(closestIntersectionIndex);
	}

	/**
	 * Returns the value (>= RayTracingEngine.e) of the closest intersection point
	 * of the specified Intersection object If there are no positive intersections,
	 * -1.0 is returned.
	 */
	public static double closestIntersectionAt(Intersection intersect) {
		double intersections[] = intersect.getIntersections();
		
		double closestIntersection = -1.0;
		
		for(int i = 0; i < intersections.length; i++) {
			if (intersections[i] >= Intersection.e) {
				if (closestIntersection == -1.0 || intersections[i] < closestIntersection) {
					closestIntersection = intersections[i];
				}
			}
		}
		
		return closestIntersection;
	}

}
