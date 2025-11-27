/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.raytracer;

import org.almostrealism.algebra.ScalarFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.geometry.ContinuousField;
import org.almostrealism.algebra.Vector;
import io.almostrealism.code.Operator;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.geometry.ShadableIntersection;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Thing extends AbstractSurface implements ScalarFeatures {
	private Plane p = new Plane(Plane.XY);

	@Override
	public ContinuousField intersectAt(Producer ray) {
		if (Math.random() > 0.5) {
			return this.p.intersectAt(ray);
		} else {
			return new ShadableIntersection(this, ray, c(-1));
		}
	}

	@Override
	public Operator<PackedCollection<?>> expect() {
		return p.expect();
	}

	@Override
	public Operator<PackedCollection<?>> get() {
		return p.get();
	}

	@Override
	public Producer<Vector> getNormalAt(Producer<Vector> point) {
		return vector(0.0, 0.0, 1.0);
	}
}
