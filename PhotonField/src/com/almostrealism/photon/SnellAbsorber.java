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
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photon;

import java.lang.Math;
import java.util.ArrayList;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.util.VectorMath;

/**
 * A SnellAbsorber is an Absorber implementation that absorbs and emits photons
 * contained within a three dimensional volume. The photons absorbed are re-emitted
 * based on Snell's law of refraction.
 * 
 * @author Mike Murray
 */
public class SnellAbsorber implements Absorber {
	private Volume volume;
	private Clock clock;
	private ArrayList Queue = new ArrayList();
	private double[] n = {0, 0}; // Refraction values for mediums 
		
	// Upon absorption energy of incoming rays is added to the Queue as an array
	// with important information, like angle and energy, so that conservation is correct.
	public boolean absorb(double[] Position, double[] Direction, double Energy) {
		if (!this.volume.inside(Position)) return false;
		
		double data[][] = {Position, Direction, {Energy}};
		Queue.add(data);
		return true;
	}

	/*
	 * Snell's law should come into effect in the emit phase, as it
	 * is related to refraction and not absorbtion. However, this means
	 * that the emit fuction requires that the incoming angle (angle of incidence)
	 * and the n values (refraction indices) are known to predict the angle of 
	 * refraction.
	 * 
	 * If the incAngle > asin(n2/n1), then no refraction occurs, and the photon is
	 * reflected at 100% strength.
	 * 
	 * Assumptions: incAngle will not be above 180
	 */
	public double[] emit() {
		if (Queue.isEmpty()) return null;
		
		double d[], normal[], R[];
		double alpha;
		// Creating n values for refractive surfaces
		n[0] = 1.00;
		n[1] = 1.0001;
		
		// d is the direction vector
		d = ((double[][])(Queue.get(0)))[1];
		
		// Accepts position vector, returns the Normal 
		normal = this.volume.getNormal(((double[][])Queue.get(0))[0]);
		
		// resultant = -(p + 2N(p.N)). What is this good for? 
		// double resultant[] = VectorMath.subtract(VectorMath.multiply(n, VectorMath.dot(d, n) * 2), d);
		
		// Snell's Law calculation
		// R.normal = alpha = sqrt(1 - (n[0]^2/n[1]^2) * (1 - d.normal)^2)
		alpha = Math.sqrt(1 - ((n[0] * n[0]) / (n[1] *n[1])) * (1 - Math.pow(VectorMath.dot(d, normal), 2)));
		
		// R = -(alpha*N) + (sqrt(1-alpha^2))(N x (N x I))
		R = VectorMath.multiply(VectorMath.add(VectorMath.multiply(normal, alpha), VectorMath.multiply(VectorMath.cross(normal, VectorMath.cross(normal, d)), Math.sqrt(1 - ((alpha * alpha))))), -1);
		
		if (Math.random() < 0.0001) {
			System.out.println(VectorMath.print(R));
		}
		
		this.Queue.remove(0);
		
		return VectorMath.normalize(R);
	}

	// Get and Set methods follow
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
		
	public void setN(double[] incN) {this.n[0] = incN[0]; this.n[1] = incN[1];}
	public double[] getN() {return this.n;}	
	
	public void setVolume(Volume v) { this.volume = v; }
	public Volume getVolume() { return this.volume; }
	
	// Returns energy of next item in the queue
	public double getEmitEnergy() {
		return ((double[][])(Queue.get(0)))[2][0];
	}
	
	// Returns confirmation of existence of another item in queue
	public double getNextEmit() {
		if (!Queue.isEmpty())
			return 0.0; // Confirms next item exists
		else
			return Double.MAX_VALUE;
	}

	// Returns Position vector of next item
	public double[] getEmitPosition() {
		if (!Queue.isEmpty()) {
			if (Math.random() < 0.0001)
				System.out.println(VectorMath.print(((double[][])Queue.get(0))[0]));
			return ((double[][])Queue.get(0))[0];
		} else {
			return null;
		}
	}
}
