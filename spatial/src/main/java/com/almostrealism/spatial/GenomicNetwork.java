/*
 * Copyright 2024 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.audio.health.AudioHealthScore;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Genome;

import java.io.File;

public class GenomicNetwork extends GenomicTimeseries {
	private int index;
	private AudioHealthScore healthScore;

	public GenomicNetwork() { }

	public GenomicNetwork(int index, Genome<PackedCollection> genome) {
		setIndex(index);
		setGenome(genome);
	}

	@Override
	public String getKey() {
		return new File(healthScore.getOutput()).getAbsolutePath();
	}

	@Override
	public int getIndex() { return index; }
	public void setIndex(int index) { this.index = index; }

	@Override
	public double getElementInterval(int layer) {
		return Math.min(super.getElementInterval(layer), 32);
	}

	public AudioHealthScore getHealthScore() {
		return healthScore;
	}

	public void setHealthScore(AudioHealthScore healthScore) {
		this.healthScore = healthScore;
		updateSeries();
	}

	@Override
	public double getDuration(TemporalSpatialContext context) {
		return healthScore == null ? 0.0 : super.getDuration(context);
	}

	@Override
	public String getWavFile() {
		return healthScore == null ? null : healthScore.getOutput();
	}
}
