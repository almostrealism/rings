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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.almostrealism.algebra.*;
import org.almostrealism.color.RGB;
import org.almostrealism.geometry.Ray;
import org.almostrealism.relation.Operator;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.Producer;


// TODO Add ParticleGroup implementation.

/**
 * A Cone object represents a cone in 3d space.
 */
public class Cone extends AbstractSurface {
	private static final double nsq = 1.0 / 2.0;

	/**
	 * Constructs a Cone object that represents a cone with a base radius of 1.0,
	 * centered at the origin, that is black.
	 */
	public Cone() { super(); }
	
	/**
	 * Constructs a Cone object that represents a cone with the specified base
	 * radius, and the specified location, that is black.
	 */
	public Cone(Vector location, double radius) { super(location, radius); }
	
	/**
	 * Constructs a Cone object that represents a cone with the specified base radius,
	 * location, and color.
	 */
	public Cone(Vector location, double radius, RGB color) { super(location, radius, color); }
	
	/**
	 * Returns a Vector object that represents the vector normal to this cone at the
	 * point represented by the specified Vector object.
	 */
	@Override
	public VectorProducer getNormalAt(Vector point) {
		// TODO  Perform computation within VectorProducer
		Vector normal = new Vector(point.getX(), -1.0 * point.getY(), point.getZ());
		super.getTransform(true).transform(normal, TransformMatrix.TRANSFORM_AS_NORMAL);
		return new ImmutableVector(normal);
	}
	
	/**
	 * @return  True if the ray represented by the specified Ray object intersects the cone
	 *          represented by this Cone object.
	 */
	@Override
	public boolean intersect(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		Vector d = ray.getDirection();
		Vector o = ray.getOrigin();
		
		double ry = d.getY();
		double oy = o.getY();
		double od = d.dotProduct(o);
		double oo = o.dotProduct(o);
		
		double c2 = ry * ry - Cone.nsq;
		double c1 = ry * oy - Cone.nsq * od;
		double c0 = oy * oy - Cone.nsq * oo;
		
		if (Math.abs(c2) >= Intersection.e) {
			double discr = c1*c1 - c0*c2;
			
			if (discr < 0.0) {
				return false;
			} else if (discr > Intersection.e) {
				double root = Math.sqrt(discr);
				double invC2 = 1.0 / c2;
				
				double t = (-c1 - root) * invC2;
				Vector p = ray.pointAt(t);
				if (p.getY() > 0.0 && p.getY() < 1.0) return true;
				
				t = (-c1 + root) * invC2;
				p = ray.pointAt(t);
				if (p.getY() > 0.0 && p.getY() < 1.0) return true;
			} else {
				double t = -c1 / c2;
				Vector p = ray.pointAt(t);
				
				if (p.getY() > 0.0 && p.getY() < 1.0) return true;
			}
		} else if (Math.abs(c1) >= Intersection.e) {
			double t = -0.5 * c0 / c1;
			Vector p = ray.pointAt(t);
			if (p.getY() > 0.0 && p.getY() < 1.0) return true;
		} else if (Math.abs(c0) < Intersection.e) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return  An {@link Intersection} storing the locations along the ray represented by
	 *          the specified {@link Ray} that intersection between the ray and the cone occurs.
	 */
	@Override
	public Producer<ShadableIntersection> intersectAt(Producer r) {
		TransformMatrix m = getTransform(true);
		if (m != null) r = new RayMatrixTransform(m.getInverse(), r);

		final Producer<Ray> fr = r;

		return new Producer<ShadableIntersection>() {
			@Override
			public ShadableIntersection evaluate(Object[] args) {
				Ray ray = fr.evaluate(args);

				Vector d = ray.getDirection().divide(ray.getDirection().length());
				Vector o = ray.getOrigin().divide(ray.getOrigin().length());

				double ry = d.getY();
				double oy = o.getY();
				double od = d.dotProduct(o);
				double oo = o.dotProduct(o);

				double c2 = ry * ry - Cone.nsq;
				double c1 = ry * oy - Cone.nsq * od;
				double c0 = oy * oy - Cone.nsq * oo;

				List inter = new ArrayList();

				if (Math.abs(c2) >= Intersection.e) {
					double discr = c1 * c1 - c0 * c2;

					if (discr < 0.0) {
						return null;
					} else if (discr > Intersection.e) {
						double root = Math.sqrt(discr);
						double invC2 = 1.0 / c2;

						double t = (-c1 - root) * invC2;
						Vector p = ray.pointAt(t);
						if (p.getY() > 0.0 && p.getY() < 1.0) inter.add(new Double(t));

						t = (-c1 + root) * invC2;
						p = ray.pointAt(t);
						if (p.getY() > 0.0 && p.getY() < 1.0) inter.add(new Double(t));
					} else {
						double t = -c1 / c2;
						Vector p = ray.pointAt(t);

						if (p.getY() > 0.0 && p.getY() < 1.0) inter.add(new Double(t));
					}
				} else if (Math.abs(c1) >= Intersection.e) {
					double t = -0.5 * c0 / c1;
					Vector p = ray.pointAt(t);
					if (p.getY() > 0.0 && p.getY() < 1.0) inter.add(new Double(t));
				} else if (Math.abs(c0) < Intersection.e) {
					inter.add(new Double(0.0));
					inter.add(new Double(1.0));
				}

				double t = Double.MAX_VALUE;
				int i = 0;

				Iterator itr = inter.iterator();
				while (itr.hasNext()) {
					double n = ((Number) itr.next()).doubleValue();
					if (n >= 0.0 && n < t) t = n;
				}

				if (t == Double.MAX_VALUE) {
					return null;
				} else {
					return new ShadableIntersection(ray, Cone.this, new Scalar(t));
				}
			}

			@Override
			public void compact() { }
		};
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