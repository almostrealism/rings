/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.test;

import com.almostrealism.FogParameters;
import com.almostrealism.RayIntersectionEngine;
import com.almostrealism.RenderParameters;
import com.almostrealism.raytracer.RayTracedScene;
import com.almostrealism.raytracer.TestScene;
import org.almostrealism.algebra.Pair;
import org.almostrealism.hardware.AcceleratedComputation;
import org.almostrealism.hardware.AcceleratedProducer;
import org.almostrealism.hardware.DynamicAcceleratedProducer;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.junit.Test;

import java.io.IOException;

public class LightingEngineAggregatorTest {
	protected RayTracedScene getScene() throws IOException {
		TestScene scene = new TestScene(false, false, false,
				true, false, true,
				false, false);

		RenderParameters rp = new RenderParameters();
		rp.width = 100;
		rp.height = 100;
		return new RayTracedScene(new RayIntersectionEngine(scene, new FogParameters()), scene.getCamera(), rp);
	}

	@Test
	public void aggregate() throws IOException {
		AcceleratedComputation p = (AcceleratedComputation) getScene().getProducer();
		System.out.println(p.getFunctionDefinition());
		System.out.println("result = " + p.evaluate(new Object[] { new Pair(50, 50) }));
	}

	@Test
	public void aggregateCompact() throws IOException {
		AcceleratedComputation p = (AcceleratedComputation) getScene().getProducer();
		p.compact();
		System.out.println(p.getFunctionDefinition());
		System.out.println("result = " + p.evaluate(new Object[] { new Pair(50, 50) }));
	}
}
