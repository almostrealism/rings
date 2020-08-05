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

package com.almostrealism.lighting;

import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.computations.VectorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shadable;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.algebra.computations.RayOrigin;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class PointLightCalculation implements Producer<RGB> {
	private Shadable surface;
	private PointLight light;
	private Producer<Ray> intersection;
	private Producer<Vector> point;
	private ShaderContext context;
	private Producer<RGB> shade;

	public PointLightCalculation(Shadable surface, PointLight light, Producer<Ray> intersection, ShaderContext context) {
		this.surface = surface;
		this.light = light;
		this.intersection = intersection;
		this.context = context;
		this.point = new RayOrigin(intersection);

		VectorProducer direction = new VectorSum(point, StaticProducer.of(light.getLocation()).scalarMultiply(-1.0));
		direction = direction.normalize().scalarMultiply(-1.0);
		context.setLightDirection(direction);

		shade = surface.shade(context);
	}

	@Override
	public RGB evaluate(Object[] args) { return shade.evaluate(args); }

	@Override
	public void compact() { shade.compact(); }
}
