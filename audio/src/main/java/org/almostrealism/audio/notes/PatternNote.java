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
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.StaticWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Factor;

import java.util.function.Supplier;

public class PatternNote implements CellFeatures, SamplingFeatures {
	private double noteAudioSelection;
	private NoteAudioProvider provider;

	private PatternNote delegate;
	private NoteAudioFilter filter;

	private Boolean valid;

	public PatternNote() { this(null); }

	public PatternNote(NoteAudioProvider provider) {
		this.provider = provider;
	}

	protected PatternNote(PatternNote delegate, NoteAudioFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	public double getNoteAudioSelection() {
		return noteAudioSelection;
	}

	public void setNoteAudioSelection(double noteAudioSelection) {
		this.noteAudioSelection = noteAudioSelection;
	}

	public NoteAudioProvider getProvider() { return provider; }

	public void setProvider(NoteAudioProvider provider) { this.provider = provider; }

	public KeyPosition<?> getRoot() {
		if (delegate != null) return delegate.getRoot();
		return provider.getRoot();
	}

	@JsonIgnore
	public void setTuning(KeyboardTuning tuning) {
		if (delegate != null) {
			delegate.setTuning(tuning);
		} else {
			provider.setTuning(tuning);
		}
	}

	@JsonIgnore
	public int getSampleRate() {
		if (delegate != null) return delegate.getSampleRate();
		return provider.getSampleRate();
	}

	@JsonIgnore
	public double getDuration(KeyPosition<?> target) {
		if (delegate != null) return delegate.getDuration(target);
		return provider.getDuration(target);
	}

	public TraversalPolicy getShape(KeyPosition<?> target) {
		if (delegate != null) return delegate.getShape(target);
		return provider.getShape(target);
	}

	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, double noteDuration) {
		if (delegate == null) {
			System.out.println("WARN: No AudioNoteFilter for PatternNote, note duration will be ignored");
			return getAudio(target);
		} else {
			// System.out.println("PatternNote: duration = " + noteDuration);
			return computeAudio(target, noteDuration);
		}
	}

	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target) {
		if (delegate != null) return delegate.getAudio(target);
		return provider.getAudio(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, double noteDuration) {
		if (delegate == null) {
			return provider.computeAudio(target);
		} else if (noteDuration > 0) {
			return sampling(getSampleRate(), getDuration(target),
					() -> filter.apply(delegate.getAudio(target), c(noteDuration)));
		} else {
			throw new UnsupportedOperationException();
		}
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

	public boolean isValid() {
		if (delegate != null) return delegate.isValid();
		if (valid != null) return valid;

		valid = provider.isValid();
		return valid;
	}

	public static PatternNote create(String source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNote create(String source, KeyPosition root) {
		return new PatternNote(new NoteAudioProvider(new FileWaveDataProvider(source), root));
	}

	public static PatternNote create(WaveData source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNote create(WaveData source, KeyPosition root) {
		return new PatternNote(new NoteAudioProvider(new StaticWaveDataProvider(source), root));
	}

	public static PatternNote create(PatternNote delegate, NoteAudioFilter filter) {
		return new PatternNote(delegate, filter);
	}

	public static PatternNote create(PatternNote delegate, Factor<PackedCollection<?>> factor) {
		return new PatternNote(delegate, (audio, duration) -> factor.getResultant(audio));
	}

	public static PatternNote create(Supplier<PackedCollection<?>> audioSupplier) {
		return create(audioSupplier, WesternChromatic.C1);
	}

	public static PatternNote create(Supplier<PackedCollection<?>> audioSupplier, KeyPosition root) {
		return new PatternNote(NoteAudioProvider.create(audioSupplier, root));
	}
}
