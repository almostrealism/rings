/*
 * Copyright (C) 2005  Mike Murray
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
import com.almostrealism.raytracer.engine.Surface;

import net.sf.j3d.util.Vector;

// TODO  Add bounding solid to make intersection calculations faster.

/**
 * A CSG object represents an object produced using a boolean
 * combination of two surfaces. 
 * 
 * @author Mike Murray
 */
public class CSG extends AbstractSurface {
  /** Integer code for boolean union (A + B). */
  public static final int UNION = 1;
  
  /** Integer code for boolean difference (A - B). */
  public static final int DIFFERENCE = 2;
  
  /** Integer code for boolean intersection (A & B). */
  public static final int INTERSECTION = 3;
  
  private int type;
  private AbstractSurface sa, sb;
  
  private boolean inverted;

  	/**
  	 * Constructs a new CSG object using the specified Surface objects.
  	 * 
  	 * @param a  Surface A.
  	 * @param b  Surface B.
  	 * @param type  The type of boolean operation to perform (UNION, DIFFERENCE, INTERSECTION).
  	 * @throws IllegalArgumentException  If the type code is not valid.
  	 */
	public CSG(AbstractSurface a, AbstractSurface b, int type) {
	    if (type < 1 || type > 3) throw new IllegalArgumentException("Illegal type: " + type);
	    
		this.sa = a;
		this.sb = b;
		
		this.type = type;
		
		this.inverted = false;
	}
	
    /**
     * @return  null.
     */
    public Vector getNormalAt(Vector point) { return null; }

    /**
     * This method calls intersectAt to determine the value to return.
     * 
     * @see com.almostrealism.raytracer.engine.Surface#intersect(com.almostrealism.raytracer.engine.Ray)
     */
    public boolean intersect(Ray ray) {
		if (this.intersectAt(ray).getIntersections().length <= 0)
		    return false;
		else
		    return true;
    }

    /**
     * @see com.almostrealism.raytracer.engine.Surface#intersectAt(com.almostrealism.raytracer.engine.Ray)
     */
    public Intersection intersectAt(Ray ray) {
    		ray.transform(this.getTransform(true).getInverse());
        
        if (this.type == CSG.UNION) {
            return RayTracingEngine.closestIntersection(ray, new Surface[] {this.sa, this.sb});
        } else if (this.type == CSG.DIFFERENCE) {
            if (this.inverted) {
                double scale[] = this.sb.getScaleCoefficients();
                this.sb.setScaleCoefficients(-1.0 * scale[0], -1.0 * scale[1], -1.0 * scale[2]);
                this.inverted = false;
            }
            
            if (!this.sa.intersect((Ray)ray.clone()))
                return new Intersection(ray, this, new double[0]);
            
            double a[] = this.sa.intersectAt((Ray)ray.clone()).getIntersections();
            if (a.length <= 0)
                return new Intersection(ray, this, new double[0]);
            
            double b[] = null;
            
            if (this.sb.intersect((Ray)ray.clone()))
                b = this.sb.intersectAt((Ray)ray.clone()).getIntersections();
            else
                b = new double[0];
            
            StringBuffer sta = new StringBuffer("a = [");
            for (int i = 0; i < a.length - 1; i++) sta.append(a[i] + ", ");
            sta.append(a[a.length - 1] + "]");
            System.out.println(sta);
            
            if (b.length > 0) {
                StringBuffer stb = new StringBuffer("b = [");
            	for (int i = 0; i < b.length - 1; i++) stb.append(b[i] + ", ");
            	if (b.length > 0) stb.append(b[b.length - 1] + "]");
            	System.out.println(stb);
            }
            
            double i[][] = null;
            
            if (b.length > 0)
                i = this.intervalDifference(this.interval(a), this.interval(b));
            else
                i = new double[][] {this.interval(a), {0.0, 0.0}};
            
            AbstractSurface s = null;
            double intersect[];
            
            if (i[0][1] - i[0][0] > RayTracingEngine.e) {
                if (i[1][1] - i[1][0] > RayTracingEngine.e) {
                    intersect = new double[] {i[0][0], i[0][1], i[1][0], i[1][0]};
                } else {
                    intersect = i[0];
                }
                
                s = this.sa;
                
                if (this.inverted) {
                    double scale[] = this.sb.getScaleCoefficients();
                    this.sb.setScaleCoefficients(-1.0 * scale[0], -1.0 * scale[1], -1.0 * scale[2]);
                    this.inverted = false;
                }
            } else {
                if (i[1][1] - i[1][0] > RayTracingEngine.e) {
                    intersect = i[1];
                } else {
                    intersect = new double[0];
                }
                
                s = this.sb;
                
                if (!this.inverted) {
                    double scale[] = this.sb.getScaleCoefficients();
                    this.sb.setScaleCoefficients(-1.0 * scale[0], -1.0 * scale[1], -1.0 * scale[2]);
                    this.inverted = true;
                }
            }
            
            return new Intersection(ray, s, intersect);
        } else if (this.type == CSG.INTERSECTION) {
            double a[] = this.sa.intersectAt((Ray)ray.clone()).getIntersections();
            if (a.length < 0) return new Intersection(ray, this, new double[0]);
            double b[] = this.sb.intersectAt((Ray)ray.clone()).getIntersections();
            
            Surface s;
            
            double ia[] = this.interval(a);
            double ib[] = this.interval(b);
            
            if (ia[0] < ib[0])
                s = this.sa;
            else
                s = this.sb;
            
            double i[] = this.intervalIntersection(ia, ib);
            
            if (i[1] - i[0] > RayTracingEngine.e)
                return new Intersection(ray, s, i);
            else
                return new Intersection(ray, s, new double[0]);
        } else {
            return null;
        }
    }
    
    public double[] interval(double intersect[]) {
        if (intersect.length <= 0) return new double[] {0.0, 0.0};
        
        double o[] = {intersect[0], intersect[0]};
        
        for (int i = 1; i < intersect.length; i++) {
            if (intersect[i] < o[0])
                o[0] = intersect[i];
            else if (intersect[i] > o[1])
                o[1] = intersect[i];
        }
        
        return o;
    }
    
    public double[][] intervalDifference(double ia[], double ib[]) {
        double o[][] = new double[2][2];
        
        if (ia[0] >= ib[0]) {
            o[0] = new double[] {0.0, 0.0};
        } else {
            o[0][0] = ia[0];
            o[0][1] = Math.min(ib[0], ia[1]);
        }
        
        if (ia[1] <= ib[1]) {
            o[1] = new double[] {0.0, 0.0};
        } else {
            o[1][0] = ib[1];
            o[1][1] = ia[1];
        }
        
        return o;
    }
    
    public double[] intervalIntersection(double ia[], double ib[]) {
        return new double[] {Math.max(ia[0], ib[0]), Math.min(ia[1], ib[1])};
    }
}