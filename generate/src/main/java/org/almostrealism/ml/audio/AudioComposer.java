/*
 * Copyright 2025 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.ml.audio;

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.relation.Factor;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioComposer implements Factor<PackedCollection<?>>, Destroyable, CodeFeatures {
	public static boolean normalizeWeights = true;

	private final AutoEncoder autoencoder;
	private final int dim;

	private List<ComposableAudioFeatures> features;

	private Random random;
	private double deviation;

	public AudioComposer(AutoEncoder autoencoder, int dim) {
		this(autoencoder, dim, System.currentTimeMillis());
	}

	public AudioComposer(AutoEncoder autoencoder, int dim, long seed) {
		this(autoencoder, dim, new Random(seed));
	}

	public AudioComposer(AutoEncoder autoencoder, int dim, Random random) {
		this.autoencoder = autoencoder;
		this.dim = dim;
		this.random = random;
		this.features = new ArrayList<>();
		this.deviation = 1.0;
	}

	public double getDeviation() { return deviation; }
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}

	public TraversalPolicy getFeatureShape() {
		if (features.isEmpty()) return null;
		return features.getLast().getFeatureShape();
	}

	public void addAudio(Producer<PackedCollection<?>> audio) {
		addSource(autoencoder.encode(audio));
	}

	public void addSource(Producer<PackedCollection<?>> features) {
		addSource(new ComposableAudioFeatures(features, createWeights(features)));
	}

	public void addSource(ComposableAudioFeatures features) {
		TraversalPolicy featureShape = getFeatureShape();
		if (featureShape != null && !featureShape.equals(features.getFeatureShape())) {
			throw new IllegalArgumentException();
		}

		this.features.add(features);
	}

	@Override
	public Producer<PackedCollection<?>> getResultant(Producer<PackedCollection<?>> value) {
		List<Producer<?>> components = new ArrayList<>();
		features.stream()
				.map(features -> features.getResultant(value))
				.forEach(components::add);
		return autoencoder.decode(add(components));
	}

	protected CollectionProducer<PackedCollection<?>> createWeights(Producer<PackedCollection<?>> features) {
		double scale = 1.0;
		int bins = shape(features).length(0);
		int time = shape(features).length(1);

		CollectionProducer<PackedCollection<?>> rand = randn(shape(dim), scale, scale * getDeviation(), random);
		if (normalizeWeights)
			rand = normalize(rand);
		return rand.repeat(bins).repeat(time);
	}

	@Override
	public void destroy() {
		if (autoencoder != null) {
			autoencoder.destroy();
		}
	}
}
