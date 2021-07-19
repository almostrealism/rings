package com.almostrealism.raytracer.test;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.primitives.Sphere;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.raytrace.IntersectionalLightingEngine;
import io.almostrealism.code.OperationAdapter;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.computations.DotProduct;
import org.almostrealism.algebra.computations.ScalarFromVector;
import org.almostrealism.algebra.computations.ScalarProduct;
import org.almostrealism.algebra.computations.VectorFromScalars;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Intersectable;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.computations.RayDirection;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.util.ArrayList;

public class IntersectionalLightingEngineTest implements TestFeatures {
	protected IntersectionalLightingEngine engine() {
		Producer<Ray> r = ray(0.0, 0.0, 10.0, 0.0, 0.0, -1.0);
		Light l = new PointLight(new Vector(0.0, 10.0, 10.0));
		Curve<RGB> s = new Sphere();
		((AbstractSurface) s).addShader(DiffuseShader.defaultDiffuseShader);

		ShaderContext c = new ShaderContext(s, l);

		return new IntersectionalLightingEngine(r, (Intersectable) s,
				new ArrayList<>(), l, new ArrayList<>(), c);
	}

	protected GeneratedColorProducer generatedColorProducer() {
		AcceleratedComputationEvaluable<RGB> engine = engine();
		return (GeneratedColorProducer) ((OperationAdapter) engine.getComputation()).getInputs().get(2);
	}

	protected DotProduct dotProduct() {
		return (DotProduct)
				((OperationAdapter) generatedColorProducer().getProducer()).getInputs().get(1);
	}

	protected VectorFromScalars vectorFromScalars() {
		return (VectorFromScalars) ((OperationAdapter) dotProduct()).getInputs().get(1);
	}

	protected ScalarProduct scalarProduct() {
		return (ScalarProduct) ((OperationAdapter) vectorFromScalars()).getInputs().get(1);
	}

	protected ScalarFromVector scalarFromVector() {
		return (ScalarFromVector) ((OperationAdapter) scalarProduct()).getInputs().get(1);
	}

	protected RayDirection rayDirection() {
		return (RayDirection) ((OperationAdapter) scalarFromVector()).getInputs().get(1);
	}

	@Test
	public void evaluateDotProduct() {
		DotProduct dp = dotProduct();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.getValue(), -1.0);
	}

	@Test
	public void evaluateVectorFromScalars() {
		VectorFromScalars dp = vectorFromScalars();
		Evaluable<Vector> ev = dp.get();
		((OperationAdapter) ev).compile();

		Vector v = ev.evaluate();
		System.out.println(v);
		assertEquals(v.getX(), 0.0);
		assertEquals(v.getY(), 0.0);
		assertEquals(v.getZ(), 1.0);
	}

	@Test
	public void evaluateScalarProduct() {
		ScalarProduct dp = scalarProduct();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(0.0, s.getValue());
	}

	@Test
	public void evaluateScalarFromVector() {
		ScalarFromVector dp = scalarFromVector();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.getValue(), 0.0);
	}

	@Test
	public void evaluateRayDirection() {
		RayDirection dp = rayDirection();
		Evaluable<Vector> ev = dp.get();
		((OperationAdapter) ev).compile();

		Vector v = ev.evaluate();
		System.out.println(v);
		assertEquals(v.getX(), 0.0);
		assertEquals(v.getY(), 0.0);
		assertEquals(v.getZ(), 1.0);
	}

	@Test
	public void compile() {
		AcceleratedComputationEvaluable<RGB> ev = engine();
		ev.compile();

		System.out.println(ev.getFunctionDefinition());
	}

	@Test
	public void evaluate() {
		AcceleratedComputationEvaluable<RGB> ev = engine();
		ev.compile();
		System.out.println(ev.evaluate());
	}
}
