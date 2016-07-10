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

package com.almostrealism.photonfield.test;

import java.io.IOException;

import javax.swing.JFrame;

import com.almostrealism.photonfield.AbsorberHashSet;
import com.almostrealism.photonfield.AbsorptionPlane;
import com.almostrealism.photonfield.Clock;
import com.almostrealism.photonfield.DefaultPhotonField;
import com.almostrealism.photonfield.PhotonField;
import com.almostrealism.photonfield.VolumeAbsorber;
import com.almostrealism.photonfield.geometry.Sphere;
import com.almostrealism.photonfield.light.LightBulb;
import com.almostrealism.photonfield.util.VectorMath;

public class RandomLights {
	public static double verbose = 0.01;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(1.0 * Math.pow(10.0, 1.0));
		
		for (int i = 0; i < 4; i++) {
			double r = Math.random();
			
			BlackBody b = new BlackBody();
			VolumeAbsorber v = new VolumeAbsorber(new Sphere(r), b);
			
			LightBulb l = new LightBulb();
			l.setPower(LightBulb.wattsToEvMsec * 0.005 * Math.random());
			
			double p[] = {Math.random() * 400 - 200,
					Math.random() * 400 - 200,
					3 * Math.random() - 1.5};
			
			a.addAbsorber(v, p);
			a.addAbsorber(l, VectorMath.subtract(p, new double[] {0.0, 0.0, r * 1.1}));
		}
		
		// Create an AbsorptionPlane to display radiation that is
		// not absorbed by the black body sphere.
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(Math.pow(10.0, 0.0)); // Each pixel is a 100 square nanometers
		plane.setWidth(400);  // Width = 50 micrometers
		plane.setHeight(400); // Height = 50 micrometers
		plane.setThickness(1); // One micrometer thick
		// Facing the negative X direction and oriented so
		// that the positive Y axis is "upward".
		plane.setSurfaceNormal(new double[] {-1.0, 0.0, 0.0});
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		a.addAbsorber(plane, new double[] {5.0, 0.0, 0.0});
		
		// Create photon field and set absorber to the absorber set
		// containing the black body and the light bulb
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		// Create a clock and add the photon field
		Clock c = new Clock();
		c.setTickInterval(Math.pow(10.0, -9.0));
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Random");
		frame.getContentPane().add(plane.getDisplay());
		frame.setSize(150, 150);
		frame.setVisible(true);
		
		long start = System.currentTimeMillis();
		
		System.out.println("RandomLights: Started at " + start);
		
		while (true) {
			c.tick();
			
			if (Math.random() < RandomLights.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				try {
					plane.saveImage("random.ppm");
				} catch (IOException ioe) {
					System.out.println("RandomLights: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}

}
