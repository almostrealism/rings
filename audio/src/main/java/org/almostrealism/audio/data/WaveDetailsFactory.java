/*
 * Copyright 2025 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.data;

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.collect.PackedCollection;

public class WaveDetailsFactory implements CodeFeatures {

	public static int defaultBins = 32; // 16;
	public static double defaultWindow = 0.25; // 0.125;

	protected static WaveDetailsFactory defaultFactory;

	private int sampleRate;
	private double fftSampleRate;

	private int freqBins;
	private int scaleBins, scaleTime;
	private PackedCollection<?> buffer;
	private Evaluable<PackedCollection<?>> sum;

	private WaveDataFeatureProvider featureProvider;

	public WaveDetailsFactory(int sampleRate) {
		this(defaultBins, defaultWindow, sampleRate);
	}

	public WaveDetailsFactory(int freqBins, double sampleWindow, int sampleRate) {
		if (WaveData.FFT_POOL_BINS % freqBins != 0) {
			throw new IllegalArgumentException("FFT bins must be a factor of " + WaveData.FFT_POOL_BINS);
		}

		this.sampleRate = sampleRate;
		this.freqBins = freqBins;
		this.scaleBins = WaveData.FFT_POOL_BINS / freqBins;

		this.fftSampleRate = sampleRate / (double) (WaveData.FFT_BINS * WaveData.FFT_POOL);
		this.scaleTime = (int) (sampleWindow * fftSampleRate);

		buffer = new PackedCollection(shape(scaleTime, WaveData.FFT_POOL_BINS));
		sum = cv(shape(scaleTime, WaveData.FFT_POOL_BINS, 1), 0)
				.enumerate(2, 1)
				.enumerate(2, scaleBins)
				.enumerate(2, scaleTime)
				.traverse(3)
				.sum()
				.get();
	}

	public int getSampleRate() { return sampleRate; }

	public WaveDataFeatureProvider getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(WaveDataFeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
	}

	public WaveDetails forFile(String file) {
		return forProvider(new FileWaveDataProvider(file), null);
	}

	public WaveDetails forProvider(WaveDataProvider provider) {
		return forProvider(provider, null);
	}

	public WaveDetails forProvider(WaveDataProvider provider, WaveDetails existing) {
		return forWaveData(provider.getIdentifier(), provider.get(), existing);
	}

	public WaveDetails forWaveData(String identifier, WaveData data, WaveDetails existing) {
		if (data == null) return existing;

		if (existing == null) {
			existing = new WaveDetails(identifier, data.getSampleRate());
		}

		if (data.getSampleRate() != getSampleRate()) {
			return existing;
		}

		existing.setSampleRate(data.getSampleRate());
		existing.setChannelCount(data.getChannelCount());
		existing.setFrameCount(data.getFrameCount());
		existing.setData(data.getData());

		if (existing.getFreqFrameCount() <= 1) {
			// TODO  FFT should be performed on all channels, not just the first
			PackedCollection<?> fft = processFft(data.fft(0, true));
			if (fft.getShape().length(0) < 1) {
				throw new UnsupportedOperationException();
			}

			existing.setFreqSampleRate(fftSampleRate / scaleTime);
			existing.setFreqChannelCount(1);
			existing.setFreqBinCount(freqBins);
			existing.setFreqFrameCount(fft.getShape().length(0));
			existing.setFreqData(fft);
		}

		if (featureProvider != null) {
			PackedCollection<?> features = prepareFeatures(data);
			existing.setFeatureSampleRate(featureProvider.getSampleRate());
			existing.setFeatureChannelCount(1);
			existing.setFeatureBinCount(features.getShape().length(1));
			existing.setFeatureFrameCount(features.getShape().length(0));
			existing.setFeatureData(features);
		}

		return existing;
	}

	public double similarity(WaveDetails a, WaveDetails b) {
		if (a.getFeatureData() != null && b.getFeatureData() != null) {
			return WaveDetails.similarity(a.getFeatureData(), b.getFeatureData());
		} else {
			return WaveDetails.similarity(a.getFreqData(), b.getFreqData());
		}
	}

	protected PackedCollection<?> processFft(PackedCollection<?> fft) {
		if (fft.getShape().length(0) < 1) {
			throw new IllegalArgumentException();
		}

		int count = fft.getShape().length(0) / scaleTime;
		if (fft.getShape().length(0) % scaleTime != 0) count++;

		TraversalPolicy inShape = shape(scaleTime, WaveData.FFT_POOL_BINS);
		PackedCollection<?> output = new PackedCollection<>(count, freqBins, 1);

		for (int i = 0; i < count; i++) {
			PackedCollection in;

			if (fft.getMemLength() < (i + 1) * inShape.getTotalSize()) {
				buffer.setMem(0, fft, i * inShape.getTotalSize(), fft.getMemLength() - i * inShape.getTotalSize());
				in = buffer;
			} else {
				in = fft.range(inShape, i * inShape.getTotalSize());
			}

			PackedCollection<?> out = output.range(shape(freqBins, 1), i * freqBins);
			sum.into(out.traverseEach()).evaluate(in.traverseEach());
		}

		return output;
	}

	protected PackedCollection<?> prepareFeatures(WaveData data) {
		PackedCollection<?> features = featureProvider.computeFeatures(data);

		if (features.getShape().length(0) < 1) {
			throw new IllegalArgumentException();
		}

		return features;
	}

	public static WaveDetailsFactory getDefault() {
		if (defaultFactory == null) {
			defaultFactory = new WaveDetailsFactory(OutputLine.sampleRate);
		}

		return defaultFactory;
	}
}
