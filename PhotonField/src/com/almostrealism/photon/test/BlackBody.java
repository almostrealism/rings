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

package com.almostrealism.photon.test;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JFrame;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.AbsorberHashSet;
import com.almostrealism.photon.AbsorptionPlane;
import com.almostrealism.photon.Clock;
import com.almostrealism.photon.DefaultPhotonField;
import com.almostrealism.photon.PhotonField;
import com.almostrealism.photon.VolumeAbsorber;
import com.almostrealism.photon.geometry.Sphere;
import com.almostrealism.photon.light.LightBulb;
import com.almostrealism.photon.util.PhysicalConstants;

/**
 * A BlackBody absorbs all photons it detects and keeps track of a
 * running total average flux.
 * 
 * @author  Mike Murray
 */
public class BlackBody implements Absorber, PhysicalConstants {
	public static double verbose = 0.001;
	
	private static DecimalFormat format = new DecimalFormat("0.000E0");
	
	private static double r = Math.pow(10.0, 0.0);
	private static double p[] = {r * 1.05, 0.0, 0.0};
	
	protected double energy;
	private Clock clock;
	
	public static void main(String args[]) {
		System.out.println("BlackBody: Initializing simulation.");
		
		// Create a black body and confine it to a sphere
		// with a radius of one micrometer.
		BlackBody b = new BlackBody();
		VolumeAbsorber v = new VolumeAbsorber(new Sphere(1.0), b);
		
		// Create an AbsorptionPlane to display radiation that is
		// not absorbed by the black body sphere.
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(Math.pow(10.0, -1.0)); // Each pixel is a 100 square nanometers
		plane.setWidth(500);  // Width = 50 micrometers
		plane.setHeight(500); // Height = 50 micrometers
		plane.setThickness(1); // One micrometer thick
		
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
		a.addAbsorber(v, p);
		a.addAbsorber(l, new double[] {0.0, 0.0, 0.0});
		a.addAbsorber(plane, new double[] {2.5 * Math.pow(10.0, 0.0), 0.0, 0.0});
		
		// Create photon field and set absorber to the absorber set
		// containing the black body and the light bulb
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		// Create a clock and add the photon field
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Black Body Test");
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
				
				System.out.println("[" + c.getTime() + " (" + rate +
							" hours per microsecond)]: Flux is " +
							format.format(b.getFlux() * BlackBody.evMsecToWatts)
							+ " watts.");
				
				try {
					plane.saveImage("black-body-sim.jpg");
				} catch (IOException ioe) {
					System.out.println("BlackBody: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}
	
	public boolean absorb(double[] x, double[] p, double energy) {
		this.energy += energy;
		return true;
	}
	
	/**
	 * Returns the running total average energy (eV) per microsecond absorbed
	 * by this BlackBody. This can be converted to watts by multiplying the value
	 * by BlockBody.evMsecToWatts.
	 */
	public double getFlux() {
		if (this.clock == null) return 0.0;
		return this.energy / this.clock.getTime();
	}
	
	public double[] emit() { return null; }
	public double getEmitEnergy() { return 0; }
	public double[] getEmitPosition() { return null; }
	public double getNextEmit() { return Integer.MAX_VALUE; }
	
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
}
