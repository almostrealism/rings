package com.almostrealism.raytracer.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.almostrealism.gl.SurfaceCanvas;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.RefractionShader;
import com.almostrealism.FogParameters;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.io.FileDecoder;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.ImageCanvas;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.RayIntersectionEngine;
import com.almostrealism.raytracer.RayTracedScene;
import com.almostrealism.RenderParameters;
import com.almostrealism.primitives.Sphere;
import org.almostrealism.texture.StripeTexture;

public class RayTracingTest {
	public static boolean useStripedFloor = true;
	public static boolean useCornellBox = false;
	public static boolean displaySpheres = true;
	public static boolean displayDragon = false;

	public static RayTracedScene generateScene() throws IOException {
		Scene<ShadableSurface> scene = useCornellBox ?
				FileDecoder.decodeScene(new FileInputStream(new File("CornellBox.xml")),
						FileDecoder.XMLEncoding,
						false, (e) -> e.printStackTrace()) : new Scene<>();

		if (useStripedFloor) {
			Plane p = new Plane(Plane.XZ);
			p.setLocation(new Vector(0.0, -10, 0.0));
			p.addTexture(new StripeTexture());
			scene.add(p);
		}

		if (displaySpheres) {
			Sphere s1 = new Sphere(new Vector(-1.0, -2.25, -2), 0.8, new RGB(0.3, 0.3, 0.3));
			s1.getShaderSet().clear();
			s1.addShader(new RefractionShader());

			Sphere s2 = new Sphere(new Vector(1.0, -2.25, -2), 0.8, new RGB(0.3, 0.3, 0.3));
			s2.addShader(new ReflectionShader(0.8, new RGB(0.8, 0.8, 0.8)));
			s2.getShaderSet().clear();

			scene.add(s1);
			scene.add(s2);
		}

		if (displayDragon) {
			scene.add(((Scene<ShadableSurface>) FileDecoder.decodeScene(new FileInputStream(new File("dragon.ply")),
					FileDecoder.PLYEncoding, false, null)).get(0));
		}

		Plane p = new Plane(Plane.XY);
		p.setLocation(new Vector(0, 0, 1));
//		scene.add(p);

		for (ShadableSurface s : scene) {
			if (s instanceof AbstractSurface) {
				((AbstractSurface) s).setShadeFront(true);
				((AbstractSurface) s).setShadeBack(true);
				if (((AbstractSurface) s).getShaderSet().size() <= 0)
					((AbstractSurface) s).addShader(new DiffuseShader());
			}

			if (s instanceof Plane) {
				((Plane) s).setColor(new RGB(1.0, 1.0, 1.0));
				if (((Plane) s).getType() == Plane.XZ) {
					if (((Plane) s).getLocation().getX() <= 0) {
						System.out.println(((Plane) s).getLocation());
					}
				}
			}
		}

		scene.addLight(new PointLight(new Vector(00.0, 10.0, -1.0), 1.0, new RGB(0.8, 0.9, 0.7)));

		PinholeCamera c = (PinholeCamera) scene.getCamera();
		if (c == null) {
			c = new PinholeCamera(new Vector(0.0, -1.0, -1.0),
					new Vector(0.0, 1.0, 1.0),
					new Vector(0.0, 1.0, 0.0));
			scene.setCamera(c);
		}

		c.setViewDirection(new Vector(0.0, -0.05, 1.0));
		c.setProjectionDimensions(50, 45);
		c.setFocalLength(400);
		Vector l = c.getLocation();
		l.setZ(-60);
		c.setLocation(l);

		new SurfaceCanvas(scene).autoPositionCamera();

		RenderParameters params = new RenderParameters();
		params.width = (int) (c.getProjectionWidth() * 10);
		params.height = (int) (c.getProjectionHeight() * 10);
		params.dx = (int) (c.getProjectionWidth() * 10);
		params.dy = (int) (c.getProjectionHeight() * 10);

		return new RayTracedScene(new RayIntersectionEngine(scene, new FogParameters()), c, params);
	}

	public static void main(String args[]) throws IOException {
 		RayTracedScene r = generateScene();

		RealizableImage img = r.realize(r.getRenderParameters());

		while (!img.isComplete()) {
			try {
				ColorProducer im[][] = img.evaluate(new Object[0]);
				ImageCanvas.encodeImageFile(im, new File("test.jpeg"),
						ImageCanvas.JPEGEncoding);
				System.out.println("Wrote image (" + (img.getCompleted() * 100) + "%)");
			} catch (FileNotFoundException fnf) {
				System.out.println("ERROR: Output file not found");
			} catch (IOException ioe) {
				System.out.println("IO ERROR");
			}

			try {
				Thread.sleep(10000); // Wait 10 seconds before trying again
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}
}
