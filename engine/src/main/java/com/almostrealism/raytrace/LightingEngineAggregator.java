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

package com.almostrealism.raytrace;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.geometry.Intersectable;
import org.almostrealism.geometry.Intersection;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.graph.PathElement;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import io.almostrealism.relation.Producer;
import io.almostrealism.code.CollectionUtils;
import org.almostrealism.geometry.DimensionAware;
import io.almostrealism.relation.ProducerWithRank;
import org.almostrealism.color.computations.RankedChoiceEvaluableForRGB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LightingEngineAggregator extends RankedChoiceEvaluableForRGB implements PathElement<RGB, RGB>, DimensionAware {
	public static boolean enableVerbose = false;

	private PairBank input;
	private List<ScalarBank> ranks;

	private boolean kernel;
	private int width, height, ssw, ssh;

	public LightingEngineAggregator(Producer<Ray> r, Iterable<Curve<RGB>> surfaces, Iterable<Light> lights, ShaderContext context) {
		this(r, surfaces, lights, context, false);
	}

	public LightingEngineAggregator(Producer<Ray> r, Iterable<Curve<RGB>> surfaces, Iterable<Light> lights, ShaderContext context, boolean kernel) {
		super(Intersection.e);
		this.kernel = kernel;
		init(r, surfaces, lights, context);
	}

	@Override
	public void setDimensions(int w, int h, int ssw, int ssh) {
		this.width = w;
		this.height = h;
		this.ssw = ssw;
		this.ssh = ssh;

		int totalWidth = w * ssw;
		int totalHeight = h * ssh;

		PairBank pixelLocations = new PairBank(totalWidth * totalHeight);

		for (double i = 0; i < totalWidth; i++) {
			for (double j = 0; j < totalHeight; j++) {
				Pair p = pixelLocations.get((int) (j * totalWidth + i));
				p.setMem(new double[] { i / ssw, j / ssh });
			}
		}

		setKernelInput(pixelLocations);

		stream().filter(p -> p instanceof DimensionAware)
				.forEach(p -> ((DimensionAware) p).setDimensions(width, height, ssw, ssh));
	}

	/**
	 * Provide a {@link MemoryBank} to use when evaluating the rank for each
	 * {@link LightingEngine}.
	 */
	private void setKernelInput(PairBank input) {
		this.input = input;
		resetRankCache();
	}

	/**
	 * Run rank computations for all {@link LightingEngine}s, if they are not already been available, using
	 * {@link KernelizedEvaluable#kernelEvaluate(MemoryBank, MemoryBank[])}.
	 */
	public synchronized void initRankCache() {
		if (this.ranks != null) return;
		if (this.input == null)
			throw new IllegalArgumentException("Kernel input must be specified ahead of rank computation");

		System.out.println("LightingEngineAggregator: Evaluating rank kernels...");

		this.ranks = new ArrayList<>();
		for (int i = 0; i < size(); i++) {
			this.ranks.add(new ScalarBank(input.getCount()));
			((KernelizedEvaluable) get(i).getRank().get()).kernelEvaluate(ranks.get(i), new MemoryBank[] { input });
		}
	}

	/**
	 * Destroy the cache of rank for {@link LightingEngine}s.
	 */
	public void resetRankCache() {
		this.ranks = null;
	}

	// TODO  Rename this class to SurfaceLightingAggregator and have LightingEngineAggregator sum the lights instead of rank choice them
	protected void init(Producer<Ray> r, Iterable<Curve<RGB>> surfaces, Iterable<Light> lights, ShaderContext context) {
		for (Curve<RGB> s : surfaces) {
			for (Light l : lights) {
				Collection<Curve<RGB>> otherSurfaces = CollectionUtils.separate(s, surfaces);
				Collection<Light> otherLights = CollectionUtils.separate(l, lights);

				ShaderContext c;

				if (context == null) {
					c = new ShaderContext(s, l);
					c.setOtherSurfaces(otherSurfaces);
					c.setOtherLights(otherLights);
				} else {
					c = context.clone();
					c.setSurface(s);
					c.setLight(l);
					c.setOtherSurfaces(otherSurfaces);
					c.setOtherLights(otherLights);
				}

				// TODO Choose which engine dynamically
				this.add(new IntersectionalLightingEngine(r, (Intersectable) s,
															otherSurfaces,
															l, otherLights,
															c));
			}
		}
	}

	@Override
	public RGB evaluate(Object args[]) {
		if (!kernel) return super.evaluate(args);

		initRankCache();

		Pair pos = (Pair) args[0];

		Producer<RGB> best = null;
		double rank = Double.MAX_VALUE;

		boolean printLog = enableVerbose && Math.random() < 0.04;

		if (printLog) {
			System.out.println("RankedChoiceProducer: There are " + size() + " Producers to choose from");
		}

		int position = DimensionAware.getPosition(pos.getX(), pos.getY(), width, height, ssw, ssh);
		// assert pos.equals(input.get(position));

		r: for (int i = 0; i < size(); i++) {
			ProducerWithRank<RGB, Scalar> p = get(i);

			double r = ranks.get(i).get(position).getValue();
			if (r < e && printLog) System.out.println(p + " was skipped due to being less than " + e);
			if (r < e) continue r;

			if (best == null) {
				if (printLog) System.out.println(p + " was assigned (rank = " + r + ")");
				best = p.getProducer();
				rank = r;
			} else {
				if (r >= e && r < rank) {
					if (printLog) System.out.println(p + " was assigned (rank = " + r + ")");
					best = p.getProducer();
					rank = r;
				}
			}

			if (rank <= e) break r;
		}

		if (printLog) System.out.println(best + " was chosen\n----------");

		return best == null ? null : best.get().evaluate(args);
	}

	@Override
	public Iterable<Producer<RGB>> getDependencies() {
		List<Producer<RGB>> p = new ArrayList<>();
		p.addAll(this);
		return p;
	}
}
