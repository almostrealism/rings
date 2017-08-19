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

import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.space.Ray;
import org.almostrealism.space.Vector;

public class RayMarchingEngine {
	public static final int MAX_RAY_STEPS = 1000;
	public static final double MIN_DISTANCE = 0.000001;
	
	private DistanceEstimator estimator;
	
	public RayMarchingEngine(DistanceEstimator e) {
		this.estimator = e;
	}
	
	public double trace(Vector from, Vector direction) {
		double totalDistance = 0.0;
		int steps;
		
		s: for (steps = 0; steps < MAX_RAY_STEPS; steps++) {
			Vector p = from.add(direction.multiply(totalDistance));
			double distance = estimator.estimateDistance(new Ray(from, p));
			totalDistance += distance;
			if (distance < MIN_DISTANCE) break s;
		}
		
		return 1.0 - steps / MAX_RAY_STEPS;
	}
}
