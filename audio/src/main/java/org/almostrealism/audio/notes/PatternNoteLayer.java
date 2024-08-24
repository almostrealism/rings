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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.StaticWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuned;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;

import java.util.function.DoubleFunction;
import java.util.function.Supplier;

public class PatternNoteLayer extends PatternNoteAudioAdapter implements KeyboardTuned {
	public static final long selectionComparisonGranularity = (long) 1e10;

	private double noteAudioSelection;
	private NoteAudioProvider provider;

	private PatternNoteLayer delegate;
	private NoteAudioFilter filter;

	private Boolean valid;

	public PatternNoteLayer() { this(null); }

	public PatternNoteLayer(double noteAudioSelection) {
		setNoteAudioSelection(noteAudioSelection);
	}

	public PatternNoteLayer(NoteAudioProvider provider) {
		this.provider = provider;
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

	public NoteAudioProvider getProvider() { return provider; }

	public void setProvider(NoteAudioProvider provider) { this.provider = provider; }

	@JsonIgnore
	@Override
	public void setTuning(KeyboardTuning tuning) {
		if (delegate != null) {
			delegate.setTuning(tuning);
		} else if (provider != null) {
			provider.setTuning(tuning);
		}
	}

	@Override
	protected PatternNoteAudio getDelegate() { return delegate; }

	@Override
	protected NoteAudioFilter getFilter() { return filter; }

	@Override
	protected NoteAudioProvider getProvider(KeyPosition<?> target, DoubleFunction<NoteAudioProvider> audioSelection) {
		if (delegate != null) return delegate.getProvider(target, audioSelection);
		return (provider == null ? audioSelection.apply(noteAudioSelection) : provider);
	}

	@JsonIgnore
	public PackedCollection<?> getAudio() {
		if (delegate != null) {
			warn("Attempting to get audio from a delegated PatternNote");
			// return getAudio(getRoot()).get().evaluate();
		} else if (provider != null) {
			provider.getAudio();
		}

		return null;
	}

	@JsonIgnore
	public boolean isValid() {
		if (delegate != null) return delegate.isValid();
		if (valid != null) return valid;

		valid = provider.isValid();
		return valid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PatternNoteLayer) {
			PatternNoteLayer other = (PatternNoteLayer) obj;

			if (provider != null) {
				return provider.equals(other.getProvider());
			} else {
				long compA = (long) (other.getNoteAudioSelection() * selectionComparisonGranularity);
				long compB = (long) (getNoteAudioSelection() * selectionComparisonGranularity);
				return compA == compB;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Double.valueOf(getNoteAudioSelection()).hashCode();
	}

	public static PatternNoteLayer create(String source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNoteLayer create(String source, KeyPosition root) {
		return new PatternNoteLayer(new NoteAudioProvider(new FileWaveDataProvider(source), root));
	}

	public static PatternNoteLayer create(WaveData source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNoteLayer create(WaveData source, KeyPosition root) {
		return new PatternNoteLayer(new NoteAudioProvider(new StaticWaveDataProvider(source), root));
	}

	public static PatternNoteLayer create(PatternNoteLayer delegate, NoteAudioFilter filter) {
		return new PatternNoteLayer(delegate, filter);
	}

	public static PatternNoteLayer create(PatternNoteLayer delegate, Factor<PackedCollection<?>> factor) {
		return new PatternNoteLayer(delegate, (audio, duration, automationLevel) -> factor.getResultant(audio));
	}

	public static PatternNoteLayer create(Supplier<PackedCollection<?>> audioSupplier) {
		return create(audioSupplier, WesternChromatic.C1);
	}

	public static PatternNoteLayer create(Supplier<PackedCollection<?>> audioSupplier, KeyPosition root) {
		return new PatternNoteLayer(NoteAudioProvider.create(audioSupplier, root));
	}
}
