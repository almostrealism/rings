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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.collect.PackedCollection;

import java.util.function.DoubleFunction;

public abstract class PatternNoteAudioAdapter implements
		PatternNoteAudio, CellFeatures, SamplingFeatures {

	@Override
	public int getSampleRate(DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() != null) return getDelegate().getSampleRate(audioSelection);
		return getProvider(audioSelection).getSampleRate();
	}

	@Override
	public double getDuration(KeyPosition<?> target, DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() != null) return getDelegate().getDuration(target, audioSelection);
		return getProvider(audioSelection).getDuration(target);
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, double noteDuration,
												  DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() == null) {
			System.out.println("WARN: No AudioNoteFilter for PatternNoteAudio, note duration will be ignored");
			return getAudio(target, audioSelection);
		} else {
			return computeAudio(target, noteDuration, audioSelection);
		}
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target,
												  DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() != null) return getDelegate().getAudio(target, audioSelection);
		return getProvider(audioSelection).getAudio(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, double noteDuration,
														 DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() == null) {
			return getProvider(audioSelection).computeAudio(target);
		} else if (noteDuration > 0) {
			return sampling(getSampleRate(audioSelection), getDuration(target, audioSelection),
					() -> getFilter().apply(getDelegate().getAudio(target, audioSelection), c(noteDuration)));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	protected abstract PatternNoteAudio getDelegate();

	protected abstract NoteAudioFilter getFilter();

	protected abstract NoteAudioProvider getProvider(DoubleFunction<NoteAudioProvider> audioSelection);
}
