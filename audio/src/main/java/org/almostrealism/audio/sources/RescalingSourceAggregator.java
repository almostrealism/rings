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

import io.almostrealism.code.ComputeRequirement;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.time.computations.FourierTransform;

public class RescalingSourceAggregator implements SourceAggregator, CellFeatures {
	private int fftBins = WaveData.FFT_BINS;

	protected FourierTransform fft(Producer<PackedCollection<?>> input) {
		return fft(fftBins, input, ComputeRequirement.CPU);
	}

	protected FourierTransform ifft(Producer<PackedCollection<?>> input) {
		return ifft(fftBins, input, ComputeRequirement.CPU);
	}

	@Override
	public Producer<PackedCollection<?>> aggregate(BufferDetails buffer,
								   Producer<PackedCollection<?>> params,
								   Producer<PackedCollection<?>> frequency,
								   Producer<PackedCollection<?>>... sources) {
		CollectionProducer<PackedCollection<?>> input = c(sources[0]);
		CollectionProducer<PackedCollection<?>> filter = c(sources[1]);

		filter = fft(filter).reshape(2, fftBins);
		filter = complexFromParts(
					subset(shape(1, fftBins), filter, 0, 0),
					subset(shape(1, fftBins), filter, 1, 0)).magnitude();
		filter = filter.reshape(fftBins).repeat(2);

		input = fft(input).reshape(2, fftBins);
		return ifft(input.multiply(filter));
	}
}
