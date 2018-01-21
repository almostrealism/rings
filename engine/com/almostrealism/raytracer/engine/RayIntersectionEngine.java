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
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

/**
 * TODO  This does not need {@link RenderParameters}, but rather it needs Fog Parameters.
 * 
 * @author  Michael Murray
 */
public class RayIntersectionEngine implements RayTracer.Engine {
	private Scene<ShadableSurface> scene;
	private RenderParameters rparams;
	
	public RayIntersectionEngine(Scene<ShadableSurface> s, RenderParameters rparams) {
		this.scene = s;
		this.rparams = rparams;
	}
	
	public ColorProducer trace(Vector from, Vector direction) {
		Ray r = new Ray(from, direction);

		IntersectionalLightingEngine l = new IntersectionalLightingEngine(scene);
		return l.lightingCalculation(r, scene, scene.getLights(), rparams.fogColor,
									rparams.fogDensity, rparams.fogRatio, null);
	}
}
