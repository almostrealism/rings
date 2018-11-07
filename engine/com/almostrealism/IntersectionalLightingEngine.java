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
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.util.Producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IntersectionalLightingEngine extends LightingEngine {
	// TODO  Arguments are redundant (they are found in ShaderContext)
    public IntersectionalLightingEngine(Producer<Ray> ray, Intersectable surface, Collection<Producer<RGB>> otherSurfaces,
										Light light, Iterable<Light> otherLights, ShaderContext p) {
        super(surface.intersectAt(ray), (Producer<RGB>) surface, otherSurfaces, light, otherLights, p);
    }

    public static List<Intersectable> filterIntersectables(Iterable<? extends Producer<RGB>> allSurfaces) {
		List<Intersectable> l = new ArrayList<>();
		for (Producer<RGB> p : allSurfaces) {
			if (p instanceof Intersectable) {
				l.add((Intersectable) p);
			}
		}

		return l;
	}
}
