/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial.series;

import com.almostrealism.spatial.FrequencyTimeseries;
import com.almostrealism.spatial.FrequencyTimeseriesAdapter;
import com.almostrealism.spatial.SoundData;
import com.almostrealism.spatial.SpatialWaveDetails;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDetails;

public class SoundDataTimeseries extends FrequencyTimeseriesAdapter {
	private SoundData sound;
	private SpatialWaveDetails details;

	public SoundDataTimeseries(SoundData sound, WaveDetails details) {
		this(sound, new SpatialWaveDetails(details));
	}

	public SoundDataTimeseries(SoundData sound, SpatialWaveDetails details) {
		this.sound = sound;
		this.details = details;
	}

	@Override
	public int getLayerCount() { return 1; }

	@Override
	public String getKey() { return details.getKey(); }

	public SoundData getSoundData() { return sound; }

	public WaveDataProvider getProvider() {
		return getSoundData().toProvider();
	}

	@Override
	protected FrequencyTimeseries getDelegate(int layer) {
		return details;
	}
}
