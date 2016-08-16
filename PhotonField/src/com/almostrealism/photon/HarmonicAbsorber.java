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

package com.almostrealism.photon;

import java.io.IOException;

import javax.swing.JFrame;

import org.almostrealism.space.VectorMath;

import com.almostrealism.photon.light.LightBulb;
import com.almostrealism.photon.test.BlackBody;

/**
 * A HarmonicAbsorber object represents a spherical absorber that stores
 * energy proportional to the square of the displacement vector.
 * 
 * @author Mike Murray
 */
public class HarmonicAbsorber implements SphericalAbsorber {
	public static double verbose = Math.pow(10.0, -3.0);
	
	private Clock clock;
	private double energy, radius, k, q, d, dp;
	private double place[];
	
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
		plane.setSurfaceNormal(new double[] {-1.0, 0.0, 0.0});
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		// Create a light bulb
		LightBulb l = new LightBulb();
		l.setPower(LightBulb.wattsToEvMsec * 0.01);
		
		// Add black body and light bulb to absorber set
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(3.0 * Math.pow(10.0, 1.0));
		a.addAbsorber(b, new double[] {0.5, 0.0, 0.0});
		a.addAbsorber(l, new double[] {-1.0, 0.0, 0.0});
		a.addAbsorber(plane, new double[] {2.5, 0.0, 0.0});
		
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
			c.tick();
			
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
		this.place = new double[] {0.0, 0.0, 0.0};
	}
	
	public void setPotentialMap(PotentialMap m) { }
	public PotentialMap getPotentialMap() { return null; }
	
	public void setRigidity(double k) { this.k = k; }
	public double getRigidity() { return this.k; }
	
	public void setRadius(double r) { this.radius = r; }
	public double getRadius() { return this.radius; }
	
	public void setQuanta(double q) { this.q = q; }
	public double getQuanta() { return this.q; }
	
	public double[] getDisplacement() {
		return VectorMath.multiply(this.place, this.dp, true);
	}
	
	protected void updateDisplacement() {
		this.d = this.radius * Math.sqrt(this.energy / this.k);
		this.dp = this.d / VectorMath.length(this.place);
	}
	
	public boolean absorb(double[] x, double[] p, double energy) {
		if (VectorMath.length(x) > this.radius) return false;
		
		if (Math.random() < verbose)
			System.out.println("HarmonicAbsorber: Absorb energy = " + energy);
		
		VectorMath.addTo(this.place, p);
		this.energy += energy;
		
		this.updateDisplacement();
		
		return true;
	}
	
	public double[] emit() {
		double e = this.getEmitEnergy();
		this.energy -= e;
		
		double pd = VectorMath.length(this.place);
		double p[] = VectorMath.multiply(this.place, 1.0 / pd, true);
		
		this.updateDisplacement();
		
		return p;
	}
	
	public double getEmitEnergy() {
		double dq = this.d - this.q;
		double e = this.energy - this.k * dq * dq;
		
		if (Math.random() < verbose)
			System.out.println("HarmonicAbsorber: Emit energy = " + e);
		
		return e;
	}
	
	public double[] getEmitPosition() { return this.getDisplacement(); }
	
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
