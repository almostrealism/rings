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

/* * Copyright (C) 2006  Almost Realism Software Group
*
*  All rights reserved.
*  This document may not be reused without
*  express written permission from Mike Murray.
**/

package com.almostrealism.photonfield.test;

import java.io.IOException;
import java.lang.Math;

import javax.swing.JFrame;

import com.almostrealism.photonfield.AbsorberHashSet;
import com.almostrealism.photonfield.AbsorptionPlane;
import com.almostrealism.photonfield.Clock;
import com.almostrealism.photonfield.DefaultPhotonField;
import com.almostrealism.photonfield.PhotonField;
import com.almostrealism.photonfield.SpecularAbsorber;
import com.almostrealism.photonfield.geometry.Box;
import com.almostrealism.photonfield.geometry.Pinhole;
import com.almostrealism.photonfield.geometry.Plane;
import com.almostrealism.photonfield.light.PlanarLight;
import com.almostrealism.photonfield.util.PhysicalConstants;

/**
 * The TestRoom is a room which is based off the Cornell Box.
 * The website which this data comes from is
 * http://www.graphics.cornell.edu/online/box/data.html
 * 
 * @author Samuel Tepper
 */

public class TestRoom  implements PhysicalConstants  {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		double scale = 150.0;
		
		PlanarLight l = new PlanarLight();
		l.setWidth(130.0);
		l.setHeight(105.0);
		l.setSurfaceNormal(new double[] {0.0, -1.0, 0.0});
		l.setOrientation(new double[] {0.0, 0.0, 1.0});
		l.setPower(PlanarLight.wattsToEvMsec * 0.1);
		l.setLightPropagation(true);
		
		//TEST THESE PARAMETERS
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(.05);
		plane.setWidth(700);
		plane.setHeight(700);
		plane.setThickness(0.05);
		plane.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		//The focal length of the Cornell Box's lense is .035
		//The distance between the Pinhole and the AbsorbtionPlane is the focal length
		//The diameter of the pinhole is given by the equation
		// d = 1.9(sqrt(f*l)) where f is the focal length and l is the avergage wavelength
		Pinhole pinhole = new Pinhole();
		pinhole.setRadius(.5*1.9 * Math.sqrt(.035 * .580));
		pinhole.setThickness(0.05);
		pinhole.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		pinhole.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		Plane LeftWall = new Plane();
		SpecularAbsorber LW = new SpecularAbsorber();
		LeftWall.setHeight(550.0);
		LeftWall.setWidth(550.0);
		LeftWall.setOrientation(new double[] {0.0, 1.0, 0.0});
		LeftWall.setSurfaceNormal(new double[] {0.0, 0.0, 0.0});
		LW.setVolume(LeftWall);
		LW.setColorRange(620.0, 130.0);
		
		Plane RightWall = new Plane();
		SpecularAbsorber RW = new SpecularAbsorber();
		RightWall.setHeight(550.0);
		RightWall.setWidth(550.0);
		RightWall.setOrientation(new double[] {0.0, 1.0, 0.0});
		RightWall.setSurfaceNormal(new double[] {1.0, 0.0, 0.0});
		RW.setVolume(RightWall);
		RW.setColorRange(495, 75);
		
		Plane BackWall = new Plane();
		SpecularAbsorber BW = new SpecularAbsorber();
		BackWall.setHeight(550.0);
		BackWall.setWidth(550.0);
		BackWall.setOrientation(new double[] {0.0, 1.0, 0.0});
		BackWall.setSurfaceNormal(new double[] {0.0, 0.0, 1.0});
		BW.setVolume(BackWall);
		BW.setColorRange(570, 50);
		
		Plane Ceiling = new Plane();
		SpecularAbsorber CL = new SpecularAbsorber();
		Ceiling.setHeight(550.0);
		Ceiling.setWidth(550.0);
		Ceiling.setOrientation(new double[] {1.0, 0.0, 0.0});
		Ceiling.setSurfaceNormal(new double[] {0.0, -1.0, 0.0});
		CL.setVolume(Ceiling);
		CL.setColorRange(570, 50);
		
		Plane Floor = new Plane();
		SpecularAbsorber FL = new SpecularAbsorber();
		Floor.setHeight(550.0);
		Floor.setWidth(550.0);
		Floor.setOrientation(new double[] {1.0, 0.0, 0.0});
		Floor.setSurfaceNormal(new double[] {0.0, 1.0, 0.0});
		FL.setVolume(Floor);
		FL.setColorRange(570, 50);
		

		Box box1 = new Box();
		box1.setWidth(160.0);
		box1.setHeight(160.0);
		box1.setWidth(160.0);
		box1.setOrientation(new double[] {-Math.sin(80.0), 0.0, Math.cos(20.0)});
		box1.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		box1.setWallThickness(.05);
		box1.makeWalls(true);
		box1.setColorRange(450.0, 50.0);
		
		
		Box box2 = new Box();
		box2.setWidth(150.0);
		box2.setHeight(350.0);
		box2.setDepth(130.0);
		box2.setOrientation(new double[] {Math.sin(40.0), 0.0, Math.cos(40.0)});
		box2.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		box2.makeWalls(true);		
		box2.setColorRange(570.0, 50.0);
		
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(1000.0);
		a.addAbsorber(l, new double[] {275.0, 550.0, 275.0});
		a.addAbsorber(plane, new double[] {275.0, 275.0, 800.0});
		a.addAbsorber(pinhole, new double[] {275.0, 275.0, 799.65});
		a.addAbsorber(LW, new double[]{0.0, 275.0, 275.0});
		a.addAbsorber(RW, new double[] {550.0, 275.0, 275.0});
		a.addAbsorber(CL, new double[] {275.0, 550.0, 275.0});
		a.addAbsorber(FL, new double[] {275.0, 0.0, 275.0});
		a.addAbsorber(BW, new double[] {275.0, 275.0, 0.0});

		
		
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Specular Absorber Test");
		frame.getContentPane().add(plane.getDisplay());
		frame.setSize(150, 150);
		frame.setVisible(true);
		
		long start = System.currentTimeMillis();
		
		while (true) {
			c.tick();
			
			if (Math.random() < Box.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				try {
					plane.saveImage("specular-sim.jpg");
				} catch (IOException ioe) {
					System.out.println("BlackBody: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
		
		
	}

}
