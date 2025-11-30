package com.almostrealism.physics;

import com.almostrealism.light.LightBulb;
import org.almostrealism.algebra.VectorFeatures;
import org.almostrealism.physics.Clock;
import org.almostrealism.physics.HarmonicAbsorber;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.primitives.AbsorptionPlane;

import javax.swing.*;
import java.io.IOException;

public class HarmonicAbsorberExperiment {
	public static double verbose = Math.pow(10.0, -3.0);

	public static void main(String[] args) {
		VectorFeatures o = VectorFeatures.getInstance();

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
		plane.setSurfaceNormal(o.vector(-1.0, 0.0, 0.0));
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});

		// Create a light bulb
		LightBulb l = new LightBulb();
		l.setPower(LightBulb.wattsToEvMsec * 0.01);

		// Add black body and light bulb to absorber set
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(3.0 * Math.pow(10.0, 1.0));
		a.addAbsorber(b, o.vector(0.5, 0.0, 0.0));
		a.addAbsorber(l, o.vector(-1.0, 0.0, 0.0));
		a.addAbsorber(plane, o.vector(2.5, 0.0, 0.0));

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

			if (Math.random() < verbose) {
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
}
