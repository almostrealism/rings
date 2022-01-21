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

package com.almostrealism.primitives;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.geometry.Ray;
import io.almostrealism.relation.Producer;
import io.almostrealism.code.Operator;
import org.almostrealism.geometry.TransformMatrix;
import org.almostrealism.hardware.DynamicProducerForMemoryData;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.geometry.ShadableIntersection;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Evaluable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

//TODO Add ParticleGroup implementation.

/**
 * A Cylinder object represents a cylinder in 3d space.
 */
public class Cylinder extends AbstractSurface implements CodeFeatures {
	/**
	 * Constructs a Cylinder object that represents a cylinder with a base radius of 1.0,
	 * with base at the origin, that is black.
	 */
	public Cylinder() {
		super();
	}
	
	/**
	 * Constructs a Cylinder object that represents a cylinder with the specified base radius, and the specified location, that is black.
	 */
	public Cylinder(Vector location, double radius) {
		super(location, radius);
	}
	
	/**
	 * Constructs a Cylinder object that represents a cylinder with the specified base radius,
	 * location, and color.
	 */
	public Cylinder(Vector location, double radius, RGB color) {
		super(location, radius, color);
	}
	
	/**
	 * @return  A Vector that represents the vector normal to this cylinder
	 *          at the point represented by the specified Vector object.
	 */
	@Override
	public Producer<Vector> getNormalAt(Producer<Vector> point) {
		Producer<Vector> normal = add(point, v(getLocation().minus()));
		normal = super.getTransform(true).transform(normal, TransformMatrix.TRANSFORM_AS_NORMAL);
		normal = multiply(normal, vector(1.0, 0.0, 1.0));
		return normal;
	}
	
	/**
	 * @return  True if the ray represented by the specified Ray object intersects the cylinder
	 *          represented by this Cylinder object.
	 */
	public boolean intersect(Ray ray) { throw new RuntimeException("Not implemented"); }
	
	/**
	 * Returns a {@link ShadableIntersection} representing the points along the ray
	 * represented by the specified {@link Ray} that intersection between the ray
	 * and the cylinder represented by this {@link Cylinder} occurs.
	 */
	@Override
	public ShadableIntersection intersectAt(Producer r) {
		TransformMatrix m = getTransform(true);
		Supplier<Evaluable<? extends Ray>> sr = r;
		if (m != null) sr = m.getInverse().transform(sr);

		final Supplier<Evaluable<? extends Ray>> fr = sr;

		Producer<Scalar> s = new DynamicProducerForMemoryData<>(args -> {
				Ray ray = fr.get().evaluate(args);

				Vector a = ray.getOrigin();
				Vector d = ray.getDirection();

				double al = a.length();
				double dl = d.length();

				a.setY(0.0);
				d.setY(0.0);

				a.multiplyBy(al / a.length());
				d.multiplyBy(dl / d.length());

				double b = d.dotProduct(a);
				double c = a.dotProduct(a);
				double g = d.dotProduct(d);

				double discriminant = (b * b) - (g) * (c - 1);
				double discriminantSqrt = Math.sqrt(discriminant) / g;

				double t0 = 0.0, t1 = 0.0;

				t0 = (-b / g) + discriminantSqrt;
				t1 = (-b / g) - discriminantSqrt;

				double l0 = ray.pointAt(scalar(t0)).get().evaluate(args).getY();
				double l1 = ray.pointAt(scalar(t1)).get().evaluate(args).getY();

				Scalar sc;

				if (l0 >= 0 && l0 <= 1.0)
					sc = new Scalar(l0);
				else if (l1 >= 0 && l1 <= 1.0)
					sc = new Scalar(l1);
				else
					return null;

				return sc;
			});

		return new ShadableIntersection(this, r, s);
	}

	@Override
	public Operator<Scalar> expect() {
		return null;
	}

	@Override
	public Operator<Scalar> get() throws InterruptedException, ExecutionException {
		return null;
	}

	@Override
	public Operator<Scalar> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}
}
