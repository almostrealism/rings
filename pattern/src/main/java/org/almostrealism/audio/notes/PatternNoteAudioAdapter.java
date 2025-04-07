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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.collect.PackedCollection;

import java.util.function.DoubleFunction;

public abstract class PatternNoteAudioAdapter implements
		PatternNoteAudio, CellFeatures, SamplingFeatures {

	@Override
	public int getSampleRate(KeyPosition<?> target, DoubleFunction<NoteAudio> audioSelection) {
		if (getDelegate() != null) return getDelegate().getSampleRate(target, audioSelection);
		return getProvider(target, audioSelection).getSampleRate();
	}

	@Override
	public double getDuration(KeyPosition<?> target, DoubleFunction<NoteAudio> audioSelection) {
		if (getDelegate() != null) return getDelegate().getDuration(target, audioSelection);

		NoteAudio provider = getProvider(target, audioSelection);
		if (provider == null) {
			warn("No provider for " + target);
			return 0.0;
		}

		return getProvider(target, audioSelection).getDuration(target);
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, double noteDuration,
												  Producer<PackedCollection<?>> automationLevel,
												  DoubleFunction<NoteAudio> audioSelection) {
		return computeAudio(target, noteDuration, automationLevel, audioSelection);
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target,
												  DoubleFunction<NoteAudio> audioSelection) {
		if (getDelegate() != null) {
			warn("Loading audio from delegate without note duration, filter will be skipped");
			return getDelegate().getAudio(target, audioSelection);
		}

		return getProvider(target, audioSelection).getAudio(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, double noteDuration,
														 Producer<PackedCollection<?>> automationLevel,
														 DoubleFunction<NoteAudio> audioSelection) {
		if (getDelegate() == null) {
			NoteAudio p = getProvider(target, audioSelection);
			if (p == null) {
				throw new UnsupportedOperationException();
			}

			return p.getAudio(target);
		} else if (noteDuration > 0) {
			return sampling(getSampleRate(target, audioSelection), getDuration(target, audioSelection),
					() -> getFilter().apply(getDelegate()
									.getAudio(target, noteDuration, automationLevel, audioSelection),
												c(noteDuration), automationLevel));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	protected abstract PatternNoteAudio getDelegate();

	protected abstract NoteAudioFilter getFilter();

	protected abstract NoteAudio getProvider(KeyPosition<?> target, DoubleFunction<NoteAudio> audioSelection);
}
