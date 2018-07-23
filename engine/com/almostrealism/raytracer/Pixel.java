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

import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.util.Producer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Pixel implements Future<ColorProducer> {
	private int size;
	private Future<Producer<RGB>> samples[][];

	public Pixel(int ssWidth, int ssHeight) {
		size = ssWidth * ssHeight;
		samples = new Future[ssWidth][ssHeight];
	}

	public synchronized void setSample(int sx, int sy, Future<Producer<RGB>> s) {
		if (s == null) {
			throw new IllegalArgumentException("Null sample not supported");
		}

		samples[sx][sy] = s;
	}

	@Override
	public synchronized boolean cancel(boolean mayInterruptIfRunning) {
		// TODO  If any samples are done or running and mayInterruptIfRunning is false, don't cancel any other samples

		boolean cannotCancel = false;

		for (int i = 0; i < samples.length; i++) {
			j: for (int j = 0; j < samples[i].length; j++) {
				if (samples[i][j] == null) {
					cannotCancel = true;
					continue j;
				}

				if (!samples[i][j].cancel(mayInterruptIfRunning)) {
					cannotCancel = true;
				}
			}
		}

		return !cannotCancel;
	}

	@Override
	public synchronized boolean isCancelled() {
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				if (samples[i][j] == null) return false;
				if (!samples[i][j].isCancelled()) return false;
			}
		}

		return true;
	}

	@Override
	public synchronized boolean isDone() {
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				if (samples[i][j] == null) return false;
				if (!samples[i][j].isDone()) return false;
			}
		}

		return true;
	}

	@Override
	public synchronized ColorProducer get() throws InterruptedException, ExecutionException {
		Producer<RGB> p[][] = new Producer[samples.length][samples[0].length];

		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				try {
					p[i][j] = samples[i][j].get();
				} catch (ExecutionException e) {
					e.getCause().printStackTrace();
				}
			}
		}

		return compile(p);
	}

	@Override
	public synchronized ColorProducer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		Producer<RGB> p[][] = new Producer[samples.length][samples[0].length];

		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				try {
					p[i][j] = samples[i][j].get(timeout, unit);
				} catch (ExecutionException e) {
					e.getCause().printStackTrace();
				}
			}
		}

		return compile(p);
	}

	public static ColorProducer compile(Producer<RGB> pr[][]) {
		return new SuperSampler(pr);
	}
}
