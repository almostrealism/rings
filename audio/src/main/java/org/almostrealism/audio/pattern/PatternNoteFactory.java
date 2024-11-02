package org.almostrealism.audio.pattern;

import org.almostrealism.audio.data.ChannelInfo;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.filter.ParameterizedEnvelopeLayers;
import org.almostrealism.audio.filter.ParameterizedFilterEnvelope;
import org.almostrealism.audio.filter.ParameterizedVolumeEnvelope;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteAudio;
import org.almostrealism.audio.notes.PatternNoteLayer;

import java.util.ArrayList;
import java.util.List;

public class PatternNoteFactory {
	public static final int LAYER_COUNT = 3;

	private ParameterizedEnvelopeLayers layerEnvelopes;
	private ParameterizedVolumeEnvelope volumeEnvelope;
	private ParameterizedFilterEnvelope filterEnvelope;

	public PatternNoteFactory() {
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		layerEnvelopes = ParameterizedEnvelopeLayers.random(LAYER_COUNT);
	}

	public int getLayerCount() { return LAYER_COUNT; }

	public ParameterizedEnvelopeLayers getLayerEnvelopes() {
		return layerEnvelopes;
	}

	public void setLayerEnvelopes(ParameterizedEnvelopeLayers layerEnvelopes) {
		this.layerEnvelopes = layerEnvelopes;
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

	public PatternNote apply(ParameterSet params, ChannelInfo.Voicing voicing, boolean blend, double... choices) {
		List<PatternNoteAudio> layers = new ArrayList<>();

		for (int i = 0; i < getLayerCount(); i++) {
			PatternNoteLayer l = new PatternNoteLayer(choices[i]);

			if (blend) {
				l = layerEnvelopes.getEnvelope(i).apply(params, voicing, l);
			} else {
//				l = volumeEnvelope.apply(params, l);
//				l = filterEnvelope.apply(params, l);
			}

			layers.add(l);
		}

		return new PatternNote(layers);
	}
}
