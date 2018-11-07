/*
 * Copyright 2018 Michael Murray
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

import org.almostrealism.algebra.Intersectable;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBAdd;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.graph.PathElement;
import org.almostrealism.space.Scene;
import org.almostrealism.util.Producer;
import org.almostrealism.util.ProducerWithRank;
import org.almostrealism.util.RankedChoiceProducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LightingEngineAggregator extends RankedChoiceProducer<RGB> implements PathElement<RGB, RGB> {
	public LightingEngineAggregator(Producer<Ray> r, Iterable<Producer<RGB>> surfaces, Iterable<Light> lights, ShaderContext context) {
		init(r, surfaces, lights, context);
	}

	protected void init(Producer<Ray> r, Iterable<Producer<RGB>> surfaces, Iterable<Light> lights, ShaderContext context) {
		for (Producer<RGB> s : surfaces) {
			for (Light l : lights) {
				Collection<Producer<RGB>> otherSurfaces = Scene.separate(s, surfaces);
				Collection<Light> otherLights = Scene.separate(l, lights);

				ShaderContext c;

				if (context == null) {
					c = new ShaderContext(s, l);
					c.setOtherSurfaces(otherSurfaces);
					c.setOtherLights(otherLights);
				} else {
					c = context.clone();
					c.setSurface(s);
					c.setLight(l);
					c.setOtherSurfaces(otherSurfaces);
					c.setOtherLights(otherLights);
				}

				// TODO Choose which engine dynamically
				this.add(new IntersectionalLightingEngine(r, (Intersectable) s,
															otherSurfaces,
															l, otherLights,
															c));
			}
		}
	}

	@Override
	public Iterable<Producer<RGB>> getDependencies() {
		List<Producer<RGB>> p = new ArrayList<>();
		p.addAll(this);
		return p;
	}
}
