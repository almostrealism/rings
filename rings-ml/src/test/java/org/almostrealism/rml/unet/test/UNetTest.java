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
import org.almostrealism.ml.DiffusionFeatures;
import org.almostrealism.model.Block;
import org.junit.Test;

public class UNetTest implements DiffusionFeatures {
	int blockOutChannels[] = { 128, 128, 256, 256, 512, 512 };

	@Test
	public void unet() {
		int width = 256, height = 256;
		int timeInputDim = blockOutChannels[0];
		int timeEmbedDim = blockOutChannels[0] * 4;

		TraversalPolicy inputShape = new TraversalPolicy(height, width);

		Block timeEmbedding = timestepEmbeddings(timeInputDim, timeEmbedDim);
	}
}
