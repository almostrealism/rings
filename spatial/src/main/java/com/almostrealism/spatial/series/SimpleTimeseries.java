package com.almostrealism.spatial.series;

import com.almostrealism.spatial.FrequencyTimeseriesAdapter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleTimeseries<T extends Tree> extends FrequencyTimeseriesAdapter implements Tree<T> {
	private String key;

	private SoundDataTimeseries delegate;
	private List<T> children;

	public SimpleTimeseries() { }

	public SimpleTimeseries(String key) {
		this.key = key;
	}

	public void setKey(String key) { this.key = key; }

	@Override
	public String getKey() { return key; }

	@JsonIgnore
	public void setDelegate(SoundDataTimeseries delegate) {
		this.delegate = delegate;
	}

	@JsonIgnore
	public SoundDataTimeseries getDelegate() { return delegate; }

	@Override
	protected SoundDataTimeseries getDelegate(int layer) {
		return getDelegate();
	}

	@JsonIgnore
	@Override
	public int getLayerCount() { return 1; }

	@Override
	public Collection<T> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
		}

		return children;
	}
}
