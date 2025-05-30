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

	default Block fourierFeatures(int batchSize, int inFeatures, int outFeatures) {
		// Output dim should be even for sin/cos pairs
		if (outFeatures % 2 != 0) {
			throw new IllegalArgumentException("Output features must be even for Fourier features");
		}

		// Create frequency basis for position embedding
		int freqDim = outFeatures / 2;
		PackedCollection<?> freqs = new PackedCollection<>(freqDim);
		for (int i = 0; i < freqDim; i++) {
			// Geometric sequence as in standard transformer positional encoding
			double freq = Math.pow(10000, -2.0 * i / freqDim);
			freqs.setMem(i, freq);
		}

		return layer("fourierFeatures",
				shape(batchSize, inFeatures),
				shape(batchSize, outFeatures),
				in -> {
					CollectionProducer<PackedCollection<?>> input = c(in);
					CollectionProducer<PackedCollection<?>> freqTensor = cp(freqs);

					// Compute x * freq for each frequency
					CollectionProducer<PackedCollection<?>> xfreq =
							input.multiply(freqTensor.expand(inFeatures).transpose());

					// Calculate sin and cos components
					CollectionProducer<PackedCollection<?>> sinValues = sin(xfreq);
					CollectionProducer<PackedCollection<?>> cosValues = cos(xfreq);

					// Concatenate sin and cos values
					return concat(shape(batchSize, outFeatures),
							sinValues.reshape(batchSize, freqDim),
							cosValues.reshape(batchSize, freqDim));
				},
				List.of(freqs));
	}

	default Block timestepEmbedding(int batchSize, int embedDim,
									PackedCollection<?> weight0, PackedCollection<?> bias0,
									PackedCollection<?> weight2, PackedCollection<?> bias2) {
		SequentialBlock embedding = new SequentialBlock(shape(batchSize, 1));
		embedding.add(fourierFeatures(batchSize, 1, 256));
		embedding.add(dense(weight0, bias0));
		embedding.add(silu(shape(embedDim)));
		embedding.add(dense(weight2, bias2));
		return embedding;
	}

	default Block transformerBlock(int batchSize, int dim, int seqLen, int heads,
								   boolean crossAttend, int contextDim,
								   int contextSeqLen, boolean globalCond, Block context,
								   // Self-attention weights
								   PackedCollection<?> selfAttRmsWeight, PackedCollection<?> selfAttRmsBias,
								   PackedCollection<?> selfWq, PackedCollection<?> selfWk,
								   PackedCollection<?> selfWv, PackedCollection<?> selfWo,
								   PackedCollection<?> selfQNormWeight, PackedCollection<?> selfQNormBias,
								   PackedCollection<?> selfKNormWeight, PackedCollection<?> selfKNormBias,
								   PackedCollection<?> invFreq,
								   // Cross-attention weights
								   PackedCollection<?> crossAttRmsWeight, PackedCollection<?> crossAttRmsBias,
								   PackedCollection<?> crossWq, PackedCollection<?> crossWk,
								   PackedCollection<?> crossWv, PackedCollection<?> crossWo,
								   PackedCollection<?> crossQNormWeight, PackedCollection<?> crossQNormBias,
								   PackedCollection<?> crossKNormWeight, PackedCollection<?> crossKNormBias,
								   // Feed-forward weights
								   PackedCollection<?> rmsFfnWeight, PackedCollection<?> rmsFfnBias,
								   PackedCollection<?> w1, PackedCollection<?> w2, PackedCollection<?> w3,
								   PackedCollection<?> w1Bias, PackedCollection<?> w2Bias) {
		SequentialBlock block = new SequentialBlock(shape(batchSize, seqLen, dim));
		int dimHead = dim / heads;

		// Create self-attention block with sequence processing
		Block selfAttention = sequenceAttention(
								batchSize, seqLen, dim, heads,
								selfAttRmsWeight, selfAttRmsBias,
								selfWk, selfWv, selfWq, selfWo,
								selfQNormWeight, selfQNormBias,
								selfKNormWeight, selfKNormBias,
								invFreq);
		block.add(residual(preNorm(selfAttention)));

		// Cross-attention (if needed)
		if (crossAttend) {
			if (context == null) {
				throw new IllegalArgumentException("Context block cannot be null for cross-attention");
			}

			// Create cross-attention block with context
			Block crossAttention = crossAttention(
					batchSize, seqLen, contextSeqLen, heads, dimHead,
					crossAttRmsWeight, crossAttRmsBias,
					crossWk, crossWv, crossWq, crossWo,
					crossQNormWeight, crossQNormBias,
					crossKNormWeight, crossKNormBias,
					context);
			block.add(residual(preNorm(crossAttention)));
		}

		// Feed-forward block
		Block feedForward = feedForward(block.getOutputShape(),
										rmsFfnWeight, rmsFfnBias,
										w1, w2, w3, w1Bias, w2Bias);
		block.add(residual(preNorm(feedForward)));

		return block;
	}

	default Block preNorm(Block block) {
		SequentialBlock preNorm = new SequentialBlock(block.getInputShape());
		preNorm.add(rmsnorm(block.getInputShape().getTotalSize()));
		preNorm.add(block);
		return preNorm;
	}
}