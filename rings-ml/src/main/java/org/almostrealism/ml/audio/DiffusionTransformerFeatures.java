/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.ml.audio;

import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.AttentionFeatures;
import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.almostrealism.model.SequentialBlock;

import java.util.List;

public interface DiffusionTransformerFeatures extends AttentionFeatures, DiffusionFeatures {

	default Block fourierFeatures(int batchSize, int inFeatures, int outFeatures, PackedCollection<?> learnedWeights) {
		// Output dim should be even for sin/cos pairs
		if (outFeatures % 2 != 0) {
			throw new IllegalArgumentException("Output features must be even for Fourier features");
		}

		return layer("fourierFeatures",
				shape(batchSize, inFeatures),
				shape(batchSize, outFeatures),
				in -> {
					CollectionProducer<PackedCollection<?>> input = c(in);
					CollectionProducer<PackedCollection<?>> weights = cp(learnedWeights);

					// Compute x * learned_frequencies for each frequency
					// learnedWeights shape: [outFeatures // 2]
					// input shape: [batchSize, inFeatures] = [batchSize, 1]
					// We want to broadcast multiply: input * weights -> [batchSize, outFeatures//2]
					CollectionProducer<PackedCollection<?>> xfreq = input.multiply(weights.expand(batchSize));

					// Calculate sin and cos components
					CollectionProducer<PackedCollection<?>> sinValues = sin(xfreq);
					CollectionProducer<PackedCollection<?>> cosValues = cos(xfreq);

					// Concatenate sin and cos values along feature dimension
					return concat(shape(batchSize, outFeatures),
							sinValues, cosValues);
				},
				List.of(learnedWeights));
	}

	default Block timestepEmbedding(int batchSize, int embedDim,
									PackedCollection<?> timestepFeaturesWeight,
									PackedCollection<?> weight0, PackedCollection<?> bias0,
									PackedCollection<?> weight2, PackedCollection<?> bias2) {
		SequentialBlock embedding = new SequentialBlock(shape(batchSize, 1));
		embedding.add(fourierFeatures(batchSize, 1, 256, timestepFeaturesWeight));
		embedding.add(dense(weight0, bias0));
		embedding.add(silu(shape(embedDim)));
		embedding.add(dense(weight2, bias2));
		return embedding;
	}
}