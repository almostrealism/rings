/*
 * Copyright 2019 Michael Murray
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

package com.almostrealism.lighting;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shadable;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.RayOrigin;
import org.almostrealism.util.Producer;

public class PointLightCalculation implements Producer<RGB> {
	private Shadable surface;
	private PointLight light;
	private Producer<Ray> intersection;
	private ShaderContext context;

	public PointLightCalculation(Shadable surface, PointLight light, Producer<Ray> intersection, ShaderContext context) {
		this.surface = surface;
		this.light = light;
		this.intersection = intersection;
		this.context = context;
	}

	@Override
	public RGB evaluate(Object[] args) {
		Vector origin = new RayOrigin(intersection).evaluate(args);
		if (origin == null) return new RGB(0.0, 0.0, 0.0);

		// TODO  Move call to shade to initialization
		Vector direction = origin.subtract(light.getLocation());

		DirectionalAmbientLight directionalLight =
				new DirectionalAmbientLight(1.0, light.getColorAt().operate(origin), direction);
		Vector l = (directionalLight.getDirection().divide(directionalLight.getDirection().length())).minus();

		ShaderContext c = context.clone();
		c.setLightDirection(l);

		Producer<RGB> s = surface.shade(c);
		if (s == null) throw new NullPointerException();

		RGB rgb = s.evaluate(args);
		if (s == null)
			throw new NullPointerException();
		return rgb;
	}

	@Override
	public void compact() {
		// TODO
	}
}
