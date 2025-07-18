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

import io.almostrealism.relation.Factor;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioComposer implements Factor<PackedCollection<?>>, CodeFeatures {
	private final AutoEncoder autoencoder;
	private List<ComposableAudioFeatures> features;

	private Random random;

	public AudioComposer(AutoEncoder autoencoder) {
		this(autoencoder, System.currentTimeMillis());
	}

	public AudioComposer(AutoEncoder autoencoder, long seed) {
		this(autoencoder, new Random(seed));
	}

	public AudioComposer(AutoEncoder autoencoder, Random random) {
		this.autoencoder = autoencoder;
		this.random = random;
		this.features = new ArrayList<>();
	}

	public void addSource(Producer<PackedCollection<?>> features) {
		addSource(new ComposableAudioFeatures(features, createWeights(features)));
	}

	public void addSource(ComposableAudioFeatures features) {
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
		return randn(shape(features), random);
	}
}
