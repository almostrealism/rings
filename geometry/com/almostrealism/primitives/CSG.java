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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.almostrealism.algebra.*;
import org.almostrealism.geometry.Ray;
import org.almostrealism.relation.Operator;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.Producer;

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
  private AbstractSurface<ShadableIntersection> sa, sb;
  
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
	
    /** @return  null. */
    @Override
    public VectorProducer getNormalAt(Vector point) { return null; }

    /**
     * This method calls intersectAt to determine the value to return.
     * 
     * @see org.almostrealism.space.ShadableSurface#intersect(org.almostrealism.geometry.Ray)
     */
    @Override
    public boolean intersect(Ray ray) {
		throw new RuntimeException("Not implemented");
    }

    /**
     * @see  Intersectable#intersectAt(Producer)
     */
    @Override
    public ContinuousField intersectAt(Producer r) {
        TransformMatrix m  = getTransform(true);
        if (m != null) r = new RayMatrixTransform(m.getInverse(), r);

        final Producer<Ray> fr = r;
        
        if (this.type == CSG.UNION) {
            return new ClosestIntersection(r, Arrays.asList(this.sa, this.sb));
        } else if (this.type == CSG.DIFFERENCE) {
			throw new RuntimeException("Not implemented");
        	/* TODO
        	return new Producer<ShadableIntersection>() {
				@Override
				public ShadableIntersection evaluate(Object[] args) {
					Ray ray = fr.evaluate(args);

					if (inverted) {
						double scale[] = sb.getScaleCoefficients();
						sb.setScaleCoefficients(-1.0 * scale[0], -1.0 * scale[1], -1.0 * scale[2]);
						inverted = false;
					}

					ShadableIntersection a = sa.intersectAt(fr).evaluate(args); // TODO  Move intersectAt call outside of inner class
					if (a == null) return null;

					ShadableIntersection b = sb.intersectAt(fr).evaluate(args); // TODO  Move intersectAt call outside of inner class

					double i[][] = null;

					if (b.length > 0)
						i = intervalDifference(interval(a), interval(b));
					else
						i = new double[][] {interval(a), {0.0, 0.0}};

					AbstractSurface s = null;
					double intersect[];

					if (i[0][1] - i[0][0] > Intersection.e) {
						if (i[1][1] - i[1][0] > Intersection.e) {
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
						if (i[1][1] - i[1][0] > Intersection.e) {
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

					return new ShadableIntersection(ray, s, intersect);
				}

				@Override
				public void compact() {
					// TODO
				}
			};
			*/
        } else if (this.type == CSG.INTERSECTION) {
        	throw new RuntimeException("Not implemented");
        	/* TODO
            double a[] = this.sa.intersectAt((Ray)ray.clone()).getIntersections();
            if (a.length < 0) return new ShadableIntersection(ray, this, new double[0]);
            double b[] = this.sb.intersectAt((Ray)ray.clone()).getIntersections();
            
            ShadableSurface s;
            
            double ia[] = this.interval(a);
            double ib[] = this.interval(b);
            
            if (ia[0] < ib[0])
                s = this.sa;
            else
                s = this.sb;
            
            double i[] = this.intervalIntersection(ia, ib);
            
            if (i[1] - i[0] > Intersection.e)
                return new ShadableIntersection(ray, s, i);
            else
                return new ShadableIntersection(ray, s, new double[0]);
            */
        } else {
            return null;
        }
    }

    @Override
    public Operator<Scalar> expect() {
        return null;
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

    @Override
    public Operator<Scalar> get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Operator<Scalar> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
