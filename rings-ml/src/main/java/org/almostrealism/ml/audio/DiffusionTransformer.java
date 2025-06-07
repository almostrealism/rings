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

import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.model.Block;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.SequentialBlock;
import org.almostrealism.model.Model;

import java.io.IOException;

public class DiffusionTransformer implements DiffusionTransformerFeatures {
	private static final int SAMPLE_SIZE = 524288;
	private static final int DOWNSAMPLING_RATIO = 2048;

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

	private final StateDictionary stateDictionary;
	private final Model model;

	private CompiledModel compiled;

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective) {
		this(ioChannels, embedDim, depth, numHeads, patchSize,
				condTokenDim, globalCondDim, diffusionObjective,
				SAMPLE_SIZE / DOWNSAMPLING_RATIO, 65, null);
	}

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective, int maxSeqLen, int condSeqLen) {
		this(ioChannels, embedDim, depth, numHeads, patchSize,
				condTokenDim, globalCondDim, diffusionObjective,
				maxSeqLen, condSeqLen, null);
	}

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective, String weightsDirectory) throws IOException {
		this(ioChannels, embedDim, depth, numHeads, patchSize,
				condTokenDim, globalCondDim, diffusionObjective,
				SAMPLE_SIZE / DOWNSAMPLING_RATIO, 65,
				weightsDirectory != null ? new StateDictionary(weightsDirectory) : null);
	}

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective, int maxSeqLen, int condSeqLen,
								StateDictionary stateDictionary) {
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
		this.stateDictionary = stateDictionary;

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
			PackedCollection<?> globalProjInWeight =
					createWeight("globalEmbeddingIn.weight", embedDim, globalCondDim);
			PackedCollection<?> globalProjOutWeight =
					createWeight("globalEmbeddingOut.weight", embedDim, embedDim);
			
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
					shape(1, audioSeqLen / patchSize, ioChannels * patchSize),
					shape(1, embedDim, audioSeqLen),
					in -> reshape(shape(1, ioChannels, audioSeqLen), in)));
		} else {
			main.reshape(batchSize, audioSeqLen, ioChannels)
					.enumerate(1, 2, 1)
					.reshape(batchSize, ioChannels, audioSeqLen);
		}

		// Output projection
		PackedCollection<?> outputProjWeight =
				createWeight("outputProjection.weight", ioChannels, ioChannels);
		main.add(residual(convolution1d(
				batchSize, ioChannels, ioChannels, audioSeqLen,
				1, 0, outputProjWeight, null)));

		return model;
	}

	protected Block createTimestampEmbedding() {
		PackedCollection<?> timestampEmbeddingInWeight = createWeight("timestepEmbedding.0.weight", embedDim, 256);
		PackedCollection<?> timestampEmbeddingInBias = createWeight("timestepEmbedding.0.bias", embedDim);
		PackedCollection<?> timestampEmbeddingOutWeight = createWeight("timestepEmbedding.2.weight", embedDim, embedDim);
		PackedCollection<?> timestampEmbeddingOutBias = createWeight("timestepEmbedding.2.bias", embedDim);

		return timestepEmbedding(batchSize, embedDim,
				timestampEmbeddingInWeight, timestampEmbeddingInBias,
				timestampEmbeddingOutWeight, timestampEmbeddingOutBias);
	}

	protected void addTransformerBlocks(SequentialBlock main, SequentialBlock condEmbed, int dim) {
		int dimHead = dim / numHeads;

		PackedCollection<?> transformerProjectInWeight =
				createWeight("transformerProjectIn.weight", dim, ioChannels * patchSize);
		PackedCollection<?> transformerProjectOutWeight =
				createWeight("transformerProjectOut.weight", ioChannels * patchSize, dim);
		PackedCollection<?> invFreq =
				createWeight("selfAttention.invFreq", dimHead / 4);

		main.add(dense(transformerProjectInWeight));

		for (int i = 0; i < depth; i++) {
			boolean hasCrossAttention = condTokenDim > 0 && condEmbed != null;
			boolean hasGlobalCond = globalCondDim > 0;

			// Create and track all weights for this transformer block
			String blockPrefix = "transformerBlocks[" + i + "]";
			PackedCollection<?> preNormWeight = createWeight(blockPrefix + ".preNorm.weight", dim).fill(1.0);
			PackedCollection<?> preNormBias = createWeight(blockPrefix + ".preNorm.bias", dim);
			PackedCollection<?> qkv = createWeight(blockPrefix + ".selfAttention.qkv", dim * 3, dim);
			PackedCollection<?> wo = createWeight(blockPrefix + ".selfAttention.wo", dim, dim);
			PackedCollection<?> selfAttQNormWeight = createWeight(blockPrefix + ".selfAttention.qNormWeight", dimHead);
			PackedCollection<?> selfAttQNormBias = createWeight(blockPrefix + ".selfAttention.qNormBias", dimHead);
			PackedCollection<?> selfAttKNormWeight = createWeight(blockPrefix + ".selfAttention.kNormWeight", dimHead);
			PackedCollection<?> selfAttKNormBias = createWeight(blockPrefix + ".selfAttention.kNormBias", dimHead);

			// Cross-attention weights (if needed)
			PackedCollection<?> crossAttPreNormWeight = null;
			PackedCollection<?> crossAttPreNormBias = null;
			PackedCollection<?> crossWq = null;
			PackedCollection<?> crossKv = null;
			PackedCollection<?> crossWo = null;
			PackedCollection<?> crossAttQNormWeight = null;
			PackedCollection<?> crossAttQNormBias = null;
			PackedCollection<?> crossAttKNormWeight = null;
			PackedCollection<?> crossAttKNormBias = null;

			if (hasCrossAttention) {
				crossAttPreNormWeight = createWeight(blockPrefix + ".crossAttention.preNormWeight", dim).fill(1.0);
				crossAttPreNormBias = createWeight(blockPrefix + ".crossAttention.preNormBias", dim);
				crossWq = createWeight(blockPrefix + ".crossAttention.wq", dim, dim);
				crossKv = createWeight(blockPrefix + ".crossAttention.kv", 2 * dim, dim);
				crossWo = createWeight(blockPrefix + ".crossAttention.wo", dim, dim);
				crossAttQNormWeight = createWeight(blockPrefix + ".crossAttention.qNormWeight", dimHead);
				crossAttQNormBias = createWeight(blockPrefix + ".crossAttention.qNormBias", dimHead);
				crossAttKNormWeight = createWeight(blockPrefix + ".crossAttention.kNormWeight", dimHead);
				crossAttKNormBias = createWeight(blockPrefix + ".crossAttention.kNormBias", dimHead);
			}

			int hiddenDim = dim * 4;
			PackedCollection<?> ffnPreNormWeight = createWeight(blockPrefix + ".feedForward.preNormWeight", dim).fill(1.0);
			PackedCollection<?> ffnPreNormBias = createWeight(blockPrefix + ".feedForward.preNormBias", dim);
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
					preNormWeight, preNormBias,
					qkv, wo,
					selfAttQNormWeight, selfAttQNormBias, selfAttKNormWeight, selfAttKNormBias,
					invFreq,
					// Cross-attention weights
					crossAttPreNormWeight, crossAttPreNormBias,
					crossWq, crossKv, crossWo,
					crossAttQNormWeight, crossAttQNormBias, crossAttKNormWeight, crossAttKNormBias,
					// Feed-forward weights
					ffnPreNormWeight, ffnPreNormBias,
					w1, w2, w3, ffW1Bias, ffW2Bias
			));
		}

		main.add(dense(transformerProjectOutWeight));
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

	protected PackedCollection<?> createWeight(String key, int... dims) {
		return createWeight(key, shape(dims));
	}

	protected PackedCollection<?> createWeight(String key, TraversalPolicy expectedShape) {
		if (stateDictionary != null && stateDictionary.containsKey(key)) {
			PackedCollection<?> weight = stateDictionary.get(key);
			
			// Verify shape compatibility
			if (!weight.getShape().trim().equalsIgnoreAxis(expectedShape.trim())) {
				if (weight.getShape().getTotalSizeLong() != expectedShape.getTotalSizeLong()) {
					throw new IllegalArgumentException("Expected " + expectedShape +
							" for key " + key + " while " + weight.getShape() + " was provided");
				} else {
					warn("Expected " + expectedShape + " for key " + key +
							" while " + weight.getShape() + " was provided");
				}
			}
			
			return weight.range(expectedShape);
		} else {
			if (stateDictionary != null) {
				throw new IllegalArgumentException(key + " not found in StateDictionary");
			}

			return  new PackedCollection<>(expectedShape);
		}
	}
}