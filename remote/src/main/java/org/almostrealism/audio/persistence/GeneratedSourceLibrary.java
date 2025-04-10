/*
 * Copyright 2025 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.persistence;

import org.almostrealism.audio.notes.NoteAudio;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.synth.AudioSynthesisModel;
import org.almostrealism.audio.synth.AudioSynthesizer;
import org.almostrealism.audio.synth.InterpolatedAudioSynthesisModel;
import org.almostrealism.audio.tone.KeyboardTuning;

import java.util.HashMap;
import java.util.Map;

public class GeneratedSourceLibrary {
	private LibraryDestination library;
	private Map<String, AudioSynthesisModel> models;

	public GeneratedSourceLibrary(LibraryDestination library) {
		this.library = library;
		this.models = new HashMap<>();
	}

	public void add(String key, AudioSynthesisModel model) {
		models.put(key, model);
	}

	public StatelessSource getSource(String key) {
		AudioSynthesizer synth = new AudioSynthesizer();

		if (models.containsKey(key)) {
			synth.setModel(models.get(key));
		}

		return synth;
	}

	public StatelessSource getSynthesizer(NoteAudio modelInput) {
		if (!(modelInput instanceof NoteAudioProvider)) {
			throw new UnsupportedOperationException();
		}

		KeyboardTuning tuning = ((NoteAudioProvider) modelInput).getTuning();

		InterpolatedAudioSynthesisModel model = InterpolatedAudioSynthesisModel
				.create(modelInput,
						((NoteAudioProvider) modelInput).getRoot(),
						tuning);
		AudioSynthesizer synth = new AudioSynthesizer(model, 12);
		synth.setTuning(tuning);
		return synth;
	}
}
