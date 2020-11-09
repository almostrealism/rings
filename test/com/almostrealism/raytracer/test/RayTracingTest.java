package com.almostrealism.raytracer.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.RefractionShader;
import com.almostrealism.FogParameters;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.graph.mesh.Mesh;
import org.almostrealism.io.FileDecoder;
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
	public static boolean waitUntilComplete = false;
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
			p.setShadeBack(true);
			p.setColor(new RGB(1.0, 1.0, 1.0));
			p.getShaderSet().add(new DiffuseShader());
			p.setLocation(new Vector(0.0, -0.3, 0.0));
			StripeTexture t = new StripeTexture();
			t.setPropertyValue(2.5, 0);
			t.setPropertyValue(true, 1);
			p.addTexture(t);
			scene.add(p);

			p = new Plane(Plane.YZ);
			p.setShadeBack(true);
			p.setColor(new RGB(1.0, 0.7, 0.0));
			p.getShaderSet().add(new DiffuseShader());
			p.setLocation(new Vector(-3.0, 0.0, 0.0));
			scene.add(p);

			p = new Plane(Plane.YZ);
			p.setShadeBack(true);
			p.setColor(new RGB(1.0, 0.0, 0.7));
			p.getShaderSet().add(new DiffuseShader());
			p.setLocation(new Vector(3.0, 0.0, 0.0));
			scene.add(p);
		}

		if (displaySpheres) {
			/* Sphere 1 */
			Sphere s1 = new Sphere(new Vector(-0.3, 0.0, 0), 0.3, new RGB(0.3, 0.3, 0.3));

			/* Shaders */
			s1.addShader(new DiffuseShader());
			s1.addShader(new ReflectionShader(1.0, RGBWhite.getProducer()));


			/* Sphere 2 */
//			Sphere s2 = new Sphere(new Vector(0.3, 0, -2), 0.15, new RGB(0.3, 0.4, 0.6));
			Sphere s2 = new Sphere(new Vector(0.3, 0.0, 0), 0.3, new RGB(0.3, 0.4, 0.6));

			/* Shaders */
			RefractionShader r = new RefractionShader();
			r.setIndexOfRefraction(1.5);
//			s2.addShader(r);
			s2.addShader(new DiffuseShader());

			scene.add(s1);
			scene.add(s2);
		}

		if (displayDragon) {
			Mesh dragon = (Mesh) ((Scene<ShadableSurface>) FileDecoder.decodeScene(new FileInputStream(new File("resources/dragon.ply")),
					FileDecoder.PLYEncoding, false, null)).get(0);
			dragon.getShaderSet().add(new DiffuseShader());
			dragon.setColor(new RGB(0.3, 0.4, 0.9));
			scene.add(dragon);
		}

		Plane p = new Plane(Plane.XY);
		p.setColor(new RGB(0.5, 0.8, 0.7));
		p.setLocation(new Vector(0, 0, 10));
		p.getShaderSet().add(new DiffuseShader());
		p.setShadeBack(true);
		scene.add(p);

//		for (ShadableSurface s : scene) {
//			if (s instanceof AbstractSurface) {
//				((AbstractSurface) s).setShadeFront(true);
//				((AbstractSurface) s).setShadeBack(true);
//				if (((AbstractSurface) s).getShaderSet().size() <= 0)
//					((AbstractSurface) s).addShader(new DiffuseShader());
//			}
//
//			if (s instanceof Plane) {
//				((Plane) s).setColor(new RGB(1.0, 1.0, 1.0));
//				if (((Plane) s).getType() == Plane.XZ) {
//					if (((Plane) s).getLocation().getX() <= 0) {
//						System.out.println(((Plane) s).getLocation());
//					}
//				}
//			}
//		}

		// scene.addLight(new PointLight(new Vector(0.0, 10.0, -1.0), 0.5, new RGB(0.8, 0.9, 0.7)));
		scene.addLight(new PointLight(new Vector(0.0, 0.0, -2.0), 1.0, new RGB(0.8, 0.8, 0.8)));

		PinholeCamera c = (PinholeCamera) scene.getCamera();
		if (c == null) {
			c = new PinholeCamera(new Vector(0.0, 0.5, -2.0),
					new Vector(0.0, 0.0, 1.0),
					new Vector(0.0, 1.0, 0.0));
			scene.setCamera(c);
		}

		c.setViewDirection(new Vector(0.0, -0.05, 1.0));
		c.setProjectionDimensions(500, 450);
		c.setFocalLength(400);

//		new SurfaceCanvas(scene).autoPositionCamera();

		double scale = 0.5;

		RenderParameters params = new RenderParameters();
		params.width = (int) (c.getProjectionWidth() * scale);
		params.height = (int) (c.getProjectionHeight() * scale);
		params.dx = (int) (c.getProjectionWidth() * scale);
		params.dy = (int) (c.getProjectionHeight() * scale);

		return new RayTracedScene(new RayIntersectionEngine(scene, new FogParameters()), c, params);
	}

	public static RealizableImage generateImage() throws IOException {
		RayTracedScene r = generateScene();
		return r.realize(r.getRenderParameters());
	}

	public static void main(String args[]) throws IOException {
		RealizableImage img = generateImage();

		try {
			ImageCanvas.encodeImageFile(img, new File("test.jpeg"),
						ImageCanvas.JPEGEncoding);
			System.out.println("Wrote image");
		} catch (FileNotFoundException fnf) {
			System.out.println("ERROR: Output file not found");
		} catch (IOException ioe) {
			System.out.println("IO ERROR");
		}
	}
}
