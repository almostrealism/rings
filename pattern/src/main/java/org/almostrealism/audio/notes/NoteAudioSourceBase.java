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

import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.StatelessSource;

import java.util.List;
import java.util.function.Function;

public abstract class NoteAudioSourceBase implements NoteAudioSource {
	private Function<NoteAudio, StatelessSource> synthesizerFactory;

	public Function<NoteAudio, StatelessSource> getSynthesizerFactory() {
		return synthesizerFactory;
	}

	public void setSynthesizerFactory(Function<NoteAudio, StatelessSource> synthesizerFactory) {
		this.synthesizerFactory = synthesizerFactory;
	}

	@Override
	public List<PatternNoteAudio> getPatternNotes() {
		if (isUseSynthesizer()) {
			return getNotes().stream().map(getSynthesizerFactory())
					.map(source -> (PatternNoteAudio) new StatelessSourceNoteAudio(source,
							new BufferDetails(OutputLine.sampleRate, 10.0), null))
					.toList();
		}

		return getNotes().stream()
				.map(SimplePatternNote::new)
				.map(PatternNoteAudio.class::cast)
				.toList();
	}

	public abstract boolean isUseSynthesizer();
}
