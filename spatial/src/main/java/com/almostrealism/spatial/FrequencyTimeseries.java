/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.algebra.Vector;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class FrequencyTimeseries implements SpatialTimeseries, ConsoleFeatures {
	public static double frequencyThreshold = 35;
	public static double frequencyScale = 1;

	private List<SpatialValue> elements;
	private double contextDuration;

	public abstract int getLayerCount();

	public abstract int getIndex(int layer);

	public abstract double getElementInterval(int layer);

	public abstract double getFrequencyTimeScale(int layer);

	public abstract List<PackedCollection> getSeries(int layer);

	protected void loadElements(TemporalSpatialContext context) {
		if (elements != null) return;

		elements = new ArrayList<>();

		l: for (int layer = 0; layer < getLayerCount(); layer++) {
			int index = getIndex(layer);
			List<PackedCollection> features = getSeries(layer);

			if (features == null) {
				elements.add(new SpatialValue<>(context.position(0, index, layer, 0.0), 0.0));
				continue l;
			}

			double skip = getElementInterval(layer);

			TraversalPolicy featureShape = features.get(0).getShape();
			int len = featureShape.length(0);
			int depth = featureShape.length(1);
			double featureData[] = extractMax(features.stream().map(PackedCollection::toArray).toList());
			double frequencyTimeScale = getFrequencyTimeScale(layer);

			for (int attempt = 1; attempt < 5; attempt++) {
				elements.clear();

				for (double id = 0; id < len; id += skip) {
					int i = (int) id;
					double center = id + skip / 2.0;
					double time = center * frequencyTimeScale;

					double aggregates[] = new double[4];

					for (int j = 0; j < depth; j++) {
						int pos = depth - j - 1;
						double freq = j / (double) depth;

						int window = (int) Math.max(i + 1, Math.ceil(center));

						double value = IntStream.range(i, Math.min(window, len))
								.mapToDouble(k -> {
									double location = Math.abs(k - center);
									double dampen = 1.0 - Math.random() * location;
									return dampen * featureData[featureShape.index(k, pos, 0)];
								})
								.max()
								.orElse(0.0);
						value = value - (frequencyThreshold / attempt);
						value = value * frequencyScale * (1.0 + 0.2 * attempt);

						if (value > 0.0) {
							Vector position = context.position(
									context.getSecondsToTime().applyAsDouble(time),
									index, layer, freq);
							elements.add(new SpatialValue<>(
									position, Math.log(value + 1), 1.0 - freq, true));

							aggregates[(int) (4 * freq)] += value;
						}
					}

					for (int j = 0; j < aggregates.length; j++) {
						double freq = (j + 1) / (double) aggregates.length;

						if (aggregates[j] > 0.0) {
							Vector position = context.position(
									context.getSecondsToTime().applyAsDouble(time),
									index, layer, freq, false);
							elements.add(new SpatialValue<>(
									position, Math.log(aggregates[j] + 1), 1.0 - freq, true, true));
						}
					}
				}

				if (elements.size() > 4 * len) {
					return;
				}

				// Otherwise, try again
			}
		}
	}

	protected double[] extractMax(List<double[]> data) {
		int len = data.get(0).length;
		double max[] = new double[len];

		for (double[] d : data) {
			for (int i = 0; i < max.length; i++) {
				if (d[i] > max[i]) max[i] = d[i];
			}
		}

		return max;
	}

	protected void resetElements() { this.elements = null; }

	@Override
	public List<SpatialValue> elements(TemporalSpatialContext context) {
		if (contextDuration != context.getDuration()) {
			contextDuration = context.getDuration();
			resetElements();
		}

		loadElements(context);
		if (elements != null) {
			elements.forEach(e -> e.setReferent(this));
		}

		return elements;
	}

	@Override
	public double getDuration(TemporalSpatialContext context) {
		return IntStream.range(0, getLayerCount())
				.filter(l -> getSeries(l) != null)
				.mapToDouble(l ->
				context.getSecondsToTime().applyAsDouble(
					getSeries(l).get(0).getShape().length(0) * getFrequencyTimeScale(l)))
				.max().orElse(0.0);
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}

	@JsonIgnore
	@Override
	public Class getLogClass() {
		return ConsoleFeatures.super.getLogClass();
	}
}
