package com.almostrealism.raytracer.test;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytrace.FogParameters;
import com.almostrealism.raytrace.RayIntersectionEngine;
import com.almostrealism.raytrace.RenderParameters;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.raytracer.RayTracedScene;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.geometry.Ray;
import org.almostrealism.primitives.Sphere;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;

/**
 * Simple rendering tests to verify the ray tracing pipeline can produce output.
 */
public class SimpleRenderTest implements TestFeatures {

	@Test
	public void testShaderIsolated() {
		log("Testing shader/lighting calculation in isolation...");

		// Enable shader diagnostics
		com.almostrealism.rayshade.DiffuseShader.produceOutput = true;

		// Create sphere at origin with white diffuse color
		Sphere sphere = new Sphere();
		sphere.setLocation(new Vector(0.0, 0.0, 0.0));
		sphere.setSize(1.0);
		sphere.setColor(new RGB(1.0, 1.0, 1.0)); // White surface
		((AbstractSurface) sphere).setShaders(new org.almostrealism.color.Shader[] {
			DiffuseShader.defaultDiffuseShader
		});
		log("Created white sphere at origin, radius 1.0");

		// Create light source at (5, 5, 5) - above and to the side
		PointLight light = new PointLight(new Vector(5.0, 5.0, 5.0));
		light.setColor(new RGB(1.0, 1.0, 1.0));
		light.setIntensity(1.0);
		log("Created white light at (5, 5, 5)");

		// Create a camera ray from (0, 0, 10) pointing towards sphere at (0, 0, -1)
		// This should hit the sphere at approximately (0, 0, 1) on the front surface
		Producer<org.almostrealism.geometry.Ray> testRay = ray(0.0, 0.0, 10.0, 0.0, 0.0, -1.0);
		log("Created ray: origin (0, 0, 10), direction (0, 0, -1)");

		// Create shader context
		org.almostrealism.color.ShaderContext context = new org.almostrealism.color.ShaderContext(sphere, light);
		log("Created shader context");

		// Create lighting engine directly
		com.almostrealism.raytrace.IntersectionalLightingEngine engine =
			new com.almostrealism.raytrace.IntersectionalLightingEngine(
				testRay, sphere, java.util.Collections.emptyList(), light, java.util.Collections.emptyList(), context);
		log("Created IntersectionalLightingEngine");

		// Get the color producer
		Producer<RGB> colorProducer = engine.getProducer();
		log("Got color producer: " + colorProducer);

		// Evaluate with a pixel position argument (doesn't matter which, just need to match expected args)
		org.almostrealism.algebra.Pair pixelPos = new org.almostrealism.algebra.Pair(32.0, 32.0);
		Object result = colorProducer.get().evaluate(pixelPos);

		// Handle PackedCollection -> RGB conversion
		RGB color;
		if (result instanceof RGB) {
			color = (RGB) result;
		} else if (result instanceof org.almostrealism.collect.PackedCollection) {
			color = new RGB((org.almostrealism.collect.PackedCollection<?>) result, 0);
		} else {
			throw new IllegalStateException("Unexpected result type: " + result.getClass().getName());
		}

		log("Color evaluated: RGB(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");

		// Check that color is non-black
		boolean isNonBlack = color.getRed() > 0.01 || color.getGreen() > 0.01 || color.getBlue() > 0.01;
		log("Is non-black: " + isNonBlack);

		assertTrue("Shader should produce non-black color for lit sphere", isNonBlack);
	}

