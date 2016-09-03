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

package com.almostrealism.rayshade;

import java.util.ArrayList;
import java.util.List;

import org.almostrealism.space.Gradient;
import org.almostrealism.space.Intersectable;
import org.almostrealism.space.Intersection;
import org.almostrealism.space.Ray;
import org.almostrealism.space.Vector;

/**
 * Extends {@link Intersection} to provide metadata that is required for shading.
 * 
 * @author  Michael Murray
 */
public class ShadableIntersection extends Intersection {
	private List<Vector> normals;
	
	public ShadableIntersection(Ray ray, Intersectable<ShadableIntersection> surface, double intersections[]) {
		super(ray, surface, intersections);
		
		normals = new ArrayList<Vector>();
		
		if (surface instanceof Gradient) {
			for (int i = 0; i < intersections.length; i++) {
				normals.add(((Gradient) surface).getNormalAt(ray.pointAt(intersections[i])));
			}
		}
	}
	
	public Vector getNormal(int index) { return normals.get(index); }
	
	public int size() { return normals.size(); }
}
