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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.almostrealism.absorption.PinholeCameraAbsorber;
import com.almostrealism.chem.ElectronCloud;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.chem.Alloy;
import org.almostrealism.chem.PeriodicTable;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.physics.PhysicalConstants;
import org.almostrealism.physics.Clock;

import com.almostrealism.geometry.Sphere;
import com.almostrealism.light.LightBulb;

import static org.almostrealism.Ops.o;

/**
 * A {@link BlackBody} absorbs all photons it detects and keeps track of
 * a running total average flux.
 * 
 * @author  Michael Murray
 */
public class BlackBody implements Absorber, PhysicalConstants {
//	public static double verbose = 0.08;
	public static double verbose = 1.1;
	
	private static DecimalFormat format = new DecimalFormat("0.000E0");

	protected double energy;
	private Clock clock;
	
	public static void main(String args[]) throws FileNotFoundException {
		Clock c;

		try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(createScene()))) {
			c = (Clock) decoder.readObject();
		}

		PinholeCameraAbsorber camera = null;

		PhotonField f = c.getPhotonFields().iterator().next();
		AbsorberHashSet s = (AbsorberHashSet) f.getAbsorber();

		for (AbsorberHashSet.StoredItem item : s) {
			if (item.absorber instanceof PinholeCameraAbsorber) {
				camera = (PinholeCameraAbsorber) item.absorber;
			}
		}

		long start = System.currentTimeMillis();
		
		// Run the simulation and print out flux measurements every second
		while (true) {
			c.tick().get().run();
			
			if (Math.random() < BlackBody.verbose) {
//				int rate = (int) ((System.currentTimeMillis() - start) /
//									(60 * 60000 * c.getTime()));
//
//				System.out.println("[" + c.getTime() + " (" + rate +
//							" hours per microsecond)]: Flux is " +
//							format.format(b.getFlux() * BlackBody.evMsecToWatts)
//							+ " watts.");
//				System.out.println("Using t2.large, exposure would require $" + (rate * 0.0928) + " per microsecond.\n");
				
				try {
					camera.getAbsorptionPlane().saveImage("black-body-sim.jpg");
				} catch (IOException ioe) {
					System.out.println("BlackBody: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}

	public static String createScene() {
		System.out.println("BlackBody: Initializing simulation.");

		// Create a black body and confine it to a sphere
		// with a radius of one micrometer.
		BlackBody b = new BlackBody();
		VolumeAbsorber v = new VolumeAbsorber(new Sphere(500000), b);

		PinholeCameraAbsorber camera = new PinholeCameraAbsorber(2.4, 35000,
				Vector.negZAxis(), Vector.yAxis());
		camera.setPixelSize(10);
		camera.setWidth(500);
		camera.setHeight(500);

		// Create a light bulb
		LightBulb l = new LightBulb();
		l.setPower(LightBulb.wattsToEvMsec * 10);

		// Add black body and light bulb to absorber set
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(100000000);
		a.addAbsorber(v, o().vector(500000.0, 0.0, 0.0)); a.setColorBufferDimensions(1, 1, 1.0);
		a.addAbsorber(l, o().vector(0.0, 500000.0, 0.0)); a.setColorBufferDimensions(1, 1, 1.0);

		// Add the absorption plane
		a.addAbsorber(camera, o().vector(0.0, 0.0, 1000000));
//		a.setColorBufferDimensions((int) (camera.getWidth() / camera.getPixelSize()),
//				(int) (camera.getHeight() / camera.getPixelSize()),
//				1.0);
		a.setColorBufferDimensions(camera.getWidth(), camera.getHeight(), 1.0);

		// Add some atoms
		ElectronCloud ec = new ElectronCloud(
				new Alloy(Arrays.asList(PeriodicTable.Gold), 1.0), 10);
		v = new VolumeAbsorber(new Sphere(1000.0), ec);
		a.addAbsorber(v, ZeroVector.getInstance());

		// Add a SpectralLineDiagram
		SpectralLineDiagram d = new SpectralLineDiagram(1200, 40);
		v = new VolumeAbsorber(new Sphere(1000.0), d);
		a.addAbsorber(v, o().vector(0.0, 0.0, -1000000.0));

		// Create photon field and set absorber to the absorber set
		// containing the black body and the light bulb
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);

		// Create a clock and add the photon field
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);

		System.out.println("BlackBody: Writing xml...");

		String output = "BlackBody.xml";

		try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(output))) {
			encoder.writeObject(c);
			encoder.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("BlackBody: XML written");

		return output;
	}

	@Override
	public boolean absorb(Vector x, Vector p, double energy) {
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

	@Override
	public Producer<Vector> emit() { return null; }

	@Override
	public double getEmitEnergy() { return 0; }

	@Override
	public Producer<Vector> getEmitPosition() { return null; }

	@Override
	public double getNextEmit() { return Integer.MAX_VALUE; }

	@Override
	public void setClock(Clock c) { this.clock = c; }

	@Override
	public Clock getClock() { return this.clock; }
}
