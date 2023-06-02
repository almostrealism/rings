/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.network.test;

import com.almostrealism.lighting.PointLight;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.primitives.Sphere;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.raytrace.IntersectionalLightingEngine;
import io.almostrealism.code.OperationAdapter;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducerBase;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Intersectable;
import org.almostrealism.geometry.Ray;
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
		Producer<RGB> engine = engine().getProducer();
		return (GeneratedColorProducer) ((OperationAdapter) engine).getInputs().get(2);
	}

	protected ExpressionComputation<Scalar> dotProduct() {
		return (ExpressionComputation<Scalar>)
				((OperationAdapter) generatedColorProducer().getProducer()).getInputs().get(1);
	}

	protected ExpressionComputation<Vector> vectorFromScalars() {
		return (ExpressionComputation<Vector>) ((OperationAdapter) dotProduct()).getInputs().get(1);
	}

	protected ExpressionComputation<Scalar> scalarProduct() {
		return (ExpressionComputation<Scalar>) ((OperationAdapter) vectorFromScalars()).getInputs().get(1);
	}

	protected ExpressionComputation<Scalar> scalarFromVector() {
		return (ExpressionComputation<Scalar>) ((OperationAdapter) scalarProduct()).getInputs().get(1);
	}

	protected VectorProducerBase rayDirection() {
		return (VectorProducerBase) ((OperationAdapter) scalarFromVector()).getInputs().get(1);
	}

	@Test
	public void evaluateDotProduct() {
		ExpressionComputation<Scalar> dp = dotProduct();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.getValue(), -1.0);
	}

	@Test
	public void evaluateVectorFromScalars() {
		ExpressionComputation<Vector> dp = vectorFromScalars();
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
		ExpressionComputation<Scalar> dp = scalarProduct();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(0.0, s.getValue());
	}

	@Test
	public void evaluateScalarFromVector() {
		ExpressionComputation<Scalar> dp = scalarFromVector();
		Evaluable<Scalar> ev = dp.get();
		((OperationAdapter) ev).compile();

		Scalar s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.getValue(), 0.0);
	}

	@Test
	public void evaluateRayDirection() {
		VectorProducerBase dp = rayDirection();
		Evaluable<Vector> ev = dp.get();
		((OperationAdapter) ev).compile();

		Vector v = ev.evaluate();
		System.out.println(v);
		assertEquals(v.getX(), 0.0);
		assertEquals(v.getY(), 0.0);
		assertEquals(v.getZ(), 1.0);
	}

	@Test
	public void evaluate() {
		Evaluable<RGB> ev = engine().getProducer().get();
		System.out.println(ev.evaluate());
	}
}
