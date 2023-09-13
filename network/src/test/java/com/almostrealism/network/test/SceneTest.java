package com.almostrealism.network.test;

import com.almostrealism.network.TestScene;
import com.almostrealism.raytrace.IntersectionalLightingEngine;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.ShadableIntersection;
import org.almostrealism.hardware.cl.CLOperator;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class SceneTest implements TestFeatures {
	@Test
	public void intersection() throws IOException {
		TestScene scene = new TestScene();
		ShadableIntersection field = (ShadableIntersection) scene.getSurfaces()[0].intersectAt(ray(0, 0, 10, 0, 0, -1));
		Producer<Scalar> distance = field.getDistance();
		assertEquals(9.0, distance.get().evaluate());
	}

	@Test
	public void lightingEngine() throws IOException {
		TestScene scene = new TestScene();

		CLOperator.verboseLog(() -> {
			ShaderContext context = new ShaderContext(scene.getSurfaces()[0], scene.getLights().get(0));
			IntersectionalLightingEngine engine = new IntersectionalLightingEngine(ray(0, 0, 10, 0, 0, -1),
					scene.getSurfaces()[0], Collections.emptyList(), scene.getLights().get(0), Collections.emptyList(), context);
			RGB color = engine.getProducer().get().evaluate();
			System.out.println(color);
			assertEquals(0.3211, color.getRed());
			assertEquals(0.3211, color.getGreen());
			assertEquals(0.3211, color.getBlue());
		});
	}
}
