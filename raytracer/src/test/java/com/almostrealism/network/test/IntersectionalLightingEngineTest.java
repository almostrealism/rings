/*
 * Copyright 2025 Michael Murray
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

import org.almostrealism.color.PointLight;
import io.almostrealism.code.ComputableBase;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.primitives.Sphere;
import org.almostrealism.color.DiffuseShader;
import org.almostrealism.raytrace.IntersectionalLightingEngine;
import io.almostrealism.code.OperationAdapter;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.algebra.Vector;
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

	protected CollectionProducer<PackedCollection<?>> dotProduct() {
		return (CollectionProducer<PackedCollection<?>>)
				((OperationAdapter) generatedColorProducer().getProducer()).getInputs().get(1);
	}

	protected CollectionProducer<Vector> vectorFromScalars() {
		return (CollectionProducer<Vector>) ((ComputableBase) dotProduct()).getInputs().get(1);
	}

	protected CollectionProducer<PackedCollection<?>> scalarProduct() {
		return (CollectionProducer<PackedCollection<?>>) ((ComputableBase) vectorFromScalars()).getInputs().get(1);
	}

	protected CollectionProducer<PackedCollection<?>> scalarFromVector() {
		return (CollectionProducer<PackedCollection<?>>) ((ComputableBase) scalarProduct()).getInputs().get(1);
	}

	protected Producer<Vector> rayDirection() {
		return (Producer<Vector>) ((ComputableBase) scalarFromVector()).getInputs().get(1);
	}

	@Test
	public void evaluateDotProduct() {
		CollectionProducer<PackedCollection<?>> dp = dotProduct();
		Evaluable<PackedCollection<?>> ev = dp.get();

		PackedCollection<?> s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.toDouble(0), -1.0);
	}

	@Test
	public void evaluateVectorFromScalars() {
		CollectionProducer<Vector> dp = vectorFromScalars();
		Evaluable<Vector> ev = dp.get();

		Vector v = ev.evaluate();
		System.out.println(v);
		assertEquals(v.getX(), 0.0);
		assertEquals(v.getY(), 0.0);
		assertEquals(v.getZ(), 1.0);
	}

	@Test
	public void evaluateScalarProduct() {
		CollectionProducer<PackedCollection<?>> dp = scalarProduct();
		Evaluable<PackedCollection<?>> ev = dp.get();

		PackedCollection<?> s = ev.evaluate();
		System.out.println(s);
		assertEquals(0.0, s.toDouble(0));
	}

	@Test
	public void evaluateScalarFromVector() {
		CollectionProducer<PackedCollection<?>> dp = scalarFromVector();
		Evaluable<PackedCollection<?>> ev = dp.get();

		PackedCollection<?> s = ev.evaluate();
		System.out.println(s);
		assertEquals(s.toDouble(0), 0.0);
	}

	@Test
	public void evaluateRayDirection() {
		Producer<Vector> dp = rayDirection();
		Evaluable<Vector> ev = dp.get();

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