	@Test
	public void debugShaderInputs() {
		log("Testing intermediate shader values...");

		// Create sphere at origin
		Sphere sphere = new Sphere();
		sphere.setLocation(new Vector(0.0, 0.0, 0.0));
		sphere.setSize(1.0);
		sphere.setColor(new RGB(1.0, 1.0, 1.0)); // White surface

		// Create ray from (0, 0, 10) pointing towards sphere
		Producer<Ray> testRay = ray(0.0, 0.0, 10.0, 0.0, 0.0, -1.0);

		// Get intersection
		org.almostrealism.geometry.ShadableIntersection intersection = sphere.intersectAt(testRay);

		// Evaluate intersection point
		Producer<Ray> intersectionRay = intersection.get(0);
		Ray intersectionData = new Ray(intersectionRay.get().evaluate(), 0);
		log("Intersection origin: (" + intersectionData.getOrigin().getX() + ", " +
			intersectionData.getOrigin().getY() + ", " + intersectionData.getOrigin().getZ() + ")");
		log("Expected: around (0, 0, 1) - front of sphere");

		// Get surface normal at intersection
		Producer<Vector> normalVector = intersection.getNormalAt(c(0.0));
		Object normalResult = normalVector.get().evaluate();
		Vector normal;
		if (normalResult instanceof Vector) {
			normal = (Vector) normalResult;
		} else if (normalResult instanceof org.almostrealism.collect.PackedCollection) {
			normal = new Vector((org.almostrealism.collect.PackedCollection<?>) normalResult, 0);
		} else {
			throw new IllegalStateException("Unexpected normal type: " + normalResult.getClass().getName());
		}
		log("Surface normal: (" + normal.getX() + ", " + normal.getY() + ", " + normal.getZ() + ")");
		log("Expected: pointing outward from sphere, roughly (0, 0, 1)");

		// Create light at (5, 5, 5)
		PointLight light = new PointLight(new Vector(5.0, 5.0, 5.0));
		light.setColor(new RGB(1.0, 1.0, 1.0));
		light.setIntensity(1.0);

		// Evaluate surface color
		Producer<RGB> surfaceColorProducer = sphere.getValueAt(origin(intersectionRay));
		RGB surfaceColor = surfaceColorProducer.get().evaluate();
		log("Surface color: RGB(" + surfaceColor.getRed() + ", " + surfaceColor.getGreen() + ", " + surfaceColor.getBlue() + ")");
		log("Expected: RGB(1.0, 1.0, 1.0) - white");

		// Evaluate light color at intersection point
		Producer<RGB> lightColorProducer = light.getColorAt(origin(intersectionRay));
		RGB lightColor = lightColorProducer.get().evaluate();
		log("Light color at point: RGB(" + lightColor.getRed() + ", " + lightColor.getGreen() + ", " + lightColor.getBlue() + ")");
		log("Expected: non-black");

		// Manually compute light direction (from point to light)
		Vector intersectionPoint = intersectionData.getOrigin();
		Vector lightPos = light.getLocation();
		Vector toLight = lightPos.subtract(intersectionPoint);
		Vector toLightNorm = toLight.divide(toLight.length());
		log("Light direction (point to light): (" + toLightNorm.getX() + ", " + toLightNorm.getY() + ", " + toLightNorm.getZ() + ")");

		// Compute dot product manually
		double dotProduct = normal.dotProduct(toLightNorm);
		log("Dot product (normal dot light): " + dotProduct);
		log("Expected: positive value if light is visible from surface");

		assertTrue("Dot product should be positive (surface facing light)", dotProduct > 0);
		assertTrue("Surface color should be non-black", surfaceColor.getRed() > 0.01 || surfaceColor.getGreen() > 0.01 || surfaceColor.getBlue() > 0.01);
		assertTrue("Light color should be non-black", lightColor.getRed() > 0.01 || lightColor.getGreen() > 0.01 || lightColor.getBlue() > 0.01);
	}

	@Test
	public void debugIntersection() {
		log("Testing direct sphere intersection...");

		// Create sphere at origin
		Sphere sphere = new Sphere();
		sphere.setLocation(new Vector(0.0, 0.0, 0.0));
		sphere.setSize(1.0);

		// Create a ray from camera position (0, 0, 10) pointing towards sphere (0, 0, -1)
		Producer<Ray> testRay = ray(0.0, 0.0, 10.0, 0.0, 0.0, -1.0);

		log("Ray created: origin (0, 0, 10), direction (0, 0, -1)");
		log("Sphere at origin (0, 0, 0), size 1.0");

		// Get intersection
		org.almostrealism.geometry.ShadableIntersection intersection = sphere.intersectAt(testRay);

		// Evaluate the distance
		org.almostrealism.collect.PackedCollection<?> distance = intersection.getDistance().get().evaluate();

		log("Distance value: " + distance.toDouble(0));
		log("Expected: around 9.0 (10 - radius of 1)");

		double distVal = distance.toDouble(0);
		assertTrue("Distance should be positive (ray hits sphere)", distVal > 0);
		assertTrue("Distance should be ~9.0 (was " + distVal + ")", Math.abs(distVal - 9.0) < 0.1);
	}

