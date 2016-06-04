/*
 * Copyright (C) 2004-06  Mike Murray
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

package com.almostrealism.raytracer.engine;

import com.almostrealism.util.TransformMatrix;
import com.almostrealism.util.Vector;

/**
 * A Ray object represents a 3d ray. It stores the origin and direction of a 3d ray,
 * which are vector quantities, as Vector objects.
 * 
 * @author  Mike Murray
 */
public class Ray implements Cloneable {
  // private double ox, oy, oz, dx, dy, dz;
  private double coords[] = new double[6];

	private Ray(double ox, double oy, double oz, double dx, double dy, double dz) {
		this.coords[0] = ox;
		this.coords[1] = oy;
		this.coords[2] = oz;
		this.coords[3] = dx;
		this.coords[4] = dy;
		this.coords[5] = dz;
	}
	
	/**
	 * Constructs a Ray object with origin and direction at the origin.
	 */
	public Ray() { }
	
	/**
	 * Constructs a Ray object using the specified origin and direction vectors.
	 */
	public Ray(Vector origin, Vector direction) {
		this.setOrigin(origin);
		this.setDirection(direction);
	}
	
	/**
	 * Sets the origin of this Ray object to the specified origin vector.
	 */
	public void setOrigin(Vector origin) {
		this.coords[0] = origin.getX();
		this.coords[1] = origin.getY();
		this.coords[2] = origin.getZ();
	}
	
	/**
	 * Sets the direction of this Ray object to the specified direction vector.
	 */
	public void setDirection(Vector direction) {
		this.coords[3] = direction.getX();
		this.coords[4] = direction.getY();
		this.coords[5] = direction.getZ();
	}
	
	/**
	 * Transforms the origin and direction of this ray using the specified TransformMatrix.
	 * 
	 * @param tm  TransformMatrix to use.
	 * @return  {{ox, oy, oz}, {dx, dy, dz}} after transformation.
	 */
	public double[] transform(TransformMatrix tm) {
//		double o[] = m.transform(this.ox, this.oy, this.oz, TransformMatrix.TRANSFORM_AS_LOCATION);
//		double d[] = m.transform(this.dx, this.dy, this.dz, TransformMatrix.TRANSFORM_AS_OFFSET);
		
		double m[][] = tm.getMatrix();
		
		this.coords[0] = m[0][0] * this.coords[0] + m[0][1] * this.coords[1] + m[0][2] * this.coords[2] + m[0][3];
		this.coords[1] = m[1][0] * this.coords[0] + m[1][1] * this.coords[1] + m[1][2] * this.coords[2] + m[1][3];
		this.coords[2] = m[2][0] * this.coords[0] + m[2][1] * this.coords[1] + m[2][2] * this.coords[2] + m[2][3];
		
		this.coords[3] = m[0][0] * this.coords[3] + m[0][1] * this.coords[4] + m[0][2] * this.coords[5];
		this.coords[4] = m[1][0] * this.coords[3] + m[1][1] * this.coords[4] + m[1][2] * this.coords[5];
		this.coords[5] = m[2][0] * this.coords[3] + m[2][1] * this.coords[4] + m[2][2] * this.coords[5];
		
		return this.coords;
	}
	
	public double[] getCoords() { return this.coords; }
	
	/**
	 * @return  The dot product of the origin of this ray with itself.
	 */
	public double oDoto() {
		return this.coords[0] * this.coords[0] +
				this.coords[1] * this.coords[1] +
				this.coords[2] * this.coords[2];
	}
	
	/**
	 * @return  The dot product of the direction of this ray with itself.
	 */
	public double dDotd() {
		return this.coords[3] * this.coords[3] +
				this.coords[4] * this.coords[4] +
				this.coords[5] * this.coords[5];
	}
	
	/**
	 * @return  The dot product of the origin of this ray with the direction of this ray.
	 */
	public double oDotd() {
		return this.coords[0] * this.coords[3] +
				this.coords[1] * this.coords[4] +
				this.coords[2] * this.coords[5];
	}
	
	/**
	 * @return  The origin of this Ray object as a Vector object.
	 */
	public Vector getOrigin() {
		return new Vector(this.coords[0], this.coords[1], this.coords[2], Vector.CARTESIAN_COORDINATES);
	}
	
	/**
	 * @return  The direction of this Ray object as a Vector object.
	 */
	public Vector getDirection() {
		return new Vector(this.coords[3], this.coords[4], this.coords[5], Vector.CARTESIAN_COORDINATES);
	}
	
	/**
	 * @return  The point on the ray represented by this Ray object at distance t from the origin
	 *          as a Vector object.
	 */
	public Vector pointAt(double t) {
		double px = this.coords[0] + this.coords[3] * t;
		double py = this.coords[1] + this.coords[4] * t;
		double pz = this.coords[2] + this.coords[5] * t;
		
		return new Vector(px, py, pz, Vector.CARTESIAN_COORDINATES);
	}
	
	public Object clone() {
		return new Ray(this.coords[0], this.coords[1], this.coords[2],
						this.coords[3], this.coords[4], this.coords[5]);
	}
	
	/**
	 * @return  A String representation of this Ray object.
	 */
	public String toString() {
		String value = "Ray: [" + this.coords[0] + ", " + this.coords[1] + ", " + this.coords[2] +
					"] [" + this.coords[3] + ", " + this.coords[4] + ", " + this.coords[5] + "]";
		
		return value;
	}
}
