/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.collect.PackedCollection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpatialWaveDetails extends FrequencyTimeseriesAdapter {
	private WaveDetails wave;
	private int offset, length;

	public SpatialWaveDetails(WaveDetails wave) {
		this(wave, 0, wave.getFrameCount());
	}

	public SpatialWaveDetails(WaveDetails wave, int offset, int length) {
		this.wave = wave;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public String getKey() {
		return wave.getIdentifier();
	}

	@Override
	public int getLayerCount() { return 1; }

	@Override
	public int getIndex(int layer) {
		return 0;
	}

	@Override
	public double getElementInterval(int layer) {
		if (wave.getFreqFrameCount() == 0) {
			return 1.0;
		}

		return Math.min(1.0, wave.getFreqFrameCount() / 40.0);
	}

	@Override
	public double getFrequencyTimeScale(int layer) {
		return 1.0 / wave.getFreqSampleRate();
	}

	@Override
	public List<PackedCollection> getSeries(int layer) {
		return IntStream.range(0, wave.getFreqChannelCount())
				.mapToObj(c -> getSeries(layer, c))
				.collect(Collectors.toList());
	}

	public PackedCollection getSeries(int layer, int channel) {
		if (wave.getFreqData() == null) return null;

		double ratio = wave.getSampleRate() / wave.getFreqSampleRate();
		int o = (int) (offset / ratio);
		int l = (int) (length / ratio);

		if (l == 0) {
			l = 1;

			if ((o + l) * wave.getFreqBinCount() > wave.getFreqData(channel).getShape().getTotalSize()) {
				o--;
			}
		}

		return wave.getFreqData(channel).range(new TraversalPolicy(l, wave.getFreqBinCount(), 1),
				o * wave.getFreqBinCount());
	}
}
