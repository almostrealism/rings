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

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.layers.CellularLayer;
import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.almostrealism.model.Model;
import org.almostrealism.model.SequentialBlock;
import org.junit.Test;

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
		if (scaleShift != null)
			throw new UnsupportedOperationException();

		SequentialBlock block = new SequentialBlock(shape(batchSize, dim, rows, cols));
		block.add(convolution2d(dimOut, 3, 1));
		block.add(norm(groups));
		// TODO
		// block.add("scaleShift", in -> multiply(in, subset(scaleShift).add(1)).add(subset(scaleShift)));
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
			mlp = mlp.reshape(batchSize, 2, dimOut);
			scaleShift = mlp
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

	public Block attention(int dim) {
		return attention(dim, 4, 32);
	}

	public Block attention(int dim, int heads, int dimHead) {
		double scale = 1.0 / Math.sqrt(dimHead);
		int hiddenDim = dimHead * heads;

		// TODO
		SequentialBlock attention = new SequentialBlock(shape(batchSize, dim));
		CellularLayer qkv = attention.add(convolution2d(dim, hiddenDim * 3, 1, 0, false));

		return attention;
	}

	@Test
	public void resNet() {
		int initDim = 28;
		int rows = 28, cols = 28;

		int timeInputDim = initDim;
		int timeEmbedDim = initDim * 4;

		Block timeEmbedding = timestepEmbeddings(timeInputDim, timeEmbedDim);

		Model resNet = new Model(shape(batchSize, channels, rows, cols));
		resNet.addBlock(resNetBlock(batchSize, channels, timeEmbedDim, timeEmbedding, rows, cols));

		resNet.compile().forward(
				new PackedCollection<>(batchSize, channels, rows, cols).randnFill());
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
		unet.addLayer(convolution2d(dim, 1));

		// for (int i = 0; i <)
	}
}
