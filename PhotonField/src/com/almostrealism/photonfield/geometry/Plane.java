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

package com.almostrealism.photonfield.geometry;

import com.almostrealism.photonfield.Volume;
import com.almostrealism.photonfield.util.VectorMath;

public class Plane implements Volume {
	public static double d = 0.0;
	
	protected double w, h;
	protected double thick = 0.5;
	protected double normal[], up[], across[];
	
	/**
	 * @param t  The thickness of the plane (usually measured in micrometers).
	 */
	public void setThickness(double t) { this.thick = t; }
	
	/**
	 * @return  The thickness of the plane (usually measured in micrometers).
	 */
	public double getThickness() { return this.thick; }
	
	/**
	 * @param w  The width of the plane (usually measured in micrometers).
	 */
	public void setWidth(double w) { this.w = w; }
	
	/**
	 * Returns the width of the plane (usually measured in micrometers).
	 */
	public double getWidth() { return this.w; }
	
	/**
	 * @param h  The height of the plane (usually measured in micrometers).
	 */
	public void setHeight(double h) { this.h = h; }
	
	/**
	 * Returns the height of the plane (usually measured in micrometers).
	 */
	public double getHeight() { return this.h; }
	
	/**
	 * @param p  {x, y, z} - The vector normal to the plane.
	 */
	public void setSurfaceNormal(double p[]) { this.normal = p;	this.across = null; }
	
	/**
	 * @return  {x, y, z} - The vector normal to the plane.
	 */
	public double[] getSurfaceNormal() { return this.normal; }
	
	/**
	 * @param p  {x, y, z} - The vector pointing upwards across the surface of this
	 *           absorption plane. This vector must be orthagonal to the surface normal.
	 */
	public void setOrientation(double p[]) { this.up = p; this.across = null; }
	
	/**
	 * @return  {x, y, z} - The vector pointing upwards across the surface of this
	 *           absorption plane.
	 */
	public double[] getOrientation() { return this.up; }
	
	public double[] getAcross() { 
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		return this.across;
	}
	
	public boolean inside(double x[]) {
		double d = Math.abs(VectorMath.dot(x, this.normal));
		Plane.d = d;
		if (d > this.thick) return false;
		
		double y = Math.abs(VectorMath.dot(x, this.up));
		if (y > this.h / 2.0) return false;
		
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		double z = Math.abs(VectorMath.dot(x, this.across));
		if (z > this.w / 2.0) return false;
		
		return true;
	}
	
	public double intersect(double p[], double d[]) {
		double a = VectorMath.dot(p, this.normal);
		double b = VectorMath.dot(d, this.normal);
		
		double d1 = (this.thick - a) / b;
		double d2 = (-this.thick - a) / b;
		
		if (d1 < 0.0) {
			d1 = Double.MAX_VALUE - 1.0;
		} else {
			double x[] = VectorMath.multiply(d, d1 + this.thick / 2.0, true);
			VectorMath.addTo(x, p);
			if (!this.inside(x)) d1 = Double.MAX_VALUE - 1.0;
		}
		
		if (d2 < 0.0) {
			d2 = Double.MAX_VALUE - 1.0;
		} else {
			double x[] = VectorMath.multiply(d, d2 - this.thick / 2.0, true);
			VectorMath.addTo(x, p);
			if (!this.inside(x)) d2 = Double.MAX_VALUE - 1.0;
		}
		
		
		
		return Math.min(d1, d2);
	}
	
	public double[] getNormal(double x[]) { return VectorMath.clone(this.normal); }

	public double[] getSpatialCoords(double uv[]) {
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		double x[] = VectorMath.multiply(this.across, (uv[0] - 0.5) * this.w, true);
		return VectorMath.addMultiple(x, this.up, (0.5 - uv[1]) * this.h);
	}

	public double[] getSurfaceCoords(double xyz[]) {
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		return new double[] {0.5 + VectorMath.dot(this.across, xyz) / this.w,
							0.5 - VectorMath.dot(this.up, xyz) / this.h};
	}
}
