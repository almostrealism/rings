/*
 * Copyright 2020 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism;

import java.util.ArrayList;

import com.almostrealism.raytracer.RayTracer;
import io.almostrealism.code.Scope;
import org.almostrealism.algebra.*;
import org.almostrealism.algebra.computations.RayDirection;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.RGBAdd;
import org.almostrealism.color.ShadableCurve;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.color.ShaderSet;
import org.almostrealism.geometry.Ray;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.relation.NameProvider;
import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.space.LightingContext;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class RayMarchingEngine extends ArrayList<Producer<Ray>> implements RayTracer.Engine, ShadableCurve, DiscreteField, HardwareFeatures {
	private ShaderContext sparams;
	private RenderParameters params;
	private FogParameters fparams;

	private DistanceEstimator estimator;
	private Iterable<? extends Producer<RGB>> allSurfaces;
	private Light allLights[];

	private Light lights[];
	private ShaderSet<? extends LightingContext> shaders;
	
	public RayMarchingEngine(Iterable<? extends Producer<RGB>> allSurfaces,
							 Light allLights[], Light l, DistanceEstimator e, ShaderSet shaders) {
		this.allSurfaces = allSurfaces;
		this.allLights = allLights;
		this.sparams = new ShaderContext(this, l);
		this.params = new RenderParameters();
		this.fparams = new FogParameters();
		this.estimator = e;
		this.lights = allLights;
		this.shaders = shaders;
	}

	@Override
	public Producer<RGB> trace(Producer<Ray> r) {
		// TODO
//		return new DistanceEstimationLightingEngine(r, allSurfaces, allLights, sparams, estimator, shaders);
		return null;
	}

	@Override
	public Producer<RGB> getValueAt(Producer<Vector> point) {
		return StaticProducer.of(new RGB(0.8, 0.8, 0.8));  // TODO  Support colors
	}

	@Override
	public Producer<Vector> getNormalAt(Producer<Vector> point) {
		return compileProducer(new RayDirection(iterator().next()));
	}

	@Override
	public RGB operate(Vector in) {
		return getValueAt(StaticProducer.of(in)).evaluate();
	}

	@Override
	public Scope getScope(NameProvider prefix) { throw new RuntimeException("getScope is not implemented"); } // TODO
	
	@Override
	public Producer<RGB> shade(ShaderContext parameters) {
		Producer<RGB> c = null;
		
		for (Shader s : shaders) {
			if (c == null) {
				c = s.shade(parameters, this);
			} else {
				c = new RGBAdd(c, s.shade(parameters, this));
			}
		}
		
		return c;
	}
}
