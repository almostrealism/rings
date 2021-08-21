/*
 * Copyright 2019 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.geometry;

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.space.Volume;
import org.almostrealism.util.CodeFeatures;
import io.almostrealism.relation.Evaluable;

/**
 * A {@link Sphere} represents a spherical volume in 3D space.
 * This Sphere is a closed rational set. Points on the surface of the sphere
 * with coordinates that can be represented by 64-bit fpp decimal values are
 * in the set.
 * 
 * @author  Michael Murray
 */
public class Sphere implements Volume<Object>, CodeFeatures {
	private double radius;
	
	/**
	 * Constructs the unit Sphere.
	 */
	public Sphere() { this(1.0); }
	
	/**
	 * Constructs a Sphere object with the specified radius.
	 * 
	 * @param radius  Radius to use.
	 */
	public Sphere(double radius) { this.radius = radius; }
	
	public void setRadius(double r) { this.radius = r; }
	public double getRadius() { return this.radius; }

	@Override
	public Producer getValueAt(Producer point) {
		return null;
	}

	/**
	 * Returns a unit length vector in the direction from the origin of the
	 * sphere to the specified vector.
	 * 
	 * @param x  {x, y, z} - Position vector.
	 */
	@Override
	public VectorProducer getNormalAt(Producer<Vector> x) {
		return normalize(x);
	}
	
	/**
	 * Returns true if the specified vector is inside this sphere, false otherwise.
	 * 
	 * @param x  {x, y, z} - Position vector.
	 */
	@Override
	public boolean inside(Producer<Vector> x) { return (x.get().evaluate().length() <= this.radius); }

	@Override
	public double intersect(Vector p, Vector d) {
		p = p.divide(radius);
		d = d.divide(radius);
		double b = p.dotProduct(d);
		double c = p.dotProduct(p);
		double g = d.dotProduct(d);
		
		double discriminant = (b * b) - (g) * (c - 1);
		double discriminantSqrt = Math.sqrt(discriminant);
		
		double t0 = (-b + discriminantSqrt) / (g);
		double t1 = (-b - discriminantSqrt) / (g);
		
		if (t0 < 0.0) t0 = Double.MAX_VALUE - 1.0;
		if (t1 < 0.0) t1 = Double.MAX_VALUE - 1.0;
		
		double t = Math.min(t0, t1);
		return t;
	}

	@Override
	public double[] getSpatialCoords(double uv[]) {
		double y = uv[0] * 2.0 * Math.PI;
		double z = uv[1] * 2.0 * Math.PI;
		
		return new double[] {this.radius * Math.sin(y) * Math.cos(z),
							this.radius * Math.sin(y) * Math.sin(z),
							this.radius * Math.cos(y)};
	}

	@Override
	public double[] getSurfaceCoords(Evaluable<Vector> v) {
		double xyz[] = v.evaluate(new Object[0]).toArray();

		double s = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1]);
		double uv[] = {0.5 + Math.asin(xyz[2]) / Math.PI, 0};
		
		if (xyz[0] < 0)
			uv[1] = 0.5 - Math.asin(xyz[1] / s) / Math.PI;
		else
			uv[1] = 0.5 + Math.asin(xyz[1] / s) / Math.PI;
		
		return uv;
	}

	@Override
	public Object operate(Vector triple) {
		return null;
	}
}
