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

package com.almostrealism.raytracer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.almostrealism.RenderParameters;
import org.almostrealism.algebra.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;

import io.almostrealism.lambda.Realization;
import org.almostrealism.color.computations.RGBBlack;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.util.DimensionAware;
import org.almostrealism.util.PassThroughProducer;
import org.almostrealism.util.Producer;
import org.almostrealism.util.Provider;

public class RayTracedScene implements Realization<RealizableImage, RenderParameters>, CodeFeatures {
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
		this(t, c, p, null);
	}

	public RayTracedScene(RayTracer.Engine t, Camera c, RenderParameters p, ExecutorService pool) {
		this.tracer = pool == null ? new RayTracer(t) : new RayTracer(t, pool);
		this.camera = c;
		this.p = p;
	}

	public RenderParameters getRenderParameters() { return p; }

	public Producer<RGB> operate(Producer<Pair> uv, Producer<Pair> sd) {
		Future<Producer<RGB>> color = tracer.trace(camera.rayAt(uv, sd));

		if (color == null) {
			color = new Future<Producer<RGB>>() {
				@Override
				public Producer<RGB> get() {
					return RGBBlack.getProducer();
				}

				@Override
				public boolean cancel(boolean mayInterruptIfRunning) { return false; }

				@Override
				public boolean isCancelled() { return false; }

				@Override
				public boolean isDone() { return true; }

				@Override
				public Producer<RGB> get(long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException, TimeoutException {
					return get();
				}
			};
		}

		try {
			return color.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Producer<RGB> getProducer() { return getProducer(getRenderParameters()); }

	public Producer<RGB> getProducer(RenderParameters p) {
		Producer<RGB> producer = operate(new PassThroughProducer(0), pair(p.width, p.height).get());

		if (producer instanceof DimensionAware) {
			((DimensionAware) producer).setDimensions(p.width, p.height, p.ssWidth, p.ssHeight);
		}

		return producer;
	}

	@Override
	public RealizableImage realize(RenderParameters p) {
		this.p = p;

		Pixel px = new Pixel(p.ssWidth, p.ssHeight);
		Producer<RGB> producer = getProducer(p);

		for (int i = 0; i < p.ssWidth; i++) {
			for (int j = 0; j < p.ssHeight; j++) {
				px.setSample(i, j, producer);
			}
		}

		return new RealizableImage(px, new Pair(p.dx, p.dy));
	}
}
