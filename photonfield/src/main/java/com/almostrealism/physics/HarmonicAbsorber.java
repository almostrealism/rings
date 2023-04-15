/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.physics;

import java.io.IOException;

import javax.swing.JFrame;

import com.almostrealism.chem.PotentialMap;
import com.almostrealism.primitives.AbsorptionPlane;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.ImmutableVector;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.physics.Clock;

import com.almostrealism.light.LightBulb;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Evaluable;

import static org.almostrealism.Ops.ops;

/**
 * A HarmonicAbsorber object represents a spherical absorber that stores
 * energy proportional to the square of the displacement vector.
 * 
 * @author  Michael Murray
 */
public class HarmonicAbsorber implements SphericalAbsorber, CodeFeatures {
	public static double verbose = Math.pow(10.0, -3.0);
	
	private Clock clock;
	private double energy, radius, k, q, d;
	private Producer<Vector> dp;
	private Vector place;
	
	public static void main(String args[]) {
		// Create a harmonic absorber and confine it to a sphere
		// with a radius of one micrometer.
		HarmonicAbsorber b = new HarmonicAbsorber();
		b.setQuanta(0.2);
		b.setRigidity(1.0);
		b.setRadius(0.1);
		
		// Create an AbsorptionPlane to display radiation that is
		// not absorbed by the black body sphere.
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(Math.pow(10.0, -1.0)); // Each pixel is a 100 square nanometers
		plane.setWidth(500);  // Width = 50 micrometers
		plane.setHeight(500); // Height = 50 micrometers
		plane.setThickness(0.1); // One micrometer thick
		
		// Facing the negative X direction and oriented so
		// that the positive Y axis is "upward".
		plane.setSurfaceNormal(new ImmutableVector(-1.0, 0.0, 0.0));
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		// Create a light bulb
		LightBulb l = new LightBulb();
		l.setPower(LightBulb.wattsToEvMsec * 0.01);
		
		// Add black body and light bulb to absorber set
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(3.0 * Math.pow(10.0, 1.0));
		a.addAbsorber(b, Ops.ops().vector(0.5, 0.0, 0.0));
		a.addAbsorber(l, Ops.ops().vector(-1.0, 0.0, 0.0));
		a.addAbsorber(plane, Ops.ops().vector(2.5, 0.0, 0.0));
		
		// Create photon field and set absorber to the absorber set
		// containing the black body and the light bulb
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		// Create a clock and add the photon field
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Harmonic Absorber Test");
		frame.getContentPane().add(plane.getDisplay());
		frame.setSize(150, 150);
		frame.setVisible(true);
		
		long start = System.currentTimeMillis();
		
		// Run the simulation and print out flux measurements every second
		while (true) {
			c.tick().get().run();
			
			if (Math.random() < BlackBody.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				try {
					plane.saveImage("harmonic-sim.jpg");
				} catch (IOException ioe) {
					System.out.println("HarmonicAbsorber: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}
	
	public HarmonicAbsorber() {
		this.place = ZeroVector.getEvaluable().evaluate();
	}

	@Override
	public void setPotentialMap(PotentialMap m) { }

	@Override
	public PotentialMap getPotentialMap() { return null; }
	
	public void setRigidity(double k) { this.k = k; }
	public double getRigidity() { return this.k; }

	@Override
	public void setRadius(double r) { this.radius = r; }

	@Override
	public double getRadius() { return this.radius; }
	
	public void setQuanta(double q) { this.q = q; }
	public double getQuanta() { return this.q; }

	@Override
	public Producer<Vector> getDisplacement() {
		return multiply(p(place), dp);
	}
	
	protected void updateDisplacement() {
		this.d = radius * Math.sqrt(energy / k);
		double off = d / place.length();
		this.dp = Ops.ops().vector(off, off, off);
	}
	
	public boolean absorb(Vector x, Vector p, double energy) {
		if (x.length() > this.radius) return false;
		
		if (Math.random() < verbose)
			System.out.println("HarmonicAbsorber: Absorb energy = " + energy);

		place = add(v(place), v(p)).get().evaluate();
		this.energy += energy;
		
		this.updateDisplacement();
		
		return true;
	}

	@Override
	public Producer<Vector> emit() {
		double e = this.getEmitEnergy();
		this.energy -= e;

		// TODO  Perform computation within the returned producer

		double pd = place.length();
		Vector p = place.divide(pd);
		
		this.updateDisplacement();
		
		return v(p);
	}

	@Override
	public double getEmitEnergy() {
		double dq = this.d - this.q;
		double e = this.energy - this.k * dq * dq;
		
		if (Math.random() < verbose)
			System.out.println("HarmonicAbsorber: Emit energy = " + e);
		
		return e;
	}

	@Override
	public Producer<Vector> getEmitPosition() { return this.getDisplacement(); }

	@Override
	public double getNextEmit() {
		if (Math.random() < HarmonicAbsorber.verbose)
			System.out.println("HarmonicAbsorber: D = " + this.d);
		
		if (this.d >= this.q)
			return 0.0;
		else
			return Integer.MAX_VALUE;
	}
	
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
}
