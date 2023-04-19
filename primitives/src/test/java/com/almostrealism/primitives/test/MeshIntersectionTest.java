package com.almostrealism.primitives.test;

import com.almostrealism.projection.ThinLensCamera;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.geometry.Intersection;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.computations.RankedChoiceEvaluable;
import org.almostrealism.hardware.KernelizedProducer;
import org.almostrealism.space.CachedMeshIntersectionKernel;
import org.almostrealism.space.DefaultVertexData;
import org.almostrealism.space.Mesh;
import org.almostrealism.space.MeshData;
import org.almostrealism.space.Triangle;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Evaluable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MeshIntersectionTest implements CodeFeatures {
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

	protected KernelizedProducer<Ray> camera() {
		ThinLensCamera c = new ThinLensCamera();
		c.setLocation(new Vector(0.0, 0.0, 10.0));
		c.setViewDirection(new Vector(0.0, 0.0, -1.0));
		c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
		c.setFocalLength(0.05);
		c.setFocus(10.0);
		c.setLensRadius(0.2);

		width = 100;
		height = (int)(c.getProjectionHeight() * (width / c.getProjectionWidth()));
		return (KernelizedProducer<Ray>) c.rayAt((Producer) v(Pair.shape(), 0), pair(width, height));
	}

	@Before
	public void init() {
		data = mesh().getMeshData();
		ray = camera();
	}

	@Test
	public void intersectAt() {
		CachedMeshIntersectionKernel kernel = new CachedMeshIntersectionKernel(data, ray);

		PackedCollection<Pair<?>> input = RealizableImage.generateKernelInput(0, 0, width, height);
		ScalarBank distances = new ScalarBank(input.getCount());
		kernel.setDimensions(width, height, 1, 1);
		kernel.kernelEvaluate(distances, new MemoryBank[] { input });

		Evaluable<Vector> closestNormal = kernel.getClosestNormal();

		int pos = 0;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(-1.0, distances.get(pos).getValue(), Math.pow(10, -10));

		pos = (height / 2) * width + width / 2;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(1.0, distances.get(pos).getValue(), Math.pow(10, -10));

		PackedCollection<?> n = closestNormal.evaluate(new Object[] { input.get(pos) });
		System.out.println("normal(" + pos + ") = " + n);
		Assert.assertEquals(0.0, n.toDouble(0), Math.pow(10, -10));
		Assert.assertEquals(0.0, n.toDouble(1), Math.pow(10, -10));
		Assert.assertEquals(1.0, n.toDouble(2), Math.pow(10, -10));

		pos = (height / 2) * width + 3 * width / 8;
		System.out.println("distance(" + pos + ") = " + distances.get(pos).getValue());
		Assert.assertEquals(1.042412281036377, distances.get(pos).getValue(), Math.pow(10, -10));

		n = closestNormal.evaluate(new Object[] { input.get(pos) });
		System.out.println("normal(" + pos + ") = " + n);
		Assert.assertEquals(-0.6666666865348816, n.toDouble(0), Math.pow(10, -10));
		Assert.assertEquals(0.3333333432674408, n.toDouble(1), Math.pow(10, -10));
		Assert.assertEquals(0.6666666865348816, n.toDouble(2), Math.pow(10, -10));
	}

	@Test
	public void triangleIntersectAtKernel() {
		PackedCollection<Ray> in = Ray.bank(1);
		ScalarBank distances = new ScalarBank(1);

		in.set(0, 0.0, 0.0, 1.0, 0.0, 0.0, -1.0);
		Triangle.intersectAt.kernelEvaluate(distances, new MemoryBank[] { in, data });
		System.out.println("distance = " + distances.get(0).getValue());
		Assert.assertEquals(1.0, distances.get(0).getValue(), Math.pow(10, -10));

		PackedCollection<Pair<?>> out = Pair.bank(1);
		PackedCollection<Pair<?>> conf = Pair.bank(1);
		conf.set(0, new Pair(1, Intersection.e));
		RankedChoiceEvaluable.highestRank.kernelEvaluate(out, new MemoryBank[] { distances, conf });
		System.out.println("highest rank: " + out.get(0));
		Assert.assertEquals(1.0, out.get(0).getA(), Math.pow(10, -10));
		Assert.assertEquals(0.0, out.get(0).getB(), Math.pow(10, -10));
	}
}
