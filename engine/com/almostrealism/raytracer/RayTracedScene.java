/*
 * Copyright 2018 Michael Murray
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
package com.almostrealism.raytracer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.almostrealism.RenderParameters;
import org.almostrealism.algebra.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;

import io.almostrealism.lambda.Realization;
import org.almostrealism.util.PassThroughProducer;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

public class RayTracedScene implements Realization<RealizableImage, RenderParameters> {
	private RayTracer tracer;
	private Camera camera;
	private RenderParameters p;
	
	/**
	 * Controls whether the color of a point light source will be adjusted based on the
	 * intensity of the point light or whether this will be left up to the shader.
	 * By default set to true.
	 */
	public static boolean premultiplyIntensity = true;
	
	public static RGB black = new RGB(0.0, 0.0, 0.0);

	public RayTracedScene(RayTracer.Engine t, Camera c) {
		this(t, c, null);
	}

	public RayTracedScene(RayTracer.Engine t, Camera c, RenderParameters p) {
		this.tracer = new RayTracer(t);
		this.camera = c;
		this.p = p;
	}

	public RenderParameters getRenderParameters() { return p; }

	public Future<Producer<RGB>> operate(Producer<Pair> uv, Producer<Pair> sd) {
		Future<Producer<RGB>> color = tracer.trace(camera.rayAt(uv, sd));

		if (color == null) {
			color = new Future<Producer<RGB>>() {
				@Override
				public RGB get() {
					return RayTracedScene.black;
				}

				@Override
				public boolean cancel(boolean mayInterruptIfRunning) { return false; }

				@Override
				public boolean isCancelled() { return false; }

				@Override
				public boolean isDone() { return true; }

				@Override
				public ColorProducer get(long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException, TimeoutException {
					return get();
				}
			};
		}

		return color;
	}

	@Override
	public RealizableImage realize(RenderParameters p) {
		this.p = p;

		Pixel px = new Pixel(p.ssWidth, p.ssHeight);

		long start = System.nanoTime();

		for (int i = 0; i < p.ssWidth; i++) {
			for (int j = 0; j < p.ssHeight; j++) {
				px.setSample(i, j, operate(new PassThroughProducer<>(0),
											new StaticProducer<>(new Pair(p.width, p.height))));
			}
		}

		System.out.println("Generated pixel template after " + (System.nanoTime() - start) + " nanoseconds");

		return new RealizableImage(px, new Pair(p.width, p.height));
	}
}
