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

package com.almostrealism.network.test;

import com.almostrealism.network.TestScene;
import org.almostrealism.raytrace.FogParameters;
import org.almostrealism.raytrace.RayIntersectionEngine;
import org.almostrealism.raytrace.RenderParameters;
import org.almostrealism.render.RayTracedScene;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.Pair;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBFeatures;
import org.almostrealism.hardware.AcceleratedComputationOperation;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import org.almostrealism.hardware.MemoryBank;
import io.almostrealism.relation.Producer;
import org.almostrealism.swing.displays.ImageDisplay;
import io.almostrealism.code.ProducerArgumentReference;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;

public class LightingEngineAggregatorTest extends KernelizedIntersectionTest implements RGBFeatures {
	protected RayTracedScene getScene() throws IOException {
		TestScene scene = new TestScene(false, false, false,
				true, false, false,
				true, false, false, false);

		RenderParameters rp = new RenderParameters();
		rp.width = 100;
		rp.height = 100;
		return new RayTracedScene(new RayIntersectionEngine(scene, new FogParameters()), scene.getCamera(), rp);
	}

	@Test
	public void aggregate() throws IOException {
		AcceleratedComputationEvaluable p = (AcceleratedComputationEvaluable) getScene().getProducer();
		System.out.println("result = " + p.evaluate(new Object[] { new Pair(50, 50) }));
	}

	@Test
	public void aggregateCompact() throws IOException {
		AcceleratedComputationEvaluable p = (AcceleratedComputationEvaluable) getScene().getProducer();
		System.out.println("result = " + p.evaluate(new Object[] { new Pair(50, 50) }));
	}

	@Test
	public void aggregateKernelCompare() throws IOException {
		AcceleratedComputationEvaluable<PackedCollection> p = (AcceleratedComputationEvaluable<PackedCollection>) getScene().getProducer();

		PackedCollection input = getInput();
		PackedCollection dim = bank(width * height, pair(width, height).get());
		PackedCollection output = RGB.bank(input.getCount());

		System.out.println("LightingEngineAggregatorTest: Invoking kernel...");
		p.into(output).evaluate(input, dim);

		/*
		System.out.println("LightingEngineAggregatorTest: Displaying image...");

		RGB image[][] = new RGB[width][height];
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				image[i][j] = output.get(j * width + i);
			}
		}

		displayImage(image);
		*/

		System.out.println("LightingEngineAggregatorTest: Comparing...");
		for (int i = 0; i < output.getCount(); i++) {
			RGB value = new RGB(p.evaluate(new Object[] { input.get(i), dim.get(i) }), 0);
			RGB outputRGB = new RGB(output.get(i), 0);
			Assert.assertEquals(value.getRed(), outputRGB.getRed(), Math.pow(10, -10));
			Assert.assertEquals(value.getGreen(), outputRGB.getGreen(), Math.pow(10, -10));
			Assert.assertEquals(value.getBlue(), outputRGB.getBlue(), Math.pow(10, -10));
		}
	}

	@Test
	public void aggregateAcceleratedCompare() throws IOException {
		RayIntersectionEngine.enableAcceleratedAggregator = false;
		Producer<PackedCollection> agg = getScene().getProducer();

		RayIntersectionEngine.enableAcceleratedAggregator = true;
		AcceleratedComputationEvaluable<PackedCollection> p = (AcceleratedComputationEvaluable<PackedCollection>) getScene().getProducer();

		PackedCollection input = getInput();
		PackedCollection dim = bank(width * height, pair(width, height).get());
		PackedCollection output = RGB.bank(input.getCount());

		System.out.println("LightingEngineAggregatorTest: Invoking kernel...");
		p.into(output).evaluate(input, dim);

		System.out.println("LightingEngineAggregatorTest: Comparing...");
		for (int i = 0; i < output.getCount(); i++) {
			PackedCollection result = agg.get().evaluate(new Object[] { input.get(i), dim.get(i) });
			RGB value = result == null ? new RGB(black().get().evaluate(), 0) : new RGB(result, 0);
			RGB outputRGB = new RGB(output.get(i), 0);
			Assert.assertEquals(value.getRed(), outputRGB.getRed(), Math.pow(10, -10));
			Assert.assertEquals(value.getGreen(), outputRGB.getGreen(), Math.pow(10, -10));
			Assert.assertEquals(value.getBlue(), outputRGB.getBlue(), Math.pow(10, -10));
		}
	}

	@Test
	public void compareDependents() throws IOException {
		PackedCollection input = getInput();
		PackedCollection dim = bank(width * height, pair(width, height).get());

		AcceleratedComputationOperation<PackedCollection> a = (AcceleratedComputationOperation<PackedCollection>) getScene().getProducer();

		i: for (int i = 1; i < a.getArguments().size(); i++) {
			if (a.getArguments().get(i) == null) continue i;

			if (a.getArguments().get(i).getProducer() instanceof ProducerArgumentReference) {
				continue i;
			} else {
				Evaluable kp = a.getArguments().get(i).getProducer().get();
				MemoryBank output = (MemoryBank) kp.createDestination(input.getCount());
				kp.into(output).evaluate(input, dim);

				System.out.println("LightingEngineAggregatorTest: Comparing...");
				for (int j = 0; j < output.getCount(); j++) {
					Object value = kp.evaluate(input.get(j), dim.get(j));
					Assert.assertEquals(value, output.get(j));
				}
			}
		}
	}

	public void displayImage(RGB image[][]) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new ImageDisplay(image));
		frame.setSize(image.length, image[0].length);
		frame.setVisible(true);
	}
}
