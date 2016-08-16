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

package com.almostrealism.raytracer.primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;
import org.almostrealism.util.TransformMatrix;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.Intersection;
import com.almostrealism.raytracer.engine.Ray;
import com.almostrealism.raytracer.engine.RayTracingEngine;


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
	 * Constructs a Cone object that represents a cone with the specified base radius,
	 * and the specified location, that is black.
	 */
	public Cone(Vector location, double radius) { super(location, radius); }
	
	/**
	 * Constructs a Cone object that represents a cone with the specified base radius,
	 * location, and color.
	 */
	public Cone(Vector location, double radius, RGB color) { super(location, radius, color); }
	
	/**
	 * Returns a Vector object that represents the vector normal to this cone at the point represented by the specified Vector object.
	 */
	public Vector getNormalAt(Vector point) {
		Vector normal = new Vector(point.getX(), -1.0 * point.getY(), point.getZ());
		
		super.getTransform(true).transform(normal, TransformMatrix.TRANSFORM_AS_NORMAL);
		
		return normal;
	}
	
	/**
	 * @return  True if the ray represented by the specified Ray object intersects the cone
	 *          represented by this Cone object.
	 */
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
		
		if (Math.abs(c2) >= RayTracingEngine.e) {
			double discr = c1*c1 - c0*c2;
			
			if (discr < 0.0) {
				return false;
			} else if (discr > RayTracingEngine.e) {
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
		} else if (Math.abs(c1) >= RayTracingEngine.e) {
			double t = -0.5 * c0 / c1;
			Vector p = ray.pointAt(t);
			if (p.getY() > 0.0 && p.getY() < 1.0) return true;
		} else if (Math.abs(c0) < RayTracingEngine.e) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return  An Intersection object storing the locations along the ray represented by
	 *          the specified Ray object that intersection between the ray and the cone occurs.
	 */
	public Intersection intersectAt(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
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
		
		if (Math.abs(c2) >= RayTracingEngine.e) {
			double discr = c1*c1 - c0*c2;
			
			if (discr < 0.0) {
				return new Intersection(ray, this, new double[0]);
			} else if (discr > RayTracingEngine.e) {
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
		} else if (Math.abs(c1) >= RayTracingEngine.e) {
			double t = -0.5 * c0 / c1;
			Vector p = ray.pointAt(t);
			if (p.getY() > 0.0 && p.getY() < 1.0) inter.add(new Double(t));
		} else if (Math.abs(c0) < RayTracingEngine.e) {
			inter.add(new Double(0.0));
			inter.add(new Double(1.0));
		}
		
		double t[] = new double[inter.size()];
		int i = 0;
		
		Iterator itr = inter.iterator();
		while(itr.hasNext()) t[i++] = ((Number)itr.next()).doubleValue();
		
		return new Intersection(ray, this, t);
	}
}
