/*
 * Copyright 2017 Michael Murray
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

package com.almostrealism.raytracer.primitives;

import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.space.DistanceEstimator;
import org.almostrealism.space.Intersection;

import com.almostrealism.rayshade.ShadableIntersection;
import com.almostrealism.raytracer.engine.AbstractSurface;

// TODO Add ParticleGroup implementation.

/**
 * A Sphere represents a primitive sphere in 3d space.
 */
public class Sphere extends AbstractSurface implements DistanceEstimator {
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
	public Vector getNormalAt(Vector point) {
		Vector normal = point.subtract(super.getLocation());
		normal = super.getTransform(true).transformAsNormal(normal);
		
		return normal;
	}
	
	/**
	 * Returns true if the ray represented by the specified Ray object intersects the sphere
	 * represented by this Sphere object in real space.
	 */
	public boolean intersect(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		double b = ray.oDotd();
		double c = ray.oDoto();
		
		double discriminant = (b * b) - (ray.dDotd()) * (c - 1);
		
		if (discriminant < 0)
			return false;
		else
			return true;
	}
	
	/**
	 * Returns an Intersection object representing the points along the ray represented
	 * by the specified Ray object that intersection between the ray and the sphere
	 * represented by this Sphere object occurs.
	 */
	public ShadableIntersection intersectAt(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		double b = ray.oDotd();
		double c = ray.oDoto();
		double g = ray.dDotd();
		
		double discriminant = (b * b) - (g) * (c - 1);
		double discriminantSqrt = Math.sqrt(discriminant);
		
		double t[] = new double[2];
		
		t[0] = (-b + discriminantSqrt) / (g);
		t[1] = (-b - discriminantSqrt) / (g);
		
		return new ShadableIntersection(ray, this, t);
	}
	
	@Override
	public double estimateDistance(Ray r) {
		return r.getOrigin().subtract(getLocation()).length();
	}
}
