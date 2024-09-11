/*
 * Copyright 2023 Michael Murray
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
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.layers.CellularLayer;
import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.Model;
import org.almostrealism.model.SequentialBlock;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

public class UNetTest implements DiffusionFeatures {
	int batchSize = 1;
	int channels = 1;
	int dimFactors[] = { 1, 2, 4, 8 };


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


	protected Block resNetBlock(int dim, int dimOut, int rows, int cols) {
		return resNetBlock(dim, dimOut, -1, null, rows, cols);
	}

	protected Block resNetBlock(int dim, int dimOut, int timeEmbedDim, Block time,
								int rows, int cols) {
		return resNetBlock(dim, dimOut, timeEmbedDim, time, 8, rows, cols);
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
		CellularLayer resConv = dim != dimOut ?
				resNet.branch(convolution2d(dim, dimOut, 1, 0)) : null;

		resNet.add(block(dim, dimOut, groups, rows, cols, scaleShift));
		resNet.add(block(dimOut, dimOut, groups, rows, cols));
		if (resConv != null) resNet.accum(resConv);
		return resNet;
	}

	protected Function<TraversalPolicy, CellularLayer> similarity(
			Block k, int heads, int dimHead, int size) {
		if (k.getOutputShape().getDimensions() != 4 ||
				k.getOutputShape().length(1) != heads ||
				k.getOutputShape().length(2) != dimHead ||
				k.getOutputShape().length(3) != size) {
			throw new IllegalArgumentException();
		}

		return compose("similarity", k, shape(batchSize, heads, size, size), (a, b) -> {
			 CollectionProducer<PackedCollection<?>> pa = c(a)
							.traverse(1)
							.enumerate(2, 1)
							.traverse(2)
							.repeat(size);
					CollectionProducer<PackedCollection<?>> pb = c(b)
							.traverse(1)
							.enumerate(2, 1)
							.repeat(size);
					return multiply(pa, pb).sum(3);
		});
	}

	protected Function<TraversalPolicy, CellularLayer> weightedSum(
			Block v, int heads, int dimHead, int size) {
		if (v.getOutputShape().getDimensions() != 4 ||
				v.getOutputShape().length(1) != heads ||
				v.getOutputShape().length(2) != dimHead ||
				v.getOutputShape().length(3) != size) {
			throw new IllegalArgumentException();
		}

		return compose("weightedSum", v, (a, b) -> {
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

	public Block attention(int dim, int rows, int cols) {
		return attention(dim, 4, 32);
	}

	public Block attention(int dim, int heads, int dimHead, int rows, int cols) {
		double scale = 1.0 / Math.sqrt(dimHead);
		int hiddenDim = dimHead * heads;
		int size = rows * cols;

		TraversalPolicy componentShape = shape(batchSize, heads, dimHead, size);

		SequentialBlock attention = new SequentialBlock(shape(batchSize, channels, rows, cols));
		attention.add(convolution2d(dim, hiddenDim * 3, 1, 0, false));
		attention
				.reshape(batchSize, 3, hiddenDim * size)
				.enumerate(shape(batchSize, 1, hiddenDim * size))
				.reshape(3, batchSize, heads, dimHead, size);

		List<Block> qkv = attention.split(componentShape, 0);
		Block k = qkv.get(1);
		Block v = qkv.get(2);

		attention.add(scale(scale));
		attention.add(similarity(k, heads, dimHead, size));
		attention.add(softmax(true));
		attention.add(weightedSum(v, heads, dimHead, size));
		attention.reshape(batchSize, size, hiddenDim)
				.enumerate(1, 2, 1)
				.reshape(batchSize, hiddenDim, rows, cols);
		return attention;
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

	@Test
	public void unet() {
		int dim = 28;
		int initDim = dim;

		int width = dim, height = dim;
		int timeInputDim = dim;
		int timeEmbedDim = dim * 4;

		Block timeEmbedding = timestepEmbeddings(timeInputDim, timeEmbedDim);

		Model unet = new Model(shape(height, width));
		unet.add(convolution2d(dim, 1));

		// for (int i = 0; i <)
	}
}
