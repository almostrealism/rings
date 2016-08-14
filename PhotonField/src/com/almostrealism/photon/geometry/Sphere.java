/*
 * Copyright 2016 Michael Murray
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

package com.almostrealism.photon.geometry;

import com.almostrealism.photon.Volume;
import com.almostrealism.photon.util.VectorMath;

/**
 * A Sphere object represents a spherical volume in 3D space.
 * This Sphere is a closed rational set. Points on the surface of the sphere
 * with coordinates that can be represented by 64-bit fpp decimal values are
 * in the set.
 * 
 * @author  Mike Murray
 */
public class Sphere implements Volume {
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
	
	/**
	 * Returns a unit length vector in the direction from the origin of the
	 * sphere to the specified vector.
	 * 
	 * @param x  {x, y, z} - Position vector.
	 */
	public double[] getNormal(double x[]) {
		return VectorMath.multiply(x, 1 / VectorMath.length(x), true);
	}
	
	/**
	 * Returns true if the specified vector is inside this sphere, false otherwise.
	 * 
	 * @param x  {x, y, z} - Position vector.
	 */
	public boolean inside(double x[]) { return (VectorMath.length(x) <= this.radius); }
	
	public double intersect(double p[], double d[]) {
		p = VectorMath.multiply(p, 1.0 / this.radius, true);
		d = VectorMath.multiply(d, 1.0 / this.radius, true);
		double b = VectorMath.dot(p, d);
		double c = VectorMath.dot(p, p);
		double g = VectorMath.dot(d, d);
		
		double discriminant = (b * b) - (g) * (c - 1);
		double discriminantSqrt = Math.sqrt(discriminant);
		
		double t0 = (-b + discriminantSqrt) / (g);
		double t1 = (-b - discriminantSqrt) / (g);
		
		if (t0 < 0.0) t0 = Double.MAX_VALUE - 1.0;
		if (t1 < 0.0) t1 = Double.MAX_VALUE - 1.0;
		
		double t = Math.min(t0, t1);
		return t;
	}

	public double[] getSpatialCoords(double uv[]) {
		double y = uv[0] * 2.0 * Math.PI;
		double z = uv[1] * 2.0 * Math.PI;
		
		return new double[] {this.radius * Math.sin(y) * Math.cos(z),
							this.radius * Math.sin(y) * Math.sin(z),
							this.radius * Math.cos(y)};
	}

	public double[] getSurfaceCoords(double xyz[]) {
		VectorMath.normalize(xyz);
		double s = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1]);
		double uv[] = {0.5 + Math.asin(xyz[2]) / Math.PI, 0};
		
		if (xyz[0] < 0)
			uv[1] = 0.5 - Math.asin(xyz[1] / s) / Math.PI;
		else
			uv[1] = 0.5 + Math.asin(xyz[1] / s) / Math.PI;
		
		return uv;
	}
}
