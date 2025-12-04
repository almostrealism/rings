/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import java.util.ArrayList;
import java.util.List;

public class SpatialDataHub {
	private static SpatialDataHub current;

	private List<SpatialDataListener> dataListeners;
	private List<SpatialSelectionListener> selectionListeners;

	public SpatialDataHub() {
		this.dataListeners = new ArrayList<>();
		this.selectionListeners = new ArrayList<>();
	}

	public void addDataListener(SpatialDataListener<?> listener) {
		this.dataListeners.add(listener);
	}

	public void removeDataListener(SpatialDataListener<?> listener) {
		this.dataListeners.remove(listener);
	}

	public void addSelectionListener(SpatialSelectionListener<?> listener) {
		this.selectionListeners.add(listener);
	}

	public void removeSelectionListener(SpatialSelectionListener<?> listener) {
		this.selectionListeners.remove(listener);
	}

	public <T extends SpatialTimeseries> void publish(SpatialData<T> data) {
		dataListeners.forEach(l -> l.published(data));
	}

	public void scan(double time) {
		dataListeners.forEach(l -> l.scan(time));
	}

	public <T extends SpatialTimeseries> void selected(T info) {
		selectionListeners.forEach(l -> {
			try {
				l.selected(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static SpatialDataHub getCurrent() {
		if (current == null) current = new SpatialDataHub();
		return current;
	}
}
