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

import com.almostrealism.raytracer.RayTracer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBBank;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.relation.Producer;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.relation.Evaluable;
import org.almostrealism.util.DimensionAware;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Michael Murray
 */
public class RayIntersectionEngine implements RayTracer.Engine {
	public static boolean enableAcceleratedAggregator = false;

	private Scene<ShadableSurface> scene;
	private ShaderContext sparams;
	private FogParameters fparams;
	
	public RayIntersectionEngine(Scene<ShadableSurface> s, FogParameters fparams) {
		this.scene = s;
		this.fparams = fparams;
	}

	@Override
	public Producer<RGB> trace(Producer<Ray> r) {
		List<Curve<RGB>> surfaces = new ArrayList<>();
		for (ShadableSurface s : scene) surfaces.add(s);
		LightingEngineAggregator agg = new LightingEngineAggregator(r, surfaces, scene.getLights(), sparams, true);
		return enableAcceleratedAggregator ? () -> agg.getAccelerated() : new Kernel(agg);
	}

	private class Kernel implements Producer<RGB>, DimensionAware {
		private LightingEngineAggregator agg;

		public Kernel(LightingEngineAggregator agg) {
			this.agg = agg;
		}

		@Override
		public Evaluable<RGB> get() {
			return agg;
		}

		@Override
		public void setDimensions(int width, int height, int ssw, int ssh) {
			agg.setDimensions(width, height, ssw, ssh);
		}
	}
}
