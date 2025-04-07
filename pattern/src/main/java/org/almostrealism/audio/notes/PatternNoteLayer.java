/*
 * Copyright 2025 Michael Murray
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuned;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;

import java.util.function.DoubleFunction;

public class PatternNoteLayer extends PatternNoteAudioAdapter implements KeyboardTuned {
	public static final long selectionComparisonGranularity = (long) 1e10;

	private double noteAudioSelection;

	private PatternNoteLayer delegate;
	private NoteAudioFilter filter;

	public PatternNoteLayer() { this(0.0); }

	public PatternNoteLayer(double noteAudioSelection) {
		setNoteAudioSelection(noteAudioSelection);
	}

	protected PatternNoteLayer(PatternNoteLayer delegate, NoteAudioFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	public double getNoteAudioSelection() {
		return delegate == null ? noteAudioSelection : delegate.getNoteAudioSelection();
	}

	public void setNoteAudioSelection(double noteAudioSelection) {
		if (delegate != null) return;

		this.noteAudioSelection = noteAudioSelection;
	}

	@JsonIgnore
	@Override
	public void setTuning(KeyboardTuning tuning) {
		if (delegate != null) {
			delegate.setTuning(tuning);
		}
	}

	@Override
	protected PatternNoteAudio getDelegate() { return delegate; }

	@Override
	protected NoteAudioFilter getFilter() { return filter; }

	@Override
	protected NoteAudio getProvider(KeyPosition<?> target, DoubleFunction<NoteAudio> audioSelection) {
		if (delegate != null) return delegate.getProvider(target, audioSelection);
		return audioSelection.apply(noteAudioSelection);
	}

	@JsonIgnore
	public PackedCollection<?> getAudio() {
		if (delegate != null) {
			warn("Attempting to get audio from a delegated PatternNote");
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PatternNoteLayer) {
			PatternNoteLayer other = (PatternNoteLayer) obj;

			long compA = (long) (other.getNoteAudioSelection() * selectionComparisonGranularity);
			long compB = (long) (getNoteAudioSelection() * selectionComparisonGranularity);
			return compA == compB;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Double.valueOf(getNoteAudioSelection()).hashCode();
	}

	public static PatternNoteLayer create(PatternNoteLayer delegate, NoteAudioFilter filter) {
		return new PatternNoteLayer(delegate, filter);
	}

	public static PatternNoteLayer create(PatternNoteLayer delegate, Factor<PackedCollection<?>> factor) {
		return new PatternNoteLayer(delegate, (audio, duration, automationLevel) -> factor.getResultant(audio));
	}
}
