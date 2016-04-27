/*
 * Copyright (C) 2004  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.primitives;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.Intersection;
import com.almostrealism.raytracer.engine.Ray;
import com.almostrealism.raytracer.engine.RayTracingEngine;

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

//TODO Add ParticleGroup implementation.

/**
 * A Sphere object represents a primitive sphere in 3d space.
 */
public class Sphere extends AbstractSurface {
	/**
	 * Constructs a Sphere object that represents a unit sphere centered at the origin that is black.
	 */
	public Sphere() {
		super();
	}
	
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
		
		if (p.subtract(this.getLocation()).lengthSq() <= s * s + RayTracingEngine.e) {
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
	public Intersection intersectAt(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		double b = ray.oDotd();
		double c = ray.oDoto();
		double g = ray.dDotd();
		
		double discriminant = (b * b) - (g) * (c - 1);
		double discriminantSqrt = Math.sqrt(discriminant);
		
		double t[] = new double[2];
		
		t[0] = (-b + discriminantSqrt) / (g);
		t[1] = (-b - discriminantSqrt) / (g);
		
		return new Intersection(ray, this, t);
	}
}
