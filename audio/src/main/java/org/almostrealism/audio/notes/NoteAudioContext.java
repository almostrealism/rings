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

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public class NoteAudioContext implements ConsoleFeatures {
	private DoubleFunction<NoteAudioProvider> audioSelection;
	private DoubleUnaryOperator nextNotePosition;

	public NoteAudioContext() { }

	public NoteAudioContext(List<NoteAudioProvider> audioChoices,
							DoubleUnaryOperator nextNotePosition) {
		this(c -> audioChoices.isEmpty() ? null : audioChoices.get((int) (c * audioChoices.size())), nextNotePosition);
	}

	public NoteAudioContext(DoubleFunction<NoteAudioProvider> audioSelection,
							DoubleUnaryOperator nextNotePosition) {
		if (audioSelection == null) {
			warn("No audio selection provided");
		}

		this.audioSelection = audioSelection;
		this.nextNotePosition = nextNotePosition;
	}

	public DoubleFunction<NoteAudioProvider> getAudioSelection() {
		return audioSelection;
	}

	public void setAudioSelection(DoubleFunction<NoteAudioProvider> audioSelection) {
		this.audioSelection = audioSelection;
	}

	public NoteAudioProvider selectAudio(double selection) {
		return getAudioSelection().apply(selection);
	}

	public DoubleUnaryOperator getNextNotePosition() {
		return nextNotePosition;
	}

	public void setNextNotePosition(DoubleUnaryOperator nextNotePosition) {
		this.nextNotePosition = nextNotePosition;
	}

	public double nextNotePosition(double pos) {
		return nextNotePosition.applyAsDouble(pos);
	}

	@Override
	public Console console() { return CellFeatures.console; }
}
