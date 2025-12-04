/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */


package com.almostrealism.spatial;

import org.almostrealism.audio.persistence.AudioLibraryPersistence;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.notes.SceneAudioNode;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ProjectedGenome;

import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenomicTimeseries extends FrequencyTimeseriesAdapter implements SpatialGenomic {

	private transient Genome<PackedCollection> genome;
	private FrequencyTimeseriesAdapter delegate;
	private PlaceholderTimeseries placeholder;

	private SceneAudioNode node;

	@Override
	@Transient
	public Genome<PackedCollection> getGenome() { return genome; }
	public void setGenome(Genome<PackedCollection> genome) { this.genome = genome; }

	public void setGenomeParameters(List<Double> params) {
		if (params == null) {
			this.genome = null;
		} else {
			this.genome = new ProjectedGenome(PackedCollection.of(params));
		}
	}

	public List<Double> getGenomeParameters() {
		if (genome instanceof ProjectedGenome) {
			return ((ProjectedGenome) genome).getParameters()
					.doubleStream().boxed().collect(Collectors.toList());
		}

		return null;
	}

	@Override
	public int getLayerCount() {
		if (delegate == null) updateSeries();
		return delegate == null ? 0 : 1;
	}

	public void updateSeries() {
		File detailBin = getDetailsFile();
		if (detailBin == null) {
			return;
		}

		try {
			if (detailBin.exists()) {
				delegate = new SpatialWaveDetails(AudioLibraryPersistence
						.loadWaveDetails(detailBin.getPath()));
			}
		} catch (IOException e) {
			warn("Failed to load wave details for " + getWavFile() + " from " + detailBin.getPath(), e);
		}

		if (delegate == null) {
			double seconds = new FileWaveDataProvider(getWavFile()).getDuration();
			placeholder = new PlaceholderTimeseries(seconds);
		}

		resetElements();
	}

	@Override
	public List<SpatialValue> elements(TemporalSpatialContext context) {
		List<SpatialValue> elements = super.elements(context);

		if (elements == null && placeholder != null) {
			return placeholder.elements(context);
		}

		return elements;
	}

	@Override
	protected FrequencyTimeseries getDelegate(int layer) {
		if (delegate == null) updateSeries();
		return delegate;
	}

	@Override
	public double getDuration(TemporalSpatialContext context) {
		if (getLayerCount() > 0) {
			return super.getDuration(context);
		}

		return placeholder == null ? 0 : placeholder.getDuration(context);
	}

	public void setSceneAudioNode(SceneAudioNode node) { this.node = node; }

	public SceneAudioNode getSceneAudioNode() { return node; }

	protected int getIndex() { return 0; }

	public abstract String getWavFile();

	public File getDetailsFile() {
		File f = new File(getWavFile());
		if (!f.exists()) return null;

		File dir = f.getParentFile();
		return new File(dir,
				new FileWaveDataProvider(getWavFile()).getIdentifier() + ".bin");
	}

	public List<File> getDependentFiles() {
		String f = getWavFile();
		if (f == null || f.isEmpty())
			return Collections.emptyList();

		File wav = new File(getWavFile());
		if (!wav.exists()) return Collections.emptyList();

		File details = getDetailsFile();
		if (details != null && details.exists()) {
			return List.of(wav, details);
		}

		return List.of(wav);
	}
}
