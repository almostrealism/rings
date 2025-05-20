package org.almostrealism.ml.audio;

import io.almostrealism.collect.TraversalPolicy;
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

	default Block timestepEmbedding(int batchSize, int embedDim) {
		SequentialBlock embedding = new SequentialBlock(shape(batchSize, 1));

		// Fourier embedding followed by MLP
		embedding.add(fourierFeatures(batchSize, 1, 256));
		embedding.add(dense(256, embedDim));
		embedding.add(silu(shape(embedDim)));
		embedding.add(dense(embedDim, embedDim));

		return embedding;
	}

	default Block convolution1d(int batchSize, int inputChannels, int outputChannels,
								int seqLength, int kernelSize, int padding) {
		return convolution1d(batchSize, inputChannels, outputChannels, seqLength, kernelSize, padding,
				new PackedCollection<>(shape(outputChannels, inputChannels, kernelSize)),
				new PackedCollection<>(shape(outputChannels)));
	}

	default Block convolution1d(int batchSize, int inputChannels, int outputChannels,
								int seqLength, int kernelSize, int padding,
								PackedCollection<?> weights, PackedCollection<?> bias) {
		TraversalPolicy inputShape = shape(batchSize, inputChannels, seqLength);
		TraversalPolicy outputShape = shape(batchSize, outputChannels, seqLength);

		return layer("convolution1d",
				inputShape, outputShape,
				input -> {
					// Implementation using matrix multiplication for 1D convolution
					CollectionProducer<PackedCollection<?>> paddedInput;
					if (padding > 0) {
						// Add padding - this would need a proper implementation
						// For now, we'll just use the input without padding
						paddedInput = c(input);
					} else {
						paddedInput = c(input);
					}

					// Extract sequence length
					int seqLen = paddedInput.getShape().length(2);

					// Reshape for matrix multiplication
					CollectionProducer<PackedCollection<?>> output =
							matmul(cp(weights.reshape(outputChannels, inputChannels * kernelSize)),
									paddedInput.reshape(batchSize, inputChannels * kernelSize, seqLen))
									.reshape(batchSize, outputChannels, seqLen);

					// Add bias
					output = output.add(cp(bias).expand(seqLen));

					return output;
				},
				List.of(weights, bias));
	}

	default Block transformerBlock(int batchSize, int dim, int seqLen, int heads,
								   boolean crossAttend, int contextDim,
								   int contextSeqLen, boolean globalCond, Block context,
								   // Self-attention weights
								   PackedCollection<?> selfAttRmsWeight,
								   PackedCollection<?> selfWq, PackedCollection<?> selfWk,
								   PackedCollection<?> selfWv, PackedCollection<?> selfWo,
								   PackedCollection<?> freqCis,
								   // Cross-attention weights
								   PackedCollection<?> crossAttRmsWeight,
								   PackedCollection<?> crossWq, PackedCollection<?> crossWk,
								   PackedCollection<?> crossWv, PackedCollection<?> crossWo,
								   // Feed-forward weights
								   PackedCollection<?> rmsFfnWeight,
								   PackedCollection<?> w1, PackedCollection<?> w2, PackedCollection<?> w3) {
		SequentialBlock block = new SequentialBlock(shape(batchSize, seqLen, dim));
		int dimHead = dim / heads;

		// Create self-attention block with sequence processing
		Block selfAttention = sequenceAttention(
								batchSize, seqLen, heads, selfAttRmsWeight,
								selfWk, selfWv, selfWq, selfWo, freqCis);
		block.add(residual(preNorm(selfAttention)));

		// Cross-attention (if needed)
		if (crossAttend) {
			if (context == null) {
				throw new IllegalArgumentException("Context block cannot be null for cross-attention");
			}

			// Create cross-attention block with context
			Block crossAttention = crossAttention(
					1, seqLen, contextSeqLen, heads, dimHead,
					crossAttRmsWeight, crossWk, crossWv, crossWq, crossWo, context);
			block.add(residual(preNorm(crossAttention)));
		}

		// Feed-forward block
		Block feedForward = feedForward(block.getOutputShape(), rmsFfnWeight, w1, w2, w3);
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