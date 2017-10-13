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

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.almostrealism.lighting.Light;
import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Triple;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.space.DistanceEstimator;

import com.almostrealism.rayshade.ShadableCurve;
import com.almostrealism.rayshade.Shader;
import com.almostrealism.rayshade.ShaderParameters;
import com.almostrealism.rayshade.ShaderSet;

public class RayMarchingEngine extends ArrayList<Callable<Ray>> implements RayTracer.Engine, ShadableCurve, DiscreteField {
	private ShaderParameters sparams;
	private RenderParameters params;
	private DistanceEstimator estimator;
	private Light lights[];
	private ShaderSet shaders;
	
	public RayMarchingEngine(ShaderParameters sparams, RenderParameters params, DistanceEstimator e, Light allLights[], ShaderSet shaders) {
		this.sparams = sparams;
		this.params = params;
		this.estimator = e;
		this.lights = allLights;
		this.shaders = shaders;
	}
	
	public ColorProducer trace(Vector from, Vector direction) {
		Ray r = new Ray(from, direction);

		DistanceEstimationLightingEngine l = new DistanceEstimationLightingEngine(estimator, shaders, this.sparams.getLight());
		return l.lightingCalculation(r, new ArrayList<Callable<ColorProducer>>(),
										this.lights, params.fogColor,
										params.fogDensity, params.fogRatio, sparams);
	}

	@Override
	public Vector getNormalAt(Vector point) {
		try {
			return iterator().next().call().getDirection();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Vector operate(Triple in) { return getNormalAt(new Vector(in.getA(), in.getB(), in.getC())); }

	@Override
	public ColorProducer call() throws Exception {
		return new RGB(0.8, 0.8, 0.8);  // TODO  Support colors
	}
	
	@Override
	public ColorProducer shade(ShaderParameters parameters) {
		ColorSum c = new ColorSum();
		
		for (Shader s : shaders) {
			c.add(s.shade(parameters, this));
		}
		
		return c;
	}
}
