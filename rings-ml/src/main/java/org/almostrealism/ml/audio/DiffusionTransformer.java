package org.almostrealism.ml.audio;

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.model.Block;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.SequentialBlock;
import org.almostrealism.model.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffusionTransformer implements DiffusionTransformerFeatures {
	public static final int batchSize = 1;

	// Model configuration from model_config.json
	private final int ioChannels;
	private final int embedDim;
	private final int depth;
	private final int numHeads;
	private final int patchSize;
	private final int condTokenDim;
	private final int globalCondDim;
	private final String diffusionObjective;
	private final int maxSeqLen;

	private final int audioSeqLen;
	private final int condSeqLen;


	// The compiled model
	private final CompiledModel model;

	// Weight mapping for loading from checkpoints
	private final Map<String, PackedCollection<?>> weightMap;

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
		this.diffusionObjective = diffusionObjective;
		this.maxSeqLen = maxSeqLen;
		this.audioSeqLen = maxSeqLen;
		this.condSeqLen = condSeqLen;
		this.weightMap = new HashMap<>();

		// Build and compile the model
		this.model = buildModel();
	}

	protected CompiledModel buildModel() {
		// Create input shape - [batch, channels, sequence_length]
		TraversalPolicy inputShape = shape(batchSize, ioChannels, audioSeqLen);

		// Create model
		Model model = new Model(inputShape);

		// Add timestep embedding input
		Block timestepEmbed = timestepEmbedding(batchSize, embedDim);
		model.addInput(timestepEmbed);

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
		SequentialBlock globalEmbed = null;
		if (globalCondDim > 0) {
			PackedCollection<?> globalProjWeight = new PackedCollection<>(shape(embedDim * 6, globalCondDim));
			weightMap.put("globalEmbedding.weight", globalProjWeight);

			globalEmbed = new SequentialBlock(shape(globalCondDim));
			globalEmbed.add(dense(globalProjWeight));
			model.addInput(globalEmbed);
		}

		// Main model pipeline
		SequentialBlock main = model.sequential();

		// Input projection
		PackedCollection<?> inputProjWeight = new PackedCollection<>(shape(ioChannels, embedDim));
		PackedCollection<?> inputProjBias = new PackedCollection<>(shape(embedDim));
		weightMap.put("inputProjection.weight", inputProjWeight);
		weightMap.put("inputProjection.bias", inputProjBias);

		// Use the weights in convolution1d
		main.add(convolution1d(batchSize, ioChannels, embedDim, audioSeqLen, 1, 0));

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
		for (int i = 0; i < depth; i++) {
			boolean hasCrossAttention = condTokenDim > 0 && condEmbed != null;
			boolean hasGlobalCond = globalCondDim > 0;

			// Create and track all weights for this transformer block
			int dimHead = embedDim / numHeads;
			String blockPrefix = "transformerBlocks[" + i + "]";

			// Self-attention weights
			PackedCollection<?> rmsAttWeight = new PackedCollection<>(shape(embedDim)).fill(1.0);
			PackedCollection<?> wq = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wk = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wv = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wo = new PackedCollection<>(shape(embedDim, embedDim));
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
				crossAttRmsWeight = new PackedCollection<>(shape(embedDim)).fill(1.0);
				crossWq = new PackedCollection<>(shape(embedDim, embedDim));
				crossWk = new PackedCollection<>(shape(embedDim, embedDim));
				crossWv = new PackedCollection<>(shape(embedDim, embedDim));
				crossWo = new PackedCollection<>(shape(embedDim, embedDim));

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
					batchSize, embedDim, audioSeqLen, numHeads,
					hasCrossAttention, condTokenDim, condSeqLen, hasGlobalCond, condEmbed,
					// Self-attention weights
					rmsAttWeight, wq, wk, wv, wo, freqCis,
					// Cross-attention weights
					crossAttRmsWeight, crossWq, crossWk, crossWv, crossWo,
					// Feed-forward weights
					rmsFfnWeight, w1, w2, w3
			));
		}

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

		return model.compile(false);
	}

	public PackedCollection<?> forward(PackedCollection<?> x, PackedCollection<?> t,
									   PackedCollection<?> crossAttnCond,
									   PackedCollection<?> globalCond) {
		// Run the model with appropriate inputs
		if (condTokenDim > 0 && globalCondDim > 0) {
			return model.forward(x, t, crossAttnCond, globalCond);
		} else if (condTokenDim > 0) {
			return model.forward(x, t, crossAttnCond);
		} else if (globalCondDim > 0) {
			return model.forward(x, t, globalCond);
		} else {
			return model.forward(x, t);
		}
	}

	public void loadWeights(Map<String, PackedCollection<?>> weights) {
		// Copy weights from the provided map to our internal weights
		for (Map.Entry<String, PackedCollection<?>> entry : weights.entrySet()) {
			if (weightMap.containsKey(entry.getKey())) {
				PackedCollection<?> dest = weightMap.get(entry.getKey());
				PackedCollection<?> src = entry.getValue();

				// Check shape compatibility
				if (dest.getShape().getTotalSize() == src.getShape().getTotalSize()) {
					// Copy weights
					for (int i = 0; i < dest.getShape().getTotalSize(); i++) {
						dest.setMem(i, src.toDouble(i));
					}
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