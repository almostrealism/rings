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
import com.almostrealism.util.TransformMatrix;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;

//TODO Add ParticleGroup implementation.

/**
 * A Cylinder object represents a cylinder in 3d space.
 */
public class Cylinder extends AbstractSurface {
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
	 * @return  A Vector object that represents the vector normal to this cylinder
	 *          at the point represented by the specified Vector object.
	 */
	public Vector getNormalAt(Vector point) {
		Vector normal = point.subtract(super.getLocation());
		super.getTransform(true).transform(normal, TransformMatrix.TRANSFORM_AS_NORMAL);
		normal.setY(0.0);
		
		return normal;
	}
	
	/**
	 * @return  True if the ray represented by the specified Ray object intersects the cylinder
	 *          represented by this Cylinder object.
	 */
	public boolean intersect(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		Vector a = (Vector) ray.getOrigin();
		Vector d = (Vector) ray.getDirection();
		
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
		
		double l0 = ray.pointAt(t0).getY();
		double l1 = ray.pointAt(t1).getY();
		
		if ((l0 >= 0 && l0 <= 1.0) || (l1 >= 0 && l1 <= 1.0))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns an Intersection object representing the points along the ray represented by the specified Ray object that intersection
	 * between the ray and the cylinder represented by this Cylinder object occurs.
	 */
	public Intersection intersectAt(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		Vector a = (Vector) ray.getOrigin();
		Vector d = (Vector) ray.getDirection();
		
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
		
		double l0 = ray.pointAt(t0).getY();
		double l1 = ray.pointAt(t1).getY();
		
		if (l0 >= 0 && l0 <= 1.0)
			return new Intersection(ray, this, new double[] { l0 });
		else if (l1 >= 0 && l1 <= 1.0)
			return new Intersection(ray, this, new double[] { l1 });
		else
			return new Intersection(ray, this, new double[0]);
	}
}
