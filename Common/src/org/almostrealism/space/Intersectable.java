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

import com.almostrealism.raytracer.engine.Intersection;

/**
 * @author  Michael Murray
 */
public interface Intersectable {
	/** Returns true if the ray intersects the 3d surface in real space. */
	public boolean intersect(Ray ray);
	
	/**
	 * Returns an Intersection object that represents the values for t that solve
	 * the vector equation p = o + t * d where p is a point of intersection of
	 * the specified ray and the surface.
	 */
	public Intersection intersectAt(Ray ray);
}