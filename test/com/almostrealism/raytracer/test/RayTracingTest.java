package com.almostrealism.raytracer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.RefractionShader;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.io.FileDecoder;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.ImageCanvas;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.engine.RayIntersectionEngine;
import com.almostrealism.raytracer.engine.RayTracedScene;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.primitives.Sphere;
import org.almostrealism.texture.StripeTexture;

public class RayTracingTest {
	public static boolean useStripedFloor = true;
	public static boolean useCornellBox = false;
	public static boolean displaySpheres = true;
	public static boolean displayDragon = false;

	public static void main(String args[]) throws IOException {
 		Scene<ShadableSurface> scene = useCornellBox ?
				FileDecoder.decodeSceneFile(new File("CornellBox.xml"), FileDecoder.XMLEncoding,
											false, (e) -> { e.printStackTrace(); }) : new Scene<>();

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
 			scene.add(FileDecoder.decodeSurfaceFile(new File("dragon.ply"),
									FileDecoder.PLYEncoding, false, null));
		}

		Plane p = new Plane(Plane.XY);
		p.setLocation(new Vector(0, 0, 1));
		p.setShadeBack(true);
		p.setShadeFront(true);
//		scene.add(p);

		for (ShadableSurface s : scene) {
			if (s instanceof AbstractSurface) {
				((AbstractSurface) s).setShadeFront(true);
				((AbstractSurface) s).setShadeBack(true);
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

//		PinholeCamera c = new PinholeCamera(new Vector(0.0, -1.0, -1.0),
//											new Vector(0.0, 1.0, 1.0),
//											new Vector(0.0, 1.0, 0.0));
		PinholeCamera c = (PinholeCamera) scene.getCamera();
		c.setViewDirection(new Vector(0.0, -0.05, 1.0));
		c.setProjectionDimensions(50, 45);
		c.setFocalLength(400);
		Vector l = c.getLocation();
		l.setZ(-60);
		c.setLocation(l);

		RenderParameters params = new RenderParameters();
		params.width = (int) (c.getProjectionWidth() * 10);
		params.height = (int) (c.getProjectionHeight() * 10);
		params.dx = (int) (c.getProjectionWidth() * 10);
		params.dy = (int) (c.getProjectionHeight() * 10);
		
		RayTracedScene r = new RayTracedScene(new RayIntersectionEngine(scene, params), c);
		
		try {
			ColorProducer im[][] = r.realize(params).evaluate(null);
			ImageCanvas.encodeImageFile(im, new File("test.jpeg"),
										ImageCanvas.JPEGEncoding);
		} catch (FileNotFoundException fnf) {
			System.out.println("ERROR: Output file not found");
		} catch (IOException ioe) {
			System.out.println("IO ERROR");
		}

		System.exit(0);
	}
}
