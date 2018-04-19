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
import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.Camera;
import org.almostrealism.algebra.Pair;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.geometry.Ray;

import io.almostrealism.lambda.Realization;
import org.almostrealism.relation.PairFunction;

public class RayTracedScene implements Realization<RealizableImage, RenderParameters>, PairFunction<Future<ColorProducer>> {
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

	@Override
	public Future<ColorProducer> operate(Pair uv) {
		double r = uv.a();
		double q = uv.b();

		Ray ray = camera.rayAt(r, p.height - q, p.width, p.height);

		Future<ColorProducer> color = tracer.trace(ray.getOrigin(), ray.getDirection());

		if (color == null) {
			color = new Future<ColorProducer>() {
				@Override
				public ColorProducer get() {
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
	public Scope<Variable<?>> getScope(String prefix) {
		Scope<Variable<?>> s = new Scope<>();
		// TODO  Add Ray produced by camera to the Scope (Camera should implement Computation).
		return s;
	}

	@Override
	public RealizableImage realize(RenderParameters p) {
		this.p = p;

		Future<ColorProducer> image[][] = new Future[p.dx][p.dy];
		
		for (int i = p.x; i < (p.x + p.dx); i++) {
			System.out.println("RayTracedScene: Realizing col " + i);

			for (int j = p.y; j < (p.y + p.dy); j++) {
				for (int k = 0; k < p.ssWidth; k++)
				for (int l = 0; l < p.ssHeight; l++) {
					double r = i + ((double) k / (double) p.ssWidth);
					double q = j + ((double) l / (double) p.ssHeight);

					Future<ColorProducer> color = operate(new Pair(r, q));
					
					if (image[i - p.x][j - p.y] == null) {
						if (p.ssWidth > 1 || p.ssHeight > 1) {
							double scale = 1.0 / (p.ssWidth * p.ssHeight);

							try {
								color = new ColorProduct(color.get());
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}

							((ColorProduct) color).add(new RGB(scale, scale, scale));
						}
						
						image[i - p.x][j - p.y] = color;
					} else {
						double scale = 1.0 / (p.ssWidth * p.ssHeight);

						try {
							color = new ColorProduct(color.get());
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}

						((ColorProduct) color).add(new RGB(scale, scale, scale));
						
						image[i - p.x][j - p.y] = new ColorSum(image[i - p.x][j - p.y], color);
					}
				}
			}
		}

		return new RealizableImage(image);
	}
}
