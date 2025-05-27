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

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.model.Block;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.SequentialBlock;
import org.almostrealism.model.Model;

import java.util.HashMap;
import java.util.Map;

public class DiffusionTransformer implements DiffusionTransformerFeatures {
	public static final int batchSize = 1;

	private final int ioChannels;
	private final int embedDim;
	private final int depth;
	private final int numHeads;
	private final int patchSize;
	private final int condTokenDim;
	private final int globalCondDim;
	private final int maxSeqLen;

	private final int audioSeqLen;
	private final int condSeqLen;

	private final Map<String, PackedCollection<?>> weightMap;
	private final Model model;

	private CompiledModel compiled;

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective) {
		this(ioChannels, embedDim, depth, numHeads, patchSize,
				condTokenDim, globalCondDim, diffusionObjective, 2048, 128);
	}

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective, int maxSeqLen, int condSeqLen) {
		this.ioChannels = ioChannels;
		this.embedDim = embedDim;
		this.depth = depth;
		this.numHeads = numHeads;
		this.patchSize = patchSize;
		this.condTokenDim = condTokenDim;
		this.globalCondDim = globalCondDim;
		this.maxSeqLen = maxSeqLen;
		this.audioSeqLen = maxSeqLen;
		this.condSeqLen = condSeqLen;
		this.weightMap = new HashMap<>();

		this.model = buildModel();
	}

	protected Model buildModel() {
		// Create model with input shape - [batch, channels, sequence_length]
		Model model = new Model(shape(batchSize, ioChannels, audioSeqLen));

		// Add timestep embedding input
		model.addInput(createTimestampEmbedding());

		// Add cross-attention condition input if needed
		SequentialBlock condEmbed = null;
		if (condTokenDim > 0) {
			PackedCollection<?> condProjWeight = new PackedCollection<>(shape(embedDim, condTokenDim));
			weightMap.put("condEmbedding.weight", condProjWeight);

			condEmbed = new SequentialBlock(shape(condSeqLen, condTokenDim));
			condEmbed.add(dense(condProjWeight));
			model.addInput(condEmbed);
		}

		// Add global condition input if needed
		if (globalCondDim > 0) {
			PackedCollection<?> globalProjInWeight = new PackedCollection<>(shape(embedDim, globalCondDim));
			PackedCollection<?> globalProjOutWeight = new PackedCollection<>(shape(embedDim, embedDim * 6));
			weightMap.put("globalEmbeddingIn.weight", globalProjInWeight);
			weightMap.put("globalEmbeddingOut.weight", globalProjOutWeight);

			SequentialBlock globalEmbed = new SequentialBlock(shape(globalCondDim));
			globalEmbed.add(dense(globalProjInWeight));
			globalEmbed.add(silu());
			globalEmbed.add(dense(globalProjOutWeight));
			model.addInput(globalEmbed);
		}

		// Main model pipeline
		SequentialBlock main = model.sequential();

		// Input projection
		PackedCollection<?> inputProjWeight = new PackedCollection<>(shape(ioChannels, embedDim));
		PackedCollection<?> inputProjBias = new PackedCollection<>(shape(embedDim));
		weightMap.put("inputProjection.weight", inputProjWeight);
		weightMap.put("inputProjection.bias", inputProjBias);

		main.add(convolution1d(batchSize, ioChannels, embedDim, audioSeqLen, 1, 0, inputProjWeight, inputProjBias));


		// Reshape from [batch, channels, seq_len] to [batch, seq_len, channels]
		if (patchSize > 1) {
			main.add(layer("patchify",
					shape(1, embedDim, audioSeqLen),
					shape(1, audioSeqLen/patchSize, embedDim * patchSize),
					in -> reshape(shape(1, audioSeqLen/patchSize, embedDim * patchSize), in)));
		} else {
			main.reshape(batchSize, embedDim, audioSeqLen)
					.enumerate(1, 2, 1)
					.reshape(batchSize, audioSeqLen, embedDim);
		}

		// Add transformer blocks
		addTransformerBlocks(main, condEmbed, embedDim);

		// Reshape back to channels-first format
		if (patchSize > 1) {
			main.add(layer("unpatchify",
					shape(1, audioSeqLen/patchSize, embedDim * patchSize),
					shape(1, embedDim, audioSeqLen),
					in -> reshape(shape(1, embedDim, audioSeqLen), in)));
		} else {
			main.reshape(batchSize, audioSeqLen, embedDim)
					.enumerate(1, 2, 1)
					.reshape(batchSize, embedDim, audioSeqLen);
		}

		// Output projection
		PackedCollection<?> outputProjWeight = new PackedCollection<>(shape(embedDim, ioChannels));
		PackedCollection<?> outputProjBias = new PackedCollection<>(shape(ioChannels));
		weightMap.put("outputProjection.weight", outputProjWeight);
		weightMap.put("outputProjection.bias", outputProjBias);

		main.add(convolution1d(
				batchSize, embedDim, ioChannels, audioSeqLen,
				1, 0, outputProjWeight, outputProjBias));

		return model;
	}

	protected Block createTimestampEmbedding() {
		PackedCollection<?> timestampEmbeddingInWeight = new PackedCollection<>(shape(256, embedDim));
		PackedCollection<?> timestampEmbeddingInBias = new PackedCollection<>(shape(embedDim));
		PackedCollection<?> timestampEmbeddingOutWeight = new PackedCollection<>(shape(embedDim, embedDim));
		PackedCollection<?> timestampEmbeddingOutBias = new PackedCollection<>(shape(embedDim));
		weightMap.put("timestepEmbedding.0.weight", timestampEmbeddingInWeight);
		weightMap.put("timestepEmbedding.0.bias", timestampEmbeddingInBias);
		weightMap.put("timestepEmbedding.2.weight", timestampEmbeddingOutWeight);
		weightMap.put("timestepEmbedding.2.bias", timestampEmbeddingOutBias);
		return timestepEmbedding(batchSize, embedDim,
				timestampEmbeddingInWeight, timestampEmbeddingInBias,
				timestampEmbeddingOutWeight, timestampEmbeddingOutBias);
	}

	protected void addTransformerBlocks(SequentialBlock main, SequentialBlock condEmbed, int dim) {
		for (int i = 0; i < depth; i++) {
			boolean hasCrossAttention = condTokenDim > 0 && condEmbed != null;
			boolean hasGlobalCond = globalCondDim > 0;

			// Create and track all weights for this transformer block
			int dimHead = dim / numHeads;
			String blockPrefix = "transformerBlocks[" + i + "]";

			// Self-attention weights
			PackedCollection<?> rmsAttWeight = new PackedCollection<>(shape(dim)).fill(1.0);
			PackedCollection<?> wq = new PackedCollection<>(shape(dim, dim));
			PackedCollection<?> wk = new PackedCollection<>(shape(dim, dim));
			PackedCollection<?> wv = new PackedCollection<>(shape(dim, dim));
			PackedCollection<?> wo = new PackedCollection<>(shape(dim, dim));
			PackedCollection<?> freqCis = new PackedCollection<>(shape(maxSeqLen, dimHead / 2, 2));

			// Initialize freqCis with rotary position embeddings
			for (int pos = 0; pos < maxSeqLen; pos++) {
				for (int j = 0; j < dimHead / 2; j++) {
					double theta = pos * Math.pow(10000, -2.0 * j / dimHead);
					freqCis.setMem(pos * (dimHead / 2) * 2 + j * 2, Math.cos(theta));     // cos
					freqCis.setMem(pos * (dimHead / 2) * 2 + j * 2 + 1, Math.sin(theta)); // sin
				}
			}

			// Store self-attention weights
			weightMap.put(blockPrefix + ".selfAttention.rmsWeight", rmsAttWeight);
			weightMap.put(blockPrefix + ".selfAttention.wq", wq);
			weightMap.put(blockPrefix + ".selfAttention.wk", wk);
			weightMap.put(blockPrefix + ".selfAttention.wv", wv);
			weightMap.put(blockPrefix + ".selfAttention.wo", wo);
			weightMap.put(blockPrefix + ".selfAttention.freqCis", freqCis);

			// Cross-attention weights (if needed)
			PackedCollection<?> crossAttRmsWeight = null;
			PackedCollection<?> crossWq = null;
			PackedCollection<?> crossWk = null;
			PackedCollection<?> crossWv = null;
			PackedCollection<?> crossWo = null;

			if (hasCrossAttention) {
				crossAttRmsWeight = new PackedCollection<>(shape(dim)).fill(1.0);
				crossWq = new PackedCollection<>(shape(dim, dim));
				crossWk = new PackedCollection<>(shape(dim, dim));
				crossWv = new PackedCollection<>(shape(dim, dim));
				crossWo = new PackedCollection<>(shape(dim, dim));

				// Store cross-attention weights
				weightMap.put(blockPrefix + ".crossAttention.rmsWeight", crossAttRmsWeight);
				weightMap.put(blockPrefix + ".crossAttention.wq", crossWq);
				weightMap.put(blockPrefix + ".crossAttention.wk", crossWk);
				weightMap.put(blockPrefix + ".crossAttention.wv", crossWv);
				weightMap.put(blockPrefix + ".crossAttention.wo", crossWo);
			}

			// Feed-forward weights
			int hiddenDim = embedDim * 4;
			PackedCollection<?> rmsFfnWeight = new PackedCollection<>(shape(embedDim)).fill(1.0);
			PackedCollection<?> w1 = new PackedCollection<>(shape(hiddenDim, embedDim));
			PackedCollection<?> w2 = new PackedCollection<>(shape(embedDim, hiddenDim));
			PackedCollection<?> w3 = new PackedCollection<>(shape(hiddenDim, embedDim));

			// Store feed-forward weights
			weightMap.put(blockPrefix + ".feedForward.rmsWeight", rmsFfnWeight);
			weightMap.put(blockPrefix + ".feedForward.w1", w1);
			weightMap.put(blockPrefix + ".feedForward.w2", w2);
			weightMap.put(blockPrefix + ".feedForward.w3", w3);

			// Add transformer block with all weights explicitly passed
			main.add(transformerBlock(
					batchSize, dim, audioSeqLen, numHeads,
					hasCrossAttention, condTokenDim, condSeqLen, hasGlobalCond, condEmbed,
					// Self-attention weights
					rmsAttWeight, wq, wk, wv, wo, freqCis,
					// Cross-attention weights
					crossAttRmsWeight, crossWq, crossWk, crossWv, crossWo,
					// Feed-forward weights
					rmsFfnWeight, w1, w2, w3
			));
		}
	}

	public PackedCollection<?> forward(PackedCollection<?> x, PackedCollection<?> t,
									   PackedCollection<?> crossAttnCond,
									   PackedCollection<?> globalCond) {
		if (compiled == null) {
			compiled = model.compile(false);
		}

		// Run the model with appropriate inputs
		if (condTokenDim > 0 && globalCondDim > 0) {
			return compiled.forward(x, t, crossAttnCond, globalCond);
		} else if (condTokenDim > 0) {
			return compiled.forward(x, t, crossAttnCond);
		} else if (globalCondDim > 0) {
			return compiled.forward(x, t, globalCond);
		} else {
			return compiled.forward(x, t);
		}
	}

	public void loadWeights(Map<String, PackedCollection<?>> weights) {
		for (Map.Entry<String, PackedCollection<?>> entry : weights.entrySet()) {
			if (weightMap.containsKey(entry.getKey())) {
				PackedCollection<?> dest = weightMap.get(entry.getKey());
				PackedCollection<?> src = entry.getValue();

				// Check shape compatibility
				if (dest.getShape().equalsIgnoreAxis(src.getShape())) {
					dest.setMem(0, src);
				} else {
					System.err.println("Shape mismatch for " + entry.getKey() + ": " +
							dest.getShape() + " vs " + src.getShape());
				}
			} else {
				System.err.println("Unknown weight key: " + entry.getKey());
			}
		}
	}
}