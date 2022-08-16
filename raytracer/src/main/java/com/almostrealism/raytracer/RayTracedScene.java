/*
 * Copyright 2022 Michael Murray
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

import com.almostrealism.raytrace.Engine;
import com.almostrealism.raytrace.FogParameters;
import com.almostrealism.raytrace.RayIntersectionEngine;
import com.almostrealism.raytrace.RenderParameters;
import io.almostrealism.relation.Realization;
import org.almostrealism.geometry.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;

import org.almostrealism.color.computations.RGBBlack;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.KernelizedProducer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.geometry.DimensionAware;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

public class RayTracedScene implements Realization<RealizableImage, RenderParameters>, CodeFeatures {
	private RayTracer tracer;
	private Camera camera;
	private RenderParameters p;
	
	/**
	 * Controls whether the color of a point light source will be adjusted based on the
	 * intensity of the point light or whether this will be left up to the shader.
	 * By default it is set to true.
	 */
	public static boolean premultiplyIntensity = true;
	
	public static RGB black = new RGB(0.0, 0.0, 0.0);

	public RayTracedScene(Engine t, Camera c) {
		this(t, c, null);
	}

	public RayTracedScene(Engine t, Camera c, RenderParameters p) {
		this(t, c, p, null);
	}

	public RayTracedScene(Engine t, Camera c, RenderParameters p, ExecutorService pool) {
		this.tracer = pool == null ? new RayTracer(t) : new RayTracer(t, pool);
		this.camera = c;
		this.p = p;
	}

	public RayTracedScene(Scene<? extends ShadableSurface> scene, FogParameters fog, RenderParameters p) {
		this(new RayIntersectionEngine(scene, fog), scene.getCamera(), p, null);
	}

	public RenderParameters getRenderParameters() { return p; }

	public KernelizedProducer<RGB> operate(Producer<Pair<?>> uv, Producer<Pair<?>> sd) {
		Future<KernelizedProducer<RGB>> color = tracer.trace(camera.rayAt(uv, sd));

		if (color == null) {
			color = new Future<KernelizedProducer<RGB>>() {
				@Override
				public KernelizedProducer<RGB> get() {
					return RGBBlack.getInstance();
				}

				@Override
				public boolean cancel(boolean mayInterruptIfRunning) { return false; }

				@Override
				public boolean isCancelled() { return false; }

				@Override
				public boolean isDone() { return true; }

				@Override
				public KernelizedProducer<RGB> get(long timeout, TimeUnit unit)
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

	public KernelizedProducer<RGB> getProducer(RenderParameters p) {
		KernelizedProducer<RGB> producer = operate((Producer) v(Pair.class, 0), pair(p.width, p.height));

		if (producer instanceof DimensionAware) {
			((DimensionAware) producer).setDimensions(p.width, p.height, p.ssWidth, p.ssHeight);
		}

		return producer;
	}

	@Override
	public RealizableImage realize(RenderParameters p) {
		this.p = p;

		Pixel px = new Pixel(p.ssWidth, p.ssHeight);
		KernelizedProducer<RGB> producer = getProducer(p);

		for (int i = 0; i < p.ssWidth; i++) {
			for (int j = 0; j < p.ssHeight; j++) {
				px.setSample(i, j, producer);
			}
		}

		return new RealizableImage(px, new Pair(p.dx, p.dy));
	}
}
