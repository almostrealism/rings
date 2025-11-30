package com.almostrealism.physics;

import com.almostrealism.geometry.Sphere;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.chem.Alloy;
import org.almostrealism.chem.ElectronCloud;
import org.almostrealism.chem.PeriodicTable;
import org.almostrealism.chem.SpectralLineDiagram;
import org.almostrealism.light.LightBulb;
import org.almostrealism.physics.BlackBody;
import org.almostrealism.physics.Clock;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.physics.VolumeAbsorber;
import org.almostrealism.primitives.PinholeCameraAbsorber;
import org.almostrealism.raytrace.AbsorberHashSet;
import org.almostrealism.raytrace.DefaultPhotonField;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.almostrealism.Ops.o;

public class BlackBodyExperiment {
	public static void main(String[] args) throws FileNotFoundException {
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
				new Alloy(List.of(PeriodicTable.Gold), 1.0), 10);
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
}
