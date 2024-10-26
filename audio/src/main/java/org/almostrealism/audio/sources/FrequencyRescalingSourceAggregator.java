/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.sources;

import io.almostrealism.compute.ComputeRequirement;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.time.computations.FourierTransform;

public class FrequencyRescalingSourceAggregator implements SourceAggregator, CellFeatures {
	private int fftBins = WaveData.FFT_BINS;

	protected FourierTransform fft(CollectionProducer<PackedCollection<?>> input) {
		int frames = shape(input).getTotalSize();
		int slices = frames / fftBins;

		if (frames % fftBins != 0) {
			slices = slices + 1;
			input = pad(shape(slices * fftBins), input, 0);
		}

		TraversalPolicy shape = shape(slices, 2, fftBins);
		input = pad(shape, input.reshape(slices, 1, fftBins), 0, 0, 0);
		return fft(fftBins, input, ComputeRequirement.CPU);
	}

	protected FourierTransform ifft(CollectionProducer<PackedCollection<?>> input) {
		return ifft(fftBins, input, ComputeRequirement.CPU);
	}

	@Override
	public Producer<PackedCollection<?>> aggregate(BufferDetails buffer,
								   Producer<PackedCollection<?>> params,
								   Producer<PackedCollection<?>> frequency,
								   Producer<PackedCollection<?>>... sources) {
		CollectionProducer<PackedCollection<?>> input = c(sources[0]);
		CollectionProducer<PackedCollection<?>> filter = c(sources[1]);

		filter = fft(filter);
		filter = filter.transpose(2).magnitude(2);

		int slices = filter.getShape().length(0);
		filter = filter.reshape(-1, fftBins)
					.transpose().sum(1)
					.divide(c(slices));

		input = fft(input);

		int inputSlices = input.getShape().length(0);
		filter = filter.traverse(0).repeat(2 * inputSlices)
				.reshape(inputSlices, 2, fftBins);

		CollectionProducer<PackedCollection<?>> result = ifft(input.multiply(filter));
		result = subset(shape(inputSlices, 1, fftBins), result, 0, 0, 0);
		return result.flatten();
	}
}
