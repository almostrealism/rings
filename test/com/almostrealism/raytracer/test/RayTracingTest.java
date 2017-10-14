package com.almostrealism.raytracer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.texture.ImageCanvas;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.engine.RayIntersectionEngine;
import com.almostrealism.raytracer.engine.RayTracedScene;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.primitives.Sphere;

public class RayTracingTest {
	public static void main(String args[]) {
 		Scene<ShadableSurface> scene = new Scene<>();
		scene.add(new Sphere(new Vector(), 1.0, new RGB(0.8, 0.8, 0.8)));
		
		scene.addLight(new PointLight(new Vector(10.0, 10.0, -10.0), 0.8, new RGB(0.8, 0.9, 0.7)));

		PinholeCamera c = new PinholeCamera(new Vector(0.0, -1.0, -1.0),
											new Vector(0.0, 1.0, 1.0),
											new Vector(0.0, 1.0, 0.0));

		RenderParameters params = new RenderParameters();
		params.width = (int) (c.getProjectionWidth() * 1000);
		params.height = (int) (c.getProjectionHeight() * 1000);
		params.dx = (int) (c.getProjectionWidth() * 1000);
		params.dy = (int) (c.getProjectionHeight() * 1000);
		
		RayTracedScene r = new RayTracedScene(new RayIntersectionEngine(scene, params), c);
		
		try {
			ImageCanvas.encodeImageFile(r.realize(params).evaluate(null),
										new File("test.jpeg"),
										ImageCanvas.JPEGEncoding);
		} catch (FileNotFoundException fnf) {
			System.out.println("ERROR: Output file not found");
		} catch (IOException ioe) {
			System.out.println("IO ERROR");
		}

		System.exit(0);
	}
}
