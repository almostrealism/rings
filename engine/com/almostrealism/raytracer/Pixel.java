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
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Pixel implements Future<ColorProducer> {
	private List<Future<ColorProducer>> samples = new ArrayList<>();

	public Pixel() { }

	public void addSample(Future<ColorProducer> s) {
		samples.add(s);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO  If any samples are done or running and mayInterruptIfRunning is false, don't cancel any other samples

		boolean cannotCancel = false;

		for (Future<ColorProducer> s : samples) {
			if (!s.cancel(mayInterruptIfRunning)) {
				cannotCancel = true;
			}
		}

		return !cannotCancel;
	}

	@Override
	public boolean isCancelled() {
		for (Future<ColorProducer> s : samples) {
			if (!s.isCancelled()) return false;
		}

		return true;
	}

	@Override
	public boolean isDone() {
		for (Future<ColorProducer> s : samples) {
			if (!s.isDone()) return false;
		}

		return true;
	}

	@Override
	public ColorProducer get() throws InterruptedException, ExecutionException {
		List<ColorProducer> p = new ArrayList<>();
		for (Future<ColorProducer> s : samples) p.add(s.get());
		return compile(p, 1.0 / samples.size());
	}

	@Override
	public ColorProducer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		List<ColorProducer> p = new ArrayList<>();
		for (Future<ColorProducer> s : samples) p.add(s.get(timeout, unit));
		return compile(p, 1.0 / samples.size());
	}

	public static ColorProducer compile(Iterable<ColorProducer> pr, double scale) {
		ColorSum s = new ColorSum();

		for (ColorProducer p : pr) {
			s.add((ColorProducer) new ColorProduct(p, new RGB(scale, scale, scale)));
		}

		return s;
	}
}
