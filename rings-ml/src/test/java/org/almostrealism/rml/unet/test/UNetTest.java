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

import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.almostrealism.model.Model;
import org.almostrealism.model.SequentialBlock;
import org.junit.Test;

public class UNetTest implements DiffusionFeatures {
	int dimFactors[] = { 1, 2, 4, 8 };

	protected Block block(int dim, int dimOut, int groups) {
		SequentialBlock block = new SequentialBlock(shape(dim, dimOut));
		block.add(convolution2d(dim, dimOut));
		block.add(norm(shape(dimOut), groups));
		block.add(silu(shape(dimOut)));
		return block;

	}

	protected Block resNetAttentionBlock(int dimIn, int dimOut) {
		throw new UnsupportedOperationException();
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
		unet.addLayer(convolution2d(1, dim));

		// for (int i = 0; i <)
	}
}
