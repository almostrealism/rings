package com.almostrealism.primitives.test;

import com.almostrealism.projection.ThinLensCamera;
import org.almostrealism.algebra.Intersection;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.RayBank;
import org.almostrealism.graph.mesh.CachedMeshIntersectionKernel;
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

	private int width, height;

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

		width = 100;
		height = (int)(c.getProjectionHeight() * (width / c.getProjectionWidth()));
		return c.rayAt(new PassThroughProducer<>(0), StaticProducer.of(new Pair(width, height)));
	}

	@Before
	public void init() {
		data = mesh().getMeshData();
		ray = (KernelizedProducer) camera();
		ray.compact();
	}

	@Test
	public void intersectAt() {
		CachedMeshIntersectionKernel kernel = new CachedMeshIntersectionKernel(data, ray);

		int size = 3;

		PairBank input = new PairBank(size);
		input.set(0, new Pair(0.00, 0.00));
		input.set(1, new Pair(43.50, 92.00));
		input.set(2, new Pair(width / 2, height / 2));

		ScalarBank distances = new ScalarBank(size);
		kernel.setDimensions(width, height, 1, 1);
		kernel.kernelEvaluate(distances, new MemoryBank[] { input });

		System.out.println("distance(0) = " + distances.get(0).getValue());
		Assert.assertEquals(-1.0, distances.get(0).getValue(), Math.pow(10, -10));

		System.out.println("distance(1) = " + distances.get(1).getValue());
		Assert.assertEquals(-1.0, distances.get(1).getValue(), Math.pow(10, -10));

		System.out.println("distance(2) = " + distances.get(2).getValue());
		Assert.assertEquals(1.0, distances.get(2).getValue(), Math.pow(10, -10));

		Producer<Vector> closestNormal = kernel.getClosestNormal();
		Vector n = closestNormal.evaluate(new Object[] { input.get(2) });
		System.out.println("normal(2) = " + n);
	}

	@Test
	public void triangleIntersectAt() {
		RayBank in = new RayBank(1);
		ScalarBank distances = new ScalarBank(1);

		in.set(0, 0.0, 0.0, 1.0, 0.0, 0.0, -1.0);
		Triangle.intersectAt.kernelEvaluate(distances, new MemoryBank[] { in, data });
		System.out.println("distance = " + distances.get(0).getValue());
		Assert.assertEquals(1.0, distances.get(0).getValue(), Math.pow(10, -10));

		PairBank out = new PairBank(1);
		PairBank conf = new PairBank(1);
		conf.set(0, new Pair(1, Intersection.e));
		RankedChoiceProducer.highestRank.kernelEvaluate(out, new MemoryBank[] { distances, conf });
		System.out.println("highest rank: " + out.get(0));
		Assert.assertEquals(1.0, out.get(0).getA(), Math.pow(10, -10));
		Assert.assertEquals(0.0, out.get(0).getB(), Math.pow(10, -10));
	}
}
