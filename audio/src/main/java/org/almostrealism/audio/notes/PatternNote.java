/*
 * Copyright 2024 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.notes;

import org.almostrealism.audio.tone.KeyboardTuning;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

public class PatternNote extends PatternNoteAudioAdapter {
	private PatternNote delegate;
	private NoteAudioFilter filter;

	private List<PatternNoteLayer> layers;

	public PatternNote() { }

	public PatternNote(List<PatternNoteLayer> layers) {
		this.layers = layers;
	}

	public PatternNote(double... noteAudioSelections) {
		this(new ArrayList<>());

		for (double noteAudioSelection : noteAudioSelections) {
			addLayer(noteAudioSelection);
		}
	}

	public PatternNote(PatternNote delegate, NoteAudioFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	public void addLayer(double noteAudioSelection) {
		layers.add(new PatternNoteLayer(noteAudioSelection));
	}

	public List<PatternNoteLayer> getLayers() {
		return layers;
	}

	public void setTuning(KeyboardTuning tuning) {
		if (delegate == null) {
			layers.forEach(l -> l.setTuning(tuning));
		} else {
			delegate.setTuning(tuning);
		}
	}

	@Override
	protected PatternNoteAudio getDelegate() {
		return delegate;
	}

	@Override
	protected NoteAudioFilter getFilter() {
		return filter;
	}

	@Override
	protected NoteAudioProvider getProvider(DoubleFunction<NoteAudioProvider> audioSelection) {
		throw new UnsupportedOperationException(); // TODO  Sum the layers
	}
}
