/*
 * Copyright 2024 Michael Murray
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
	public static boolean enableNormalizeSimilarity = false;
	public static boolean enableFeatures = false;

	public static int defaultBins = 32; // 16;
	public static double defaultWindow = 0.25; // 0.125;

	protected static WaveDetailsFactory defaultFactory;

	private int sampleRate;
	private double fftSampleRate;

	private int freqBins;
	private int scaleBins, scaleTime;
	private PackedCollection<?> buffer;
	private Evaluable<PackedCollection<?>> sum;
	private Evaluable<PackedCollection<?>> difference;

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
		difference = cv(shape(freqBins, 1), 0)
				.subtract(cv(shape(freqBins, 1), 1))
				.traverseEach()
				.magnitude()
				.get();
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public WaveDetails forFile(String file) {
		return forProvider(new FileWaveDataProvider(file));
	}

	public WaveDetails forProvider(WaveDataProvider provider) {
		return forWaveData(provider.getIdentifier(), provider.get());
	}

	public WaveDetails forWaveData(String identifier, WaveData data) {
		if (data.getSampleRate() != getSampleRate()) {
			return new WaveDetails(identifier, data.getSampleRate());
		}

		WaveDetails details = new WaveDetails(identifier);
		details.setSampleRate(data.getSampleRate());
		details.setChannelCount(1);
		details.setFrameCount(data.getCollection().getMemLength());
		details.setData(data.getCollection());

		PackedCollection<?> fft = processFft(data.fft(true));
		if (fft.getShape().length(0) < 1) {
			throw new UnsupportedOperationException();
		}

		details.setFreqSampleRate(fftSampleRate / scaleTime);
		details.setFreqChannelCount(1);
		details.setFreqBinCount(freqBins);
		details.setFreqFrameCount(fft.getShape().length(0));
		details.setFreqData(fft);

		if (enableFeatures) {
			PackedCollection<?> features = processFeatures(data.features());
			if (features.getShape().length(0) < 1) {
				throw new UnsupportedOperationException();
			}

			// TODO  This is not the most accurate way to determine the sample rate
			details.setFeatureSampleRate(features.getShape().length(0) / data.getDuration());
			details.setFeatureChannelCount(1);
			details.setFeatureBinCount(features.getShape().length(1));
			details.setFeatureFrameCount(features.getShape().length(0));
			details.setFeatureData(features);
		}

		return details;
	}

	public double similarity(WaveDetails a, WaveDetails b) {
		int bins = freqBins;
		int n;

		if (a.getFreqBinCount() == b.getFreqBinCount()) {
			n = Math.min(a.getFreqFrameCount(), b.getFreqFrameCount());
			bins = a.getFreqBinCount();
		} else {
			// WaveDetails with different shapes are not easily comparable
			n = 0;
		}

		TraversalPolicy overlap = new TraversalPolicy(true, n, bins, 1);

		double d = 0.0;

		if (n > 0) {
			PackedCollection<?> aFft = a.getFreqData().range(overlap).traverse(1);
			PackedCollection<?> bFft = b.getFreqData().range(overlap).traverse(1);
			PackedCollection diff = difference.evaluate(aFft, bFft);
			d += diff.doubleStream().sum();
		}

		if (a.getFreqFrameCount() > n) {
			d += a.getFreqData().range(shape(a.getFreqFrameCount() - n, a.getFreqBinCount(), 1), overlap.getTotalSize())
					.doubleStream().map(Math::abs).sum();
		}

		if (b.getFreqFrameCount() > n) {
			d += b.getFreqData().range(shape(b.getFreqFrameCount() - n, b.getFreqBinCount(), 1), overlap.getTotalSize())
					.doubleStream().map(Math::abs).sum();
		}

		if (enableNormalizeSimilarity) {
			double max = Math.max(
					a.getFreqFrameCount() <= 0 ? 0.0 : a.getFreqData().doubleStream().map(Math::abs).max().orElse(0.0),
					b.getFreqFrameCount() <= 0 ? 0.0 : b.getFreqData().doubleStream().map(Math::abs).max().orElse(0.0));
			max = max * freqBins * Math.max(a.getFreqFrameCount(), b.getFreqFrameCount());
			double r = max == 0 ? Double.MAX_VALUE : (d / max);

			if (r > 1.0 && max != 0) {
				warn("Similarity = " + r);
			}

			return r;
		} else {
			return d;
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

	protected PackedCollection<?> processFeatures(PackedCollection<?> features) {
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
