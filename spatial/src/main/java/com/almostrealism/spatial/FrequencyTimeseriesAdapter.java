/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.collect.PackedCollection;

import java.util.List;

public abstract class FrequencyTimeseriesAdapter extends FrequencyTimeseries {
	protected FrequencyTimeseries getDelegate(int layer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIndex(int layer) {
		return getDelegate(layer).getIndex(layer);
	}

	@Override
	public double getFrequencyTimeScale(int layer) {
		return getDelegate(layer).getFrequencyTimeScale(layer);
	}

	@Override
	public double getElementInterval(int layer) {
		return getDelegate(layer).getElementInterval(layer);
	}

	@Override
	public List<PackedCollection> getSeries(int layer) {
		FrequencyTimeseries delegate = getDelegate(layer);
		if (delegate == null) return null;

		return delegate.getSeries(layer);
	}

	@JsonIgnore
	@Override
	public boolean isEmpty() { return false; }
}
