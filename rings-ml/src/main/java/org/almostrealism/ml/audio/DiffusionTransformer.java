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

	// The compiled model
	private final CompiledModel model;

	// Weight mapping for loading from checkpoints
	private final Map<String, PackedCollection<?>> weightMap;

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective) {
		this(ioChannels, embedDim, depth, numHeads, patchSize,
				condTokenDim, globalCondDim, diffusionObjective, 2048);
	}

	public DiffusionTransformer(int ioChannels, int embedDim, int depth, int numHeads,
								int patchSize, int condTokenDim, int globalCondDim,
								String diffusionObjective, int maxSeqLen) {
		this.ioChannels = ioChannels;
		this.embedDim = embedDim;
		this.depth = depth;
		this.numHeads = numHeads;
		this.patchSize = patchSize;
		this.condTokenDim = condTokenDim;
		this.globalCondDim = globalCondDim;
		this.diffusionObjective = diffusionObjective;
		this.maxSeqLen = maxSeqLen;
		this.weightMap = new HashMap<>();

		// Build and compile the model
		this.model = buildModel();
	}

	private CompiledModel buildModel() {
		// Create input shape - [batch, channels, sequence_length]
		TraversalPolicy inputShape = shape(batchSize, ioChannels, -1);

		// Create model
		Model model = new Model(inputShape);

		// Add timestep embedding input
		Block timestepEmbed = timestepEmbedding(batchSize, embedDim);
		model.addInput(timestepEmbed);

		// Add cross-attention condition input if needed
		Block condEmbed = null;
		if (condTokenDim > 0) {
			PackedCollection<?> condProjWeight = new PackedCollection<>(shape(condTokenDim, embedDim));
			weightMap.put("condEmbedding.weight", condProjWeight);

			condEmbed = layer("condEmbedding",
					shape(1, -1, condTokenDim),
					shape(1, -1, embedDim),
					in -> matmul(cp(condProjWeight), in),
					List.of(condProjWeight));
			model.addInput(condEmbed);
		}

		// Add global condition input if needed
		Block globalEmbed = null;
		if (globalCondDim > 0) {
			PackedCollection<?> globalProjWeight = new PackedCollection<>(shape(globalCondDim, embedDim * 6));
			weightMap.put("globalEmbedding.weight", globalProjWeight);

			globalEmbed = layer("globalEmbedding",
					shape(1, globalCondDim),
					shape(1, embedDim * 6),
					in -> matmul(cp(globalProjWeight), in),
					List.of(globalProjWeight));
			model.addInput(globalEmbed);
		}

		// Main model pipeline
		SequentialBlock main = model.sequential();

		// Input projection
		PackedCollection<?> inputProjWeight = new PackedCollection<>(shape(ioChannels, embedDim));
		PackedCollection<?> inputProjBias = new PackedCollection<>(shape(embedDim));
		weightMap.put("inputProjection.weight", inputProjWeight);
		weightMap.put("inputProjection.bias", inputProjBias);

		main.add(convolution1d(batchSize, ioChannels, embedDim, 1, 0));

		// Patching if needed (reshape from channels-first to sequence-of-tokens format)
		if (patchSize > 1) {
			main.add(layer("patchify",
					shape(1, embedDim, -1),
					shape(1, -1, embedDim * patchSize),
					in -> {
						// Implementation for patchify operation
						return reshape(shape(1, -1, embedDim * patchSize), in);
					}));
		} else {
			// Just reshape from [batch, channels, seq_len] to [batch, seq_len, channels]
			main.reshape(batchSize, embedDim, -1)
				.enumerate(1, 2, 1)
				.reshape(batchSize, -1, embedDim);
		}

		// Add transformer blocks
		for (int i = 0; i < depth; i++) {
			boolean hasCrossAttention = condTokenDim > 0 && condEmbed != null;
			boolean hasGlobalCond = globalCondDim > 0;

			// Create and track all weights for this transformer block
			int dimHead = embedDim / numHeads;

			// Self-attention weights
			PackedCollection<?> rmsAttWeight = new PackedCollection<>(shape(embedDim));
			PackedCollection<?> wq = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wk = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wv = new PackedCollection<>(shape(embedDim, embedDim));
			PackedCollection<?> wo = new PackedCollection<>(shape(embedDim, embedDim));

			// Cross-attention weights (if needed)
			PackedCollection<?> crossAttRmsWeight = null;
			PackedCollection<?> crossWq = null;
			PackedCollection<?> crossWk = null;
			PackedCollection<?> crossWv = null;
			PackedCollection<?> crossWo = null;

			if (hasCrossAttention) {
				crossAttRmsWeight = new PackedCollection<>(shape(embedDim));
				crossWq = new PackedCollection<>(shape(embedDim, embedDim));
				crossWk = new PackedCollection<>(shape(condTokenDim, embedDim));
				crossWv = new PackedCollection<>(shape(condTokenDim, embedDim));
				crossWo = new PackedCollection<>(shape(embedDim, embedDim));
			}

			// Feedforward weights
			PackedCollection<?> rmsFfnWeight = new PackedCollection<>(shape(embedDim));
			PackedCollection<?> w1 = new PackedCollection<>(shape(embedDim, embedDim * 4));
			PackedCollection<?> w2 = new PackedCollection<>(shape(embedDim * 4, embedDim));
			PackedCollection<?> w3 = new PackedCollection<>(shape(embedDim, embedDim * 4));

			// Position encoding
			PackedCollection<?> freqCis = new PackedCollection<>(shape(maxSeqLen, dimHead / 2, 2));

			// Store all weights in map for later loading
			String blockPrefix = "transformerBlocks[" + i + "]";

			// Self-attention weights
			weightMap.put(blockPrefix + ".selfAttention.rmsWeight", rmsAttWeight);
			weightMap.put(blockPrefix + ".selfAttention.wq", wq);
			weightMap.put(blockPrefix + ".selfAttention.wk", wk);
			weightMap.put(blockPrefix + ".selfAttention.wv", wv);
			weightMap.put(blockPrefix + ".selfAttention.wo", wo);

			// Cross-attention weights
			if (hasCrossAttention) {
				weightMap.put(blockPrefix + ".crossAttention.rmsWeight", crossAttRmsWeight);
				weightMap.put(blockPrefix + ".crossAttention.wq", crossWq);
				weightMap.put(blockPrefix + ".crossAttention.wk", crossWk);
				weightMap.put(blockPrefix + ".crossAttention.wv", crossWv);
				weightMap.put(blockPrefix + ".crossAttention.wo", crossWo);
			}

			// Feed-forward weights
			weightMap.put(blockPrefix + ".feedForward.rmsWeight", rmsFfnWeight);
			weightMap.put(blockPrefix + ".feedForward.w1", w1);
			weightMap.put(blockPrefix + ".feedForward.w2", w2);
			weightMap.put(blockPrefix + ".feedForward.w3", w3);

			// Position encoding
			weightMap.put(blockPrefix + ".position.freqCis", freqCis);

			// Create position supplier
			Producer<PackedCollection<?>> position = p(new PackedCollection<>(1));

			// Add transformer block with proper context
			main.add(transformerBlock(batchSize, embedDim, numHeads,
					hasCrossAttention, condTokenDim,
					hasGlobalCond, condEmbed));
		}

		// Reshape back to channels-first format if needed
		if (patchSize > 1) {
			main.add(layer("unpatchify",
					shape(1, -1, embedDim * patchSize),
					shape(1, embedDim, -1),
					in -> {
						// Implementation for unpatchify operation
						return reshape(shape(1, embedDim, -1), in);
					}));
		} else {
			// Reshape from [batch, seq_len, channels] back to [batch, channels, seq_len]
			main.reshape(batchSize, -1, embedDim)
					.enumerate(1, 2, 1)
					.reshape(batchSize, embedDim, -1);
		}

		// Output projection
		PackedCollection<?> outputProjWeight = new PackedCollection<>(shape(embedDim, ioChannels));
		PackedCollection<?> outputProjBias = new PackedCollection<>(shape(ioChannels));
		weightMap.put("outputProjection.weight", outputProjWeight);
		weightMap.put("outputProjection.bias", outputProjBias);

		main.add(convolution1d(batchSize, embedDim, ioChannels, 1, 0));

		return model.compile();
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