	@Test
	public void debugCameraRay() {
		log("Testing camera ray generation...");

		// Create camera same as in renderSingleSphere
		PinholeCamera camera = new PinholeCamera();
		camera.setLocation(new Vector(0.0, 0.0, 10.0));
		camera.setViewDirection(new Vector(0.0, 0.0, -1.0));
		camera.setFocalLength(0.05);
		camera.setProjectionDimensions(0.1, 0.1);

		log("Camera at (0, 0, 10), looking at (0, 0, -1)");
		log("Focal length: 0.05, projection: 0.1x0.1");

		// Generate ray for center pixel using 64x64 screen (same as renderSingleSphere)
		// Center pixel is at (32, 32) in a 64x64 grid
		Producer<org.almostrealism.algebra.Pair<?>> centerPos = pair(32.0, 32.0);
		Producer<org.almostrealism.algebra.Pair<?>> screenDim = pair(64, 64);

		Producer<Ray> cameraRay = camera.rayAt(centerPos, screenDim);

		// Evaluate the ray - use Ray view wrapper
		Ray ray = new Ray(cameraRay.get().evaluate(), 0);

		log("Camera ray origin: (" + ray.getOrigin().getX() + ", " + ray.getOrigin().getY() + ", " + ray.getOrigin().getZ() + ")");
		log("Camera ray direction: (" + ray.getDirection().getX() + ", " + ray.getDirection().getY() + ", " + ray.getDirection().getZ() + ")");
		log("Expected: origin (0, 0, 10), direction pointing towards -Z");

		// Verify direction is pointing towards -Z (doesn't need to be normalized for intersection)
		assertTrue("Direction Z should be negative (pointing towards sphere)", ray.getDirection().getZ() < 0);

		// Now test intersection with this camera ray
		Sphere sphere = new Sphere();
		sphere.setLocation(new Vector(0.0, 0.0, 0.0));
		sphere.setSize(1.0);

		org.almostrealism.geometry.ShadableIntersection intersection = sphere.intersectAt(cameraRay);
		org.almostrealism.collect.PackedCollection<?> distance = intersection.getDistance().get().evaluate();

		log("Intersection distance from camera ray: " + distance.toDouble(0));
		double dist = distance.toDouble(0);
		assertTrue("Ray should hit sphere (distance > 0)", dist > 0);
		log("Camera ray correctly hits sphere at distance " + dist);
	}

