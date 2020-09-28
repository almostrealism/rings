package com.almostrealism.primitives.test;

import com.almostrealism.projection.ThinLensCamera;
import org.almostrealism.algebra.Intersection;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RealizableImage;
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
		DefaultVertexData data = new DefaultVertexData(5, 3);
		data.getVertices().set(0, new Vector(0.0, 1.0, 0.0));
		data.getVertices().set(1, new Vector(-1.0, -1.0, 0.0));
		data.getVertices().set(2, new Vector(1.0, -1.0, 0.0));
		data.getVertices().set(3, new Vector(-1.0, 1.0, -1.0));
		data.getVertices().set(4, new Vector(1.0, 1.0, -1.0));
		data.setTriangle(0, 0, 1, 2);
		data.setTriangle(1, 3, 1, 0);
		data.setTriangle(2, 0, 2, 4);

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

		PairBank input = RealizableImage.generateKernelInput(0, 0, width, height);
		ScalarBank distances = new ScalarBank(input.getCount());
		kernel.setDimensions(width, height, 1, 1);
		kernel.kernelEvaluate(distances, new MemoryBank[] { input });

		Producer<Vector> closestNormal = kernel.getClosestNormal();

		int pos = 0;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(-1.0, distances.get(pos).getValue(), Math.pow(10, -10));

		pos = (height / 2) * width + width / 2;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(1.0, distances.get(pos).getValue(), Math.pow(10, -10));

		Vector n = closestNormal.evaluate(new Object[] { input.get(pos) });
		System.out.println("normal(" + pos + ") = " + n);
		Assert.assertEquals(0.0, n.getX(), Math.pow(10, -10));
		Assert.assertEquals(0.0, n.getY(), Math.pow(10, -10));
		Assert.assertEquals(1.0, n.getZ(), Math.pow(10, -10));

		pos = (height / 2) * width + 3 * width / 8;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(1.042412281036377, distances.get(pos).getValue(), Math.pow(10, -10));

		n = closestNormal.evaluate(new Object[] { input.get(pos) });
		System.out.println("normal(" + pos + ") = " + n);
		Assert.assertEquals(-0.6666666865348816, n.getX(), Math.pow(10, -10));
		Assert.assertEquals(0.3333333432674408, n.getY(), Math.pow(10, -10));
		Assert.assertEquals(0.6666666865348816, n.getZ(), Math.pow(10, -10));
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
