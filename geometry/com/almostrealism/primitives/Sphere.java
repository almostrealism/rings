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

package com.almostrealism.primitives;

import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.*;
import org.almostrealism.color.RGB;
import org.almostrealism.graph.mesh.Mesh;
import org.almostrealism.geometry.Ray;
import io.almostrealism.relation.Constant;
import io.almostrealism.relation.NameProvider;
import io.almostrealism.relation.Operator;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.BoundingSolid;
import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.CodeFeatures;
import io.almostrealism.relation.Evaluable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

// TODO Add ParticleGroup implementation.

/** A {@link Sphere} represents a primitive sphere in 3d space. */
public class Sphere extends AbstractSurface implements DistanceEstimator, CodeFeatures {
	private static boolean enableHardwareAcceleration = true;

	/** Constructs a {@link Sphere} representing a unit sphere centered at the origin that is black. */
	public Sphere() { }
	
	/**
	 * Constructs a {@link Sphere} representing a sphere with the specified center radius
	 * centered at the origin that is black.
	 */
	public Sphere(double radius) { this(new Vector(0.0, 0.0, 0.0), radius); }
	
	/**
	 * Constructs a Sphere object that represents a sphere with the specified center location
	 * and radius that is black.
	 */
	public Sphere(Vector location, double radius) {
		super(location, radius);
	}
	
	/**
	 * Constructs a Sphere object that represents a sphere with the specified center location, radius,
	 * and color.
	 */
	public Sphere(Vector location, double radius, RGB color) {
		super(location, radius, color);
	}

	@Override
	public Mesh triangulate() {
		Mesh m = super.triangulate();
		
		m.addVector(new Vector(0.0, 1.0, 0.0));
		m.addVector(new Vector(1.0, 0.0, 0.0));
		m.addVector(new Vector(0.0, -1.0, 0.0));
		m.addVector(new Vector(-1.0, 0.0, 0.0));
		m.addVector(new Vector(0.0, 0.0, 1.0));
		m.addVector(new Vector(0.0, 0.0, -1.0));
		
		m.addTriangle(0, 1, 4);
		m.addTriangle(1, 2, 4);
		m.addTriangle(2, 3, 4);
		m.addTriangle(3, 0, 4);
		m.addTriangle(1, 0, 5);
		m.addTriangle(2, 1, 5);
		m.addTriangle(3, 2, 5);
		m.addTriangle(0, 3, 5);
		
		return m;
	}

	@Override
	public double getIndexOfRefraction(Vector p) {
		double s = this.getSize();
		
		if (p.subtract(this.getLocation()).lengthSq() <= s * s + Intersection.e) {
			return super.getIndexOfRefraction();
		} else {
			return 1.0;
		}
	}
	
	/**
	 * Returns a Vector object that represents the vector normal to this sphere at the point represented
	 * by the specified Vector object.
	 */
	@Override
	public Producer<Vector> getNormalAt(Producer<Vector> point) {
		Producer<Vector> normal = add(point, v(getLocation().minus()));
		if (getTransform(true) != null) {
			Producer<Vector> fnormal = normal;
			normal = getTransform(true).transform(fnormal, TransformMatrix.TRANSFORM_AS_NORMAL);
		}

		return normal;
	}

	/**
	 * Returns a {@link ShadableIntersection} representing the nearest postive point
	 * along the the specified {@link Ray} that intersection between the ray and
	 * this {@link Sphere} occurs.
	 */
	@Override
	public ShadableIntersection intersectAt(Producer r) {
		TransformMatrix m = getTransform(true);

		Supplier<Evaluable<? extends Ray>> tr = r;
		if (m != null) tr = m.getInverse().transform(tr);

		final Supplier<Evaluable<? extends Ray>> fr = tr;

		if (enableHardwareAcceleration) {
			return new ShadableIntersection(this, r, () -> new SphereIntersectAt(fr));
		} else {
			Evaluable<Scalar> s = new Evaluable<Scalar>() {
				@Override
				public Scalar evaluate(Object[] args) {
					Ray ray = fr.get().evaluate(args);

					double b = ray.oDotd().evaluate(args).getValue();
					double c = ray.oDoto().evaluate(args).getValue();
					double g = ray.dDotd().evaluate(args).getValue();

					double discriminant = (b * b) - (g) * (c - 1);

					if (discriminant < 0) {
						return new Scalar(-1);
					}

					double discriminantSqrt = Math.sqrt(discriminant);

					double t[] = new double[2];

					t[0] = (-b + discriminantSqrt) / (g);
					t[1] = (-b - discriminantSqrt) / (g);

					Scalar st;

					if (t[0] > 0 && t[1] > 0) {
						if (t[0] < t[1]) {
							st = new Scalar(t[0]);
						} else {
							st = new Scalar(t[1]);
						}
					} else if (t[0] > 0) {
						st = new Scalar(t[0]);
					} else if (t[1] > 0) {
						st = new Scalar(t[1]);
					} else {
						return new Scalar(-1);
						// return null;
					}

					return st;
				}
			};

			return new ShadableIntersection(this, r, () -> s);
		}
	}

	@Override
	public double estimateDistance(Ray r) {
		return r.getOrigin().subtract(getLocation()).length() - getSize();
	}

	@Override
	public Operator<Scalar> get() throws InterruptedException, ExecutionException {
		return new Operator<Scalar>() {
			@Override
			public Scalar evaluate(Object[] args) {
				return new Scalar(getInput().evaluate(args).lengthSq());
			}

			@Override
			public Scope<Scalar> getScope(NameProvider p) {
				// TODO  Not sure this is correct
				Scope s = new Scope();
				s.getVariables().add(new Variable(p.getFunctionName() + "scalar", evaluate(new Object[0])));
				return s;
			}
		};
	}

	@Override
	public Operator<Scalar> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

	@Override
	public Operator<Scalar> expect() {
		return new Constant<>(new Scalar(1.0));
	}

	@Override
	public BoundingSolid calculateBoundingSolid() {
		Vector c = getLocation();
		double r = getSize();
		return new BoundingSolid(c.getX()-r, c.getX()+r, c.getY()-r, c.getY()+r, c.getZ()-r, c.getZ()+r);
	}
}
