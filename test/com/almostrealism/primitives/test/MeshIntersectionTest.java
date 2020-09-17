package com.almostrealism.primitives.test;

import com.almostrealism.projection.ThinLensCamera;
import org.almostrealism.algebra.Intersection;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.Ray;
import org.almostrealism.graph.mesh.DefaultVertexData;
import org.almostrealism.graph.mesh.Mesh;
import org.almostrealism.graph.mesh.MeshData;
import org.almostrealism.graph.mesh.Triangle;
import org.almostrealism.hardware.KernelizedProducer;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.util.AdaptProducer;
import org.almostrealism.util.PassThroughProducer;
import org.almostrealism.util.Producer;
import org.almostrealism.util.ProducerWithRank;
import org.almostrealism.util.ProducerWithRankAdapter;
import org.almostrealism.util.RankedChoiceProducer;
import org.almostrealism.util.StaticProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MeshIntersectionTest {
	private MeshData data;
	private KernelizedProducer<Ray> ray;

	protected Mesh mesh() {
		DefaultVertexData data = new DefaultVertexData(3, 1);
		data.getVertices().set(0, new Vector(0.0, 1.0, 0.0));
		data.getVertices().set(1, new Vector(-1.0, -1.0, 0.0));
		data.getVertices().set(2, new Vector(1.0, -1.0, 0.0));
		data.setTriangle(0, 0, 1, 2);

		return new Mesh(data);
	}

	protected Producer<Ray> camera() {
		ThinLensCamera c = new ThinLensCamera();
		c.setLocation(new Vector(0.0, 0.0, 10.0));
		c.setViewDirection(new Vector(0.0, 0.0, -1.0));
		c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
		c.setFocalLength(0.05);
		c.setFocus(10.0);
		c.setLensRadius(0.2);

		int w = 100;
		int h = (int)(c.getProjectionHeight() * (w / c.getProjectionWidth()));
		return c.rayAt(new PassThroughProducer<>(0), StaticProducer.of(new Pair(w, h)));
	}

	@Before
	public void init() {
		data = mesh().getMeshData();
		ray = (KernelizedProducer) camera();
		ray.compact();
	}

	@Test
	public void intersectAt() {
		PairBank input = new PairBank(2);
		input.set(0, new Pair(0.00, 0.00));
		input.set(1, new Pair(43.50, 92.00));

		ScalarBank distances = new ScalarBank(2);
		data.evaluateIntersectionKernel(ray, distances, new MemoryBank[] { input }, 0, distances.getCount());

		System.out.println("distance(0) = " + distances.get(0).getValue());
		Assert.assertEquals(-1.0, distances.get(0).getValue(), Math.pow(10, -10));

		System.out.println("distance(1) = " + distances.get(1).getValue());
		Assert.assertEquals(-1.0, distances.get(1).getValue(), Math.pow(10, -10));

		RankedChoiceProducer<Vector> closestNormal = new RankedChoiceProducer<>(Intersection.e, false);
		closestNormal.add(new ProducerWithRankAdapter<>(
					StaticProducer.of(data.get(0).getNormal()),
					new AdaptProducer<>(Triangle.intersectAt, ray, StaticProducer.of(data.get(0)))));
		closestNormal.evaluate(new Object[] { new Pair(43.50, 92.00) });
	}

	public void intersectAtGrid() {

	}
}
