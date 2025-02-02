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

package org.almostrealism.rml.unet.test;

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.profile.OperationProfileNode;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGBFeatures;
import org.almostrealism.hardware.HardwareOperator;
import org.almostrealism.hardware.metal.MetalMemoryProvider;
import org.almostrealism.io.Console;
import org.almostrealism.io.OutputFeatures;
import org.almostrealism.layers.CellularLayer;
import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.Model;
import org.almostrealism.model.SequentialBlock;
import org.almostrealism.util.SignalWireDeliveryProvider;
import org.almostrealism.util.TestFeatures;
import org.almostrealism.util.TestUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class UNetTest implements DiffusionFeatures, RGBFeatures, TestFeatures {

	static {
		if (TestUtils.getTrainTests()) {
			HardwareOperator.enableLargeInstructionSetMonitoring = true;
			MetalMemoryProvider.enableLargeAllocationLogging = true;

			Console.root().addListener(OutputFeatures.fileOutput("results/logs/train.out"));

			SignalWireDeliveryProvider.attachDefault();
		}
	}

	int batchSize = 1;
	int channels = 1;
	int dimFactors[] = { 1, 2, 4 };
	// int dimFactors[] = { 1, 2, 4, 8 };


	protected Block block(int dim, int dimOut, int rows, int cols) {
		return block(dim, dimOut, 8, rows, cols, null);
	}

	protected Block block(int dim, int dimOut, int groups, int rows, int cols) {
		return block(dim, dimOut, groups, rows, cols, null);
	}

	protected Block block(int dim, int dimOut, int groups, int rows, int cols, Block scaleShift) {
		SequentialBlock block = new SequentialBlock(shape(batchSize, dim, rows, cols));
		block.add(convolution2d(dimOut, 3, 1));
		block.add(norm(groups));

		if (scaleShift != null) {
			if (scaleShift.getOutputShape().getDimensions() != 5 ||
					scaleShift.getOutputShape().length(1) != batchSize) {
				throw new IllegalArgumentException();
			}

			block.add(compose("scaleShift", scaleShift,
					(in, ss) -> {
						TraversalPolicy shape = scaleShift.getOutputShape().traverse(1).item();
						CollectionProducer<PackedCollection<?>> scale =
								subset(shape.prependDimension(1), ss, 0, 0, 0, 0, 0);
						CollectionProducer<PackedCollection<?>> shift =
								subset(shape.prependDimension(1), ss, 1, 0, 0, 0, 0);
						return multiply(in, scale.add(1.0)).add(shift);
					}));
		}

		block.add(silu());
		return block;
	}

	protected Block mlp(int dim, int dimOut) {
		SequentialBlock mlp = new SequentialBlock(shape(batchSize, dim));
		mlp.add(silu());
		mlp.add(dense(dim, dimOut));
		return mlp;
	}

	protected Block resNetBlock(int dim, int dimOut, int timeEmbedDim, Block time,
								int rows, int cols) {
		return resNetBlock(dim, dimOut, timeEmbedDim, time, 8, rows, cols);
	}

	protected Function<TraversalPolicy, Block> resNetBlock(int dim, int dimOut, int timeEmbedDim,
														   Block time, int groups) {
		return shape -> {
			if (shape.getDimensions() != 4) {
				throw new IllegalArgumentException();
			}

			int rows = shape.length(2);
			int cols = shape.length(3);
			return resNetBlock(dim, dimOut, timeEmbedDim, time, groups, rows, cols);
		};
	}

	protected Block resNetBlock(int dim, int dimOut, int timeEmbedDim, Block time,
								int groups, int rows, int cols) {
		Block scaleShift = null;

		if (timeEmbedDim > 0) {
			Block mlp = time.andThen(mlp(timeEmbedDim, dimOut * 2));
			scaleShift = mlp
					.reshape(batchSize, 2, dimOut)
					.enumerate(shape(batchSize, 1, dimOut))
					.reshape(2, batchSize, dimOut, 1, 1);
		}

		SequentialBlock resNet = new SequentialBlock(shape(batchSize, dim, rows, cols));
		Block resConv = dim != dimOut ?
				resNet.branch(convolution2d(dim, dimOut, 1, 0)) : null;

		resNet.add(block(dim, dimOut, groups, rows, cols, scaleShift));
		resNet.add(block(dimOut, dimOut, groups, rows, cols));
		if (resConv != null) resNet.accum(resConv, false);
		log("\tResNet[" + dim + "," + dimOut + "]: " + resNet.getOutputShape());
		return resNet;
	}


	public Function<TraversalPolicy, CellularLayer> weightedSum(
			Block v, int heads, int dimHead, int size) {
		if (v.getOutputShape().getDimensions() != 4 ||
				v.getOutputShape().length(1) != heads ||
				v.getOutputShape().length(2) != dimHead ||
				v.getOutputShape().length(3) != size) {
			throw new IllegalArgumentException();
		}

		return compose("weightedSum", v, shape(batchSize, heads, size, dimHead),
				(a, b) -> {
					CollectionProducer<PackedCollection<?>> pa = c(a)
							.traverse(4)
							.repeat(dimHead);
					CollectionProducer<PackedCollection<?>> pb = c(b)
							.traverse(2)
							.enumerate(3, 1)
							.traverse(2)
							.repeat(size);
					return multiply(pa, pb)
							.reshape(batchSize, heads, size, size, dimHead)
							.traverse(3)
							.enumerate(4, 1)
							.sum(4);
		});
	}

	protected Function<TraversalPolicy, CellularLayer> context(Block v, int heads, int dimHead, int size) {
		if (v.getOutputShape().getDimensions() != 4 ||
				v.getOutputShape().length(1) != heads ||
				v.getOutputShape().length(2) != dimHead ||
				v.getOutputShape().length(3) != size) {
			throw new IllegalArgumentException();
		}

		return compose("context", v, shape(batchSize, heads, dimHead, dimHead), (a, b) -> {
			CollectionProducer<PackedCollection<?>> pa = c(a)
					.traverse(3)
					.repeat(dimHead);
			CollectionProducer<PackedCollection<?>> pb = c(b)
					.traverse(2)
					.repeat(dimHead);
			return multiply(pa, pb).sum(4);
		});
	}

	public Function<TraversalPolicy, Block> attention(int dim) {
		return shape -> attention(dim, shape.length(1), shape.length(2), shape.length(3));
	}

	public Block attention(int dim, int inputChannels, int rows, int cols) {
		return attention(dim, 4, 32, inputChannels, rows, cols);
	}

	public Block attention(int dim, int heads, int dimHead,
						   int inputChannels, int rows, int cols) {
		double scale = 1.0 / Math.sqrt(dimHead);
		int hiddenDim = dimHead * heads;
		int size = rows * cols;

		TraversalPolicy componentShape = shape(batchSize, heads, dimHead, size);

		SequentialBlock attention = new SequentialBlock(shape(batchSize, inputChannels, rows, cols));
		attention.add(convolution2d(dim, hiddenDim * 3, 1, 0, false));
		attention
				.reshape(batchSize, 3, hiddenDim * size)
				.enumerate(shape(batchSize, 1, hiddenDim * size))
				.reshape(3, batchSize, heads, dimHead, size);

		List<Block> qkv = attention.split(componentShape, 0);
		Block k = qkv.get(1);
		Block v = qkv.get(2);

		attention.add(scale(scale));
		attention.add(similarity(k, heads, size, size));
		attention.add(softmax(true));
		attention.add(weightedSum(v, heads, dimHead, size));
		attention.reshape(batchSize, size, hiddenDim)
				.enumerate(1, 2, 1)
				.reshape(batchSize, hiddenDim, rows, cols);
		attention.add(convolution2d(hiddenDim, dim, 1, 0));
		return attention;
	}

	public Function<TraversalPolicy, Block> linearAttention(int dim) {
		return shape -> {
			int inputChannels = shape.length(1);
			int rows = shape.length(2);
			int cols = shape.length(3);
			return linearAttention(dim, inputChannels, rows, cols);
		};
	}

	public Block linearAttention(int dim, int inputChannels, int rows, int cols) {
		return linearAttention(dim, 4, 32, inputChannels, rows, cols);
	}

	public Block linearAttention(int dim, int heads, int dimHead,
								 int inputChannels, int rows, int cols) {
		double scale = 1.0 / Math.sqrt(dimHead);
		int hiddenDim = dimHead * heads;
		int size = rows * cols;

		TraversalPolicy shape = shape(batchSize, inputChannels, rows, cols);
		TraversalPolicy componentShape = shape(batchSize, heads, dimHead, size);

		SequentialBlock attention = new SequentialBlock(shape);
		attention.add(convolution2d(dim, hiddenDim * 3, 1, 0, false));

		attention
				.reshape(batchSize, 3, hiddenDim * size)
				.enumerate(shape(batchSize, 1, hiddenDim * size))
				.reshape(3, batchSize, heads, dimHead, size);

		List<Block> qkv = attention.split(componentShape, 1);
		Block q = qkv.get(0)
				.andThen(scale(scale))
				.reshape(batchSize, heads, dimHead * size)
				.andThen(softmax(false))
				.reshape(batchSize, heads, dimHead, size);
		Block v = qkv.get(2);

		attention.add(softmax(false));
		attention.add(context(v, heads, dimHead, size));
		attention.add(similarity(q, heads, dimHead, size));
		attention.reshape(batchSize, hiddenDim, rows, cols);
		attention.add(convolution2d(hiddenDim, dim, 1, 0));
		attention.add(norm());

		if (!attention.getOutputShape().equalsIgnoreAxis(shape)) {
			throw new IllegalArgumentException();
		}

		return attention;
	}

	protected Function<TraversalPolicy, Block> preNorm(Function<TraversalPolicy, Block> block) {
		return shape -> preNorm(block.apply(shape));
	}

	protected Block preNorm(Block block) {
		SequentialBlock out = new SequentialBlock(block.getInputShape());
		out.add(norm());
		out.add(block);
		return block;
	}

	@Test
	public void resNet() {
		int initDim = 28;
		int rows = 28, cols = 28;

		int timeInputDim = initDim;
		int timeEmbedDim = initDim * 4;

		Block timeEmbedding = timestepEmbeddings(timeInputDim, timeEmbedDim);

		CompiledModel resNet =
				new Model(shape(batchSize, channels, rows, cols))
					.addInput(timeEmbedding)
					.add(resNetBlock(batchSize, channels, timeEmbedDim, timeEmbedding, rows, cols))
					.compile();

		resNet.forward(
				new PackedCollection<>(batchSize, channels, rows, cols).randnFill(),
				new PackedCollection<>(batchSize, timeInputDim).randnFill());
	}

	protected Block sinPositionEmbeddings(int dim) {
		int hd = dim / 2;
		double scale = Math.log(10000) / (hd - 1);

		PackedCollection<?> values = new PackedCollection<>(hd).fill(pos -> pos[0] * -scale);

		return layer("sinEmbed", shape(batchSize, 1), shape(batchSize, dim), (in) -> {
			CollectionProducer<PackedCollection<?>> embeddings =
					multiply(
							c(in).repeat(1, hd).reshape(batchSize, hd),
							cp(values).repeat(batchSize).reshape(batchSize, hd));
			return concat(shape(batchSize, dim), sin(embeddings), cos(embeddings));
		});
	}

	protected Block sinTimestepEmbeddings(int dim, int timeLen) {
		return sinTimestepEmbeddings(dim, timeLen, timeLen);
	}

	protected Block sinTimestepEmbeddings(int dim, int timeLen, int outLen) {
		SequentialBlock block = new SequentialBlock(shape(batchSize, 1));
		block.add(sinPositionEmbeddings(dim));
		block.add(dense(dim, timeLen));
		block.add(gelu(shape(timeLen)));
		block.add(dense(timeLen, outLen));
		return block;
	}

	protected Function<TraversalPolicy, Block> residual(Function<TraversalPolicy, Block> block) {
		return shape -> residual(block.apply(shape));
	}

	protected Block residual(Block block) {
		if (block.getInputShape().getTotalSize() != block.getOutputShape().getTotalSize())
			throw new IllegalArgumentException();

		SequentialBlock residual = new SequentialBlock(block.getInputShape());
		residual.accum(block);
		return residual;
	}

	protected Function<TraversalPolicy, Block> upsample(int dim) {
		return upsample(dim, dim);
	}

	protected Function<TraversalPolicy, Block> upsample(int dim, int dimOut) {
		return shape -> {
			int inputChannels = shape.length(1);
			int h = shape.length(2);
			int w = shape.length(3);

			SequentialBlock upsample = new SequentialBlock(shape(batchSize, inputChannels, h, w));
			upsample.add(layer("repeat2d",
					shape(batchSize, inputChannels, h, w),
					shape(batchSize, inputChannels, h * 2, w * 2),
					(in) ->
							c(in)
									.repeat(4, 2)
									.repeat(3, 2)
									.reshape(batchSize, inputChannels, h * 2, w * 2)));
			upsample.add(convolution2d(dim, dimOut, 3, 1));
			return upsample;
		};
	}

	protected Function<TraversalPolicy, Block> downsample(int dim) {
		return downsample(dim, dim);
	}

	protected Function<TraversalPolicy, Block> downsample(int dim, int dimOut) {
		return shape -> {
			int inputChannels = shape.length(1);
			int h = shape.length(2);
			int w = shape.length(3);

			SequentialBlock downsample = new SequentialBlock(shape(batchSize, inputChannels, h, w));
			downsample.add(layer("enumerate",
					shape(batchSize, inputChannels, h, w),
					shape(batchSize, inputChannels * 4, h / 2, w / 2),
					in -> c(in).traverse(2)
							.enumerate(3, 2)
							.enumerate(3, 2)
							.reshape(batchSize, inputChannels, (h * w) / 4, 4)
							.traverse(2)
							.enumerate(3, 1)
							.reshape(batchSize, inputChannels * 4, h / 2, w / 2)));
			downsample.add(convolution2d(dim * 4, dimOut, 1, 0));
			return downsample;
		};
	}

	protected Model unet(int dim) {
		return unet(dim, null, null, false, 4);
	}

	protected Model unet(int dim, Integer initDim, Integer outDim,
						boolean selfCondition, int resnetBlockGroups) {
		int width = dim, height = dim;

		int inputChannels = selfCondition ? channels * 2 : channels;
		int initDimValue = (initDim != null) ? initDim : dim;
		int outDimValue = (outDim != null) ? outDim : channels;

		Model unet = new Model(shape(batchSize, inputChannels, height, width));
		unet.add(convolution2d(inputChannels, initDimValue, 1, 0));

		int[] dims = new int[dimFactors.length + 1];
		dims[0] = initDimValue;
		for (int i = 0; i < dimFactors.length; i++) {
			dims[i + 1] = dim * dimFactors[i];
		}

		int timeDim = dim * 4;
		Block timeMlp = sinTimestepEmbeddings(dim, timeDim);
		unet.addInput(timeMlp);

		SequentialBlock main = unet.sequential();
		Block residual = main.branch();

		Stack<Block> featureMaps = new Stack<>();

		for (int i = 0; i < dims.length - 1; i++) {
			boolean isLast = i >= dims.length - 2;
			SequentialBlock downBlock = new SequentialBlock(main.getOutputShape());
			log("DownBlock[" + i + "]: " + dims[i] + " -> " + dims[i + 1]);

			downBlock.add(resNetBlock(dims[i], dims[i], timeDim, timeMlp.branch(), resnetBlockGroups));
			featureMaps.push(downBlock.branch());
			log("\t\tAdded feature map " + featureMaps.peek().getOutputShape());

			downBlock.add(resNetBlock(dims[i], dims[i], timeDim, timeMlp.branch(), resnetBlockGroups));
			downBlock.add(residual(preNorm(linearAttention(dims[i]))));
			featureMaps.push(downBlock.branch());
			log("\t\tAdded feature map " + featureMaps.peek().getOutputShape());

			if (!isLast) {
				downBlock.add(downsample(dims[i], dims[i + 1]));
			} else {
				downBlock.add(convolution2d(dims[i], dims[i + 1], 3, 1));
			}

			main.add(downBlock);
		}

		main.add(resNetBlock(dims[dims.length - 1], dims[dims.length - 1], timeDim, timeMlp.branch(), resnetBlockGroups));
		main.add(residual(preNorm(attention(dims[dims.length - 1]))));
		main.add(resNetBlock(dims[dims.length - 1], dims[dims.length - 1], timeDim, timeMlp.branch(), resnetBlockGroups));

		for (int i = dims.length - 1; i > 0; i--) {
			boolean isLast = i == 1;
			SequentialBlock upBlock = new SequentialBlock(main.getOutputShape());

			int dimIn = dims[i - 1];
			int dimOut = dims[i];
			int totalDim = dimIn + dimOut;
			log("UpBlock[" + i + "]: (" + dimIn + "+" + dimOut + ") " + totalDim + " -> " + dimOut);

			log(upBlock.getOutputShape());
			log("\t\tConnecting feature map " + featureMaps.peek().getOutputShape());
			upBlock.add(concat(1, featureMaps.pop()));
			upBlock.add(resNetBlock(totalDim, dimOut, timeDim, timeMlp.branch(), resnetBlockGroups));

			log(upBlock.getOutputShape());
			log("\t\tConnecting feature map " + featureMaps.peek().getOutputShape());
			upBlock.add(concat(1, featureMaps.pop()));
			upBlock.add(resNetBlock(totalDim, dimOut, timeDim, timeMlp.branch(), resnetBlockGroups));
			upBlock.add(residual(preNorm(linearAttention(dimOut))));

			if (!isLast) {
				upBlock.add(upsample(dimOut, dimIn));
			} else {
				upBlock.add(convolution2d(dimOut, dimIn, 3, 1));
			}

			main.add(upBlock);
		}

		main.add(concat(1, residual));
		main.add(resNetBlock(dim * 2, dim, timeDim, timeMlp.branch(), resnetBlockGroups));
		main.add(convolution2d(dim, outDimValue, 1, 0));
		return unet;
	}

	public CollectionProducer<PackedCollection<?>> linearBetaSchedule(int timesteps) {
		double betaStart = 0.0001;
		double betaEnd = 0.02;
		return linear(betaStart, betaEnd, timesteps);
	}

	public <T extends PackedCollection<?>> CollectionProducer<T> cumulativeProduct(Producer<T> input, boolean pad) {
		return func(shape(input), inputs -> args -> {
			PackedCollection<?> in = inputs[0];
			PackedCollection<?> result = new PackedCollection<>(in.getShape());

			double r = 1.0;
			int offset = 0;

			if (pad) {
				result.setMem(0, r);
				offset = 1;
			}

			for (int i = offset; i < in.getMemLength(); i++) {
				r *= in.toDouble(i - offset);
				result.setMem(i, r);
			}

			return result;
		}, input);
	}

	public CollectionProducer<PackedCollection<?>> extract(CollectionProducer<PackedCollection<?>> a,
														   CollectionProducer<PackedCollection<?>> t,
														   TraversalPolicy xShape) {
		if (t.getShape().getDimensions() != 1) {
			throw new IllegalArgumentException();
		}

		int batches = t.getShape().length(0);
		CollectionProducer<PackedCollection<?>> out = a.valueAt(integers(0, batches), t);

		int depth = xShape.getDimensions();
		TraversalPolicy resultShape =
				padDimensions(shape(batches), 1, depth, true);
		return out.reshape(resultShape);
	}

	public CollectionProducer<PackedCollection<?>> imageTransform(CollectionProducer<PackedCollection<?>> image) {
		return null;
	}

	@Test
	public void imageTransform() throws IOException {
		CollectionProducer<PackedCollection<?>> data =
				channels(new File("/Users/michael/Desktop/output_cats.jpeg"));
		log(data.getShape());

		saveChannels("results/test_out.png", data).get().run();
	}

	public void runUnet(int dim, OperationProfileNode profile) {
		int timesteps = 300;

		CollectionProducer<PackedCollection<?>> betas = linearBetaSchedule(timesteps);

		CollectionProducer<PackedCollection<?>> alphas = c(1.0).subtract(betas);
		CollectionProducer<PackedCollection<?>> alphasCumProd = cumulativeProduct(alphas, false);
		CollectionProducer<PackedCollection<?>> alphasCumProdPrev = cumulativeProduct(alphas, true);
		CollectionProducer<PackedCollection<?>> sqrtRecipAlphas = sqrt(alphas.reciprocal());

		CollectionProducer<PackedCollection<?>> sqrtAlphasCumProd = sqrt(alphasCumProd);
		CollectionProducer<PackedCollection<?>> sqrtOneMinusAlphasCumProd = sqrt(c(1.0).subtract(alphasCumProd));

		CollectionProducer<PackedCollection<?>> posteriorVariance = betas
				.multiply(c(1.0).subtract(alphasCumProdPrev))
				.divide(c(1.0).subtract(alphasCumProd));

		CompiledModel unet = unet(dim).compile(profile);

		PackedCollection<?> image = new PackedCollection<>(batchSize, channels, dim, dim).randnFill();
		PackedCollection<?> time = new PackedCollection<>(batchSize, 1).randnFill();

		for (int i = 0; i < 200; i++) {
			unet.forward(image, time);
			unet.backward(new PackedCollection<>(unet.getOutputShape()).randFill());
			if (i % 10 == 0) alert("UNet test completed backward pass " + i);
		}
	}

	@Test
	public void unet() throws IOException {
		int dim = 28;

		OperationProfileNode profile = new OperationProfileNode("unet");
		boolean failed = false;

		try {
			profile(profile, () -> {
				runUnet(dim, profile);
			});
		} catch (Exception e) {
			alert("UNet test failed", e);
			failed = true;
		} finally {
			if (!failed)
				alert("UNet test completed");

			profile.save("results/unet.xml");
		}
	}
}
