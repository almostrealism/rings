/*
 * Copyright 2017 Michael Murray
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

import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.space.DistanceEstimator;

import com.almostrealism.rayshade.ShaderSet;

public class RayMarchingEngine implements RayTracer.Engine {
	public static final int MAX_RAY_STEPS = 30;
	
	private DistanceEstimator estimator;
	private ShaderSet shaders;
	
	public RayMarchingEngine(DistanceEstimator e, ShaderSet shaders) {
		this.estimator = e;
		this.shaders = shaders;
	}
	
	public ColorProducer trace(Vector from, Vector direction) {
		double totalDistance = 0.0;
		
		int steps;
		
		Ray r = new Ray(from, direction);
		
		steps: for (steps = 0; steps < MAX_RAY_STEPS; steps++) {
			Vector p = from.add(direction.multiply(totalDistance));
			r = new Ray(p, direction);
			double distance = estimator.estimateDistance(r);
			totalDistance += distance;
			if (distance < 0.0001) break steps;
		}
		
		double d = 1.0 - steps / ((double) MAX_RAY_STEPS);
		
		
		
		return new RGB(d, d, d);
	}
}
