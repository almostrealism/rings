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

package org.almostrealism.audio.synth;

import io.almostrealism.relation.Factor;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.collect.PackedCollection;

public class NoiseGenerator implements StatelessSource, SamplingFeatures {

	public NoiseGenerator() {
	}

	@Override
	public Producer<PackedCollection<?>> generate(BufferDetails buffer,
												  Producer<PackedCollection<?>> params,
												  Factor<PackedCollection<?>> frequency) {
		double amp = 0.1;

		return sampling(buffer.getSampleRate(), () -> {
			// Normally distributed noise is around 3 times the amplitude of uniform noise
			double scale = amp / 3.0;

			// Generate a series of normally distributed random numbers
			Producer<PackedCollection<?>> series = randn(buffer.getFrames());
			return multiply(traverseEach(series), c(scale));
		});
	}
}
