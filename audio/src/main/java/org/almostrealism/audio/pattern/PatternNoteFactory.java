package org.almostrealism.audio.pattern;

import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.filter.ParameterizedFilterEnvelope;
import org.almostrealism.audio.filter.ParameterizedVolumeEnvelope;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteLayer;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class PatternNoteFactory {

	private ParameterizedVolumeEnvelope volumeEnvelope;
	private ParameterizedFilterEnvelope filterEnvelope;

	public PatternNoteFactory() {
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		volumeEnvelope = ParameterizedVolumeEnvelope.random(ParameterizedVolumeEnvelope.Mode.NOTE_LAYER);
		filterEnvelope = ParameterizedFilterEnvelope.random(ParameterizedFilterEnvelope.Mode.NOTE_LAYER);
	}

	public ParameterizedVolumeEnvelope getVolumeEnvelope() {
		return volumeEnvelope;
	}

	public void setVolumeEnvelope(ParameterizedVolumeEnvelope volumeEnvelope) {
		this.volumeEnvelope = volumeEnvelope;
	}

	public ParameterizedFilterEnvelope getFilterEnvelope() {
		return filterEnvelope;
	}

	public void setFilterEnvelope(ParameterizedFilterEnvelope filterEnvelope) {
		this.filterEnvelope = filterEnvelope;
	}

	public PatternNote apply(ParameterSet params, double... choices) {
		return new PatternNote(DoubleStream.of(choices)
				.mapToObj(PatternNoteLayer::new)
				.map(note -> volumeEnvelope.apply(params, note))
				.map(note -> filterEnvelope.apply(params, note))
				.collect(Collectors.toList()));
	}
}
