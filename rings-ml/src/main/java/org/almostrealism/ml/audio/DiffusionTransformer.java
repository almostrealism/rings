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
		// TODO  Switch to use feedForward
		if (condTokenDim > 0) {
			PackedCollection<?> condProjWeight1 = createWeight("condEmbedding.0.weight", embedDim, condTokenDim);
			PackedCollection<?> condProjWeight2 = createWeight("condEmbedding.2.weight", embedDim, embedDim);

			condEmbed = new SequentialBlock(shape(condSeqLen, condTokenDim));
			condEmbed.add(dense(condProjWeight1));
			condEmbed.add(silu());
			condEmbed.add(dense(condProjWeight2));
			model.addInput(condEmbed);
		}

		// Add global condition input if needed
		if (globalCondDim > 0) {
			PackedCollection<?> globalProjInWeight = new PackedCollection<>(shape(embedDim, globalCondDim));
			PackedCollection<?> globalProjOutWeight = new PackedCollection<>(shape(embedDim, embedDim));
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
		PackedCollection<?> inputProjWeight =
				createWeight("inputProjection.weight", ioChannels, ioChannels);
		main.add(residual(convolution1d(batchSize, ioChannels, ioChannels, audioSeqLen,
				1, 0, inputProjWeight, null)));


		// Reshape from [batch, channels, seq_len] to [batch, seq_len, channels]
		if (patchSize > 1) {
			main.add(layer("patchify",
					shape(1, ioChannels, audioSeqLen),
					shape(1, audioSeqLen / patchSize, ioChannels * patchSize),
					in -> reshape(shape(1, audioSeqLen / patchSize, ioChannels * patchSize), in)));
		} else {
			main.reshape(batchSize, ioChannels, audioSeqLen)
					.enumerate(1, 2, 1)
					.reshape(batchSize, audioSeqLen, ioChannels);
		}

		// Add transformer blocks
		addTransformerBlocks(main, condEmbed, embedDim);

		// Reshape back to channels-first format
		if (patchSize > 1) {
			main.add(layer("unpatchify",
					shape(1, audioSeqLen / patchSize, embedDim * patchSize),
					shape(1, embedDim, audioSeqLen),
					in -> reshape(shape(1, embedDim, audioSeqLen), in)));
		} else {
			main.reshape(batchSize, audioSeqLen, embedDim)
					.enumerate(1, 2, 1)
					.reshape(batchSize, embedDim, audioSeqLen);
		}

		// Output projection
		PackedCollection<?> outputProjWeight =
				createWeight("outputProjection.weight", ioChannels, ioChannels);
		main.add(residual(convolution1d(
				batchSize, embedDim, ioChannels, audioSeqLen,
				1, 0, outputProjWeight, null)));

		return model;
	}

	protected Block createTimestampEmbedding() {
		PackedCollection<?> timestepFeaturesWeight = new PackedCollection<>(shape(128));
		weightMap.put("timestepFeatures.weight", timestepFeaturesWeight);

		PackedCollection<?> timestampEmbeddingInWeight = new PackedCollection<>(shape(embedDim, 256));
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
		PackedCollection<?> transformerProjectInWeight = new PackedCollection<>(shape(dim, ioChannels * patchSize));
		PackedCollection<?> transformerProjectOutWeight = new PackedCollection<>(shape(ioChannels * patchSize, dim));
		weightMap.put("transformerProjectIn.weight", transformerProjectInWeight);
		weightMap.put("transformerProjectOut.weight", transformerProjectOutWeight);

		for (int i = 0; i < depth; i++) {
			boolean hasCrossAttention = condTokenDim > 0 && condEmbed != null;
			boolean hasGlobalCond = globalCondDim > 0;

			// Create and track all weights for this transformer block
			int dimHead = dim / numHeads;
			String blockPrefix = "transformerBlocks[" + i + "]";
			
			PackedCollection<?> rmsAttWeight = createWeight(blockPrefix + ".selfAttention.rmsWeight", dim).fill(1.0);
			PackedCollection<?> selfAttRmsBias = createWeight(blockPrefix + ".selfAttention.rmsBias", dim);
			PackedCollection<?> wq = createWeight(blockPrefix + ".selfAttention.wq", dim, dim);
			PackedCollection<?> wk = createWeight(blockPrefix + ".selfAttention.wk", dim, dim);
			PackedCollection<?> wv = createWeight(blockPrefix + ".selfAttention.wv", dim, dim);
			PackedCollection<?> wo = createWeight(blockPrefix + ".selfAttention.wo", dim, dim);
			PackedCollection<?> selfAttQNormWeight = createWeight(blockPrefix + ".selfAttention.qNormWeight", dimHead);
			PackedCollection<?> selfAttQNormBias = createWeight(blockPrefix + ".selfAttention.qNormBias", dimHead);
			PackedCollection<?> selfAttKNormWeight = createWeight(blockPrefix + ".selfAttention.kNormWeight", dimHead);
			PackedCollection<?> selfAttKNormBias = createWeight(blockPrefix + ".selfAttention.kNormBias", dimHead);
			PackedCollection<?> freqCis = createWeight(blockPrefix + ".selfAttention.freqCis", maxSeqLen, dimHead / 2, 2);

			// Cross-attention weights (if needed)
			PackedCollection<?> crossAttRmsWeight = null;
			PackedCollection<?> crossAttRmsBias = null;
			PackedCollection<?> crossWq = null;
			PackedCollection<?> crossWk = null;
			PackedCollection<?> crossWv = null;
			PackedCollection<?> crossWo = null;
			PackedCollection<?> crossAttQNormWeight = null;
			PackedCollection<?> crossAttQNormBias = null;
			PackedCollection<?> crossAttKNormWeight = null;
			PackedCollection<?> crossAttKNormBias = null;

			if (hasCrossAttention) {
				crossAttRmsWeight = createWeight(blockPrefix + ".crossAttention.rmsWeight", dim).fill(1.0);
				crossAttRmsBias = createWeight(blockPrefix + ".crossAttention.rmsBias", dim);
				crossWq = createWeight(blockPrefix + ".crossAttention.wq", dim, dim);
				crossWk = createWeight(blockPrefix + ".crossAttention.wk", dim, dim);
				crossWv = createWeight(blockPrefix + ".crossAttention.wv", dim, dim);
				crossWo = createWeight(blockPrefix + ".crossAttention.wo", dim, dim);
				crossAttQNormWeight = createWeight(blockPrefix + ".crossAttention.qNormWeight", dimHead);
				crossAttQNormBias = createWeight(blockPrefix + ".crossAttention.qNormBias", dimHead);
				crossAttKNormWeight = createWeight(blockPrefix + ".crossAttention.kNormWeight", dimHead);
				crossAttKNormBias = createWeight(blockPrefix + ".crossAttention.kNormBias", dimHead);
			}

			int hiddenDim = dim * 4;
			PackedCollection<?> rmsFfnWeight = createWeight(blockPrefix + ".feedForward.rmsWeight", dim).fill(1.0);
			PackedCollection<?> ffRmsBias = createWeight(blockPrefix + ".feedForward.rmsBias", dim);
			PackedCollection<?> w1 = createWeight(blockPrefix + ".feedForward.w1", hiddenDim, dim);
			PackedCollection<?> ffW1Bias = createWeight(blockPrefix + ".feedForward.w1Bias", hiddenDim);
			PackedCollection<?> w2 = createWeight(blockPrefix + ".feedForward.w2", dim, hiddenDim);
			PackedCollection<?> ffW2Bias = createWeight(blockPrefix + ".feedForward.w2Bias", dim);
			PackedCollection<?> w3 = createWeight(blockPrefix + ".feedForward.w3", hiddenDim, dim);

			// Add transformer block with all weights explicitly passed
			main.add(transformerBlock(
					batchSize, dim, audioSeqLen, numHeads,
					hasCrossAttention, condTokenDim, condSeqLen, hasGlobalCond, condEmbed,
					// Self-attention weights
					rmsAttWeight, selfAttRmsBias,
					wq, wk, wv, wo,
					selfAttQNormWeight, selfAttQNormBias, selfAttKNormWeight, selfAttKNormBias,
					freqCis,
					// Cross-attention weights
					crossAttRmsWeight, crossAttRmsBias,
					crossWq, crossWk, crossWv, crossWo,
					crossAttQNormWeight, crossAttQNormBias, crossAttKNormWeight, crossAttKNormBias,
					// Feed-forward weights
					rmsFfnWeight, ffRmsBias,
					w1, w2, w3, ffW1Bias, ffW2Bias
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

	protected PackedCollection<?> createWeight(String key, int... shape) {
		PackedCollection<?> weight = new PackedCollection<>(shape);
		weightMap.put(key, weight);
		return weight;
	}

	public void loadWeights(Map<String, PackedCollection<?>> weights) {
		for (Map.Entry<String, PackedCollection<?>> entry : weights.entrySet()) {
			if (weightMap.containsKey(entry.getKey())) {
				PackedCollection<?> dest = weightMap.get(entry.getKey());
				PackedCollection<?> src = entry.getValue();

				// Confirm shape compatibility
				if (dest.getShape().trim().equalsIgnoreAxis(src.getShape().trim())) {
					dest.setMem(0, src);
				}

				// Warn about shapes not being identical
				if (!dest.getShape().equalsIgnoreAxis(src.getShape())) {
					warn("Shape mismatch for " + entry.getKey() + ": Expected " +
							dest.getShape() + " while " + src.getShape() + " was provided");
				}
			} else {
				warn("Unknown weight key: " + entry.getKey());
			}
		}
	}
}