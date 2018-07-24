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

import org.almostrealism.algebra.ClosestIntersection;
import org.almostrealism.algebra.Intersectable;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.Light;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.Producer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class IntersectionalLightingEngine extends LightingEngine {
    public IntersectionalLightingEngine(Producer<Ray> ray, Iterable<? extends Callable<ColorProducer>> allSurfaces, Light allLights[], ShaderContext p) {
        super(new ClosestIntersection<>(ray, filterIntersectables(allSurfaces)), allSurfaces, allLights, p);
    }

    public static List<Intersectable<ShadableIntersection, ?>> filterIntersectables(Iterable<? extends Callable<ColorProducer>> allSurfaces) {
		List<Intersectable<ShadableIntersection, ?>> l = new ArrayList<>();
		for (Callable<ColorProducer> p : allSurfaces) {
			if (p instanceof Intersectable) {
				l.add((Intersectable<ShadableIntersection, ?>) p);
			}
		}

		return l;
	}
}
