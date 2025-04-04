/*
 * Copyright 2023 Michael Murray
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

import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.tone.KeyboardTuning;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ListNoteSource implements NoteAudioSource {
	private KeyboardTuning tuning;
	private List<NoteAudioProvider> notes;

	public ListNoteSource() { }

	public ListNoteSource(NoteAudioProvider... notes) {
		this(List.of(notes));
	}

	public ListNoteSource(List<NoteAudioProvider> notes) {
		this.notes = notes;
	}

	@Override
	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;

		for (NoteAudioProvider note : getNotes()) {
			note.setTuning(tuning);
		}
	}

	public String getOrigin() { return null; }

	@Override
	public List<NoteAudioProvider> getNotes() {
		return notes == null ? Collections.emptyList() : notes;
	}

	public void setNotes(List<NoteAudioProvider> notes) {
		this.notes = notes;
	}

	public boolean checkResourceUsed(String canonicalPath) {
		return notes.stream().anyMatch(note -> {
			if (note.getProvider() instanceof FileWaveDataProvider) {
				try {
					return new File(((FileWaveDataProvider) note.getProvider()).getResourcePath()).getCanonicalPath().equals(canonicalPath);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		});
	}
}
