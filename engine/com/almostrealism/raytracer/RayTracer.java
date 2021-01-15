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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.almostrealism.color.RGB;
import org.almostrealism.geometry.Ray;
import io.almostrealism.relation.Producer;

public class RayTracer {
	public static boolean enableThreadPool = false;

	private ExecutorService pool;
	private static long threadCount = 0;

	private Engine engine;
	
	public RayTracer(Engine engine) {
		this(engine, Executors.newFixedThreadPool(10, r -> new Thread(r, "RayTracer Thread " + (threadCount++))));
	}

	public RayTracer(Engine engine, ExecutorService pool) {
		this.engine = engine;
		this.pool = pool;
	}

	public Future<Producer<RGB>> trace(Producer<Ray> r) {
		if (enableThreadPool) {
			Callable<Producer<RGB>> c = () -> engine.trace(r);
			return pool.submit(c);
		} else {
			return CompletableFuture.completedFuture(engine.trace(r));
		}
	}

	public interface Engine {
		Producer<RGB> trace(Producer<Ray> r);
	}
}