	@Test
	public void renderSingleSphere() throws Exception {
		log("Creating simple scene with one sphere...");

		// Enable verbose logging to see what's happening during rendering
		com.almostrealism.raytrace.LightingEngineAggregator.enableVerbose = true;

		// Create scene
		Scene<ShadableSurface> scene = new Scene<>();

		// Create a sphere
		Sphere sphere = new Sphere();
		sphere.setLocation(new Vector(0.0, 0.0, 0.0));
		sphere.setSize(1.0);
		sphere.setColor(new RGB(0.8, 0.2, 0.2)); // Red sphere
		((AbstractSurface) sphere).setShaders(new org.almostrealism.color.Shader[] {
			DiffuseShader.defaultDiffuseShader
		});

		// Ensure transform is calculated before adding to scene
		sphere.calculateTransform();
		log("Sphere transform calculated: " + (sphere.getTransform() != null));

		// Temporarily disable transform to debug intersection issue
		org.almostrealism.primitives.Sphere.enableTransform = false;

		scene.add(sphere);

		// Add a point light
		PointLight light = new PointLight(new Vector(5.0, 5.0, 10.0));
		scene.addLight(light);

		log("Scene created with sphere and light");

		// Create camera
		try {
			PinholeCamera camera = new PinholeCamera();
			camera.setLocation(new Vector(0.0, 0.0, 10.0));
			camera.setViewDirection(new Vector(0.0, 0.0, -1.0));
			camera.setFocalLength(0.05);
			camera.setProjectionDimensions(0.04, 0.04);  // Zoomed in from 0.1

			scene.setCamera(camera);
			log("Camera created and configured");

			// Create render parameters for a small image
			RenderParameters params = new RenderParameters();
			params.width = 64;   // Small for fast test
			params.height = 64;
			params.dx = 64;
			params.dy = 64;
			params.ssWidth = 1;  // No supersampling
			params.ssHeight = 1;

			log("Render parameters: 64x64, no supersampling");

			// Create ray traced scene
			RayTracedScene rayTracedScene = new RayTracedScene(
				new RayIntersectionEngine(scene, new FogParameters()),
				camera,
				params
			);

			log("Starting render...");

			// Render the image
			RealizableImage realizableImage = rayTracedScene.realize(params);

			log("Evaluating image...");

			// Evaluate to get RGB data
			RGB[][] imageData = realizableImage.get().evaluate();

			log("Image evaluated successfully!");
			log("Image size: " + imageData.length + "x" + imageData[0].length);

			// Check that we got some non-black pixels
			int nonBlackPixels = 0;
			for (int x = 0; x < imageData.length; x++) {
				for (int y = 0; y < imageData[x].length; y++) {
					RGB pixel = imageData[x][y];
					if (pixel != null && (pixel.getRed() > 0.01 || pixel.getGreen() > 0.01 || pixel.getBlue() > 0.01)) {
						nonBlackPixels++;
					}
				}
			}

			log("Non-black pixels: " + nonBlackPixels);

			assertTrue("Should have some non-black pixels", nonBlackPixels > 0);

			// Try to save the image
			try {
				File outputDir = new File("results");
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}

				File outputFile = new File("results/simple-sphere-test.jpg");
				log("Saving image to: " + outputFile.getAbsolutePath());

				ImageCanvas.encodeImageFile(v(imageData).get(), outputFile, ImageCanvas.JPEGEncoding);

				log("Image saved successfully!");
			} catch (Exception e) {
				log("Warning: Could not save image: " + e.getMessage());
			}

		} catch (Exception e) {
			log("Exception during render: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void renderTwoSpheres() throws Exception {
		log("Creating scene with two spheres...");

		// Create scene
		Scene<ShadableSurface> scene = new Scene<>();

		// Create first sphere (red)
		Sphere sphere1 = new Sphere();
		sphere1.setLocation(new Vector(-1.5, 0.0, 0.0));
		sphere1.setSize(1.0);
		sphere1.setColor(new RGB(0.8, 0.2, 0.2)); // Red
		((AbstractSurface) sphere1).setShaders(new org.almostrealism.color.Shader[] {
			DiffuseShader.defaultDiffuseShader
		});
		scene.add(sphere1);

		// Create second sphere (green)
		Sphere sphere2 = new Sphere();
		sphere2.setLocation(new Vector(1.5, 0.0, 0.0));
		sphere2.setSize(1.0);
		sphere2.setColor(new RGB(0.2, 0.8, 0.2)); // Green
		((AbstractSurface) sphere2).setShaders(new org.almostrealism.color.Shader[] {
			DiffuseShader.defaultDiffuseShader
		});
		scene.add(sphere2);

		// Add a point light
		PointLight light = new PointLight(new Vector(0.0, 5.0, 10.0));
		scene.addLight(light);

		log("Scene created with two spheres and light");

		// Create camera
		PinholeCamera camera = new PinholeCamera();
		camera.setLocation(new Vector(0.0, 0.0, 10.0));
		camera.setViewDirection(new Vector(0.0, 0.0, -1.0));
		camera.setFocalLength(0.05);
		camera.setProjectionDimensions(0.15, 0.15);  // Zoomed in from 0.2

		scene.setCamera(camera);

		// Create render parameters
		RenderParameters params = new RenderParameters();
		params.width = 128;
		params.height = 128;
		params.dx = 128;
		params.dy = 128;
		params.ssWidth = 1;
		params.ssHeight = 1;

		log("Render parameters: 128x128");

		// Create and render
		RayTracedScene rayTracedScene = new RayTracedScene(
			new RayIntersectionEngine(scene, new FogParameters()),
			camera,
			params
		);

		log("Starting render...");
		RealizableImage realizableImage = rayTracedScene.realize(params);

		log("Evaluating image...");
		RGB[][] imageData = realizableImage.get().evaluate();

		log("Image evaluated! Size: " + imageData.length + "x" + imageData[0].length);

		// Try to save the image
		try {
			File outputDir = new File("results");
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}

			File outputFile = new File("results/two-spheres-test.jpg");
			log("Saving image to: " + outputFile.getAbsolutePath());

			ImageCanvas.encodeImageFile(v(imageData).get(), outputFile, ImageCanvas.JPEGEncoding);

			log("Image saved successfully!");
		} catch (Exception e) {
			log("Warning: Could not save image: " + e.getMessage());
		}
	}
}
