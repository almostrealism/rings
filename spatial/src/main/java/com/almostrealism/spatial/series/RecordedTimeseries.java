/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial.series;

public class RecordedTimeseries extends SimpleTimeseries<RecordedTimeseries> {
	private boolean group;

	public RecordedTimeseries() { }

	public RecordedTimeseries(String key) {
		this(key, false);
	}

	public RecordedTimeseries(String key, boolean group) {
		super(key);
		this.group = group;
	}

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}
}
