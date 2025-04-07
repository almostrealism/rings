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
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;

public class StatelessSourceNoteAudio implements NoteAudio, CodeFeatures {
	private StatelessSource source;
	private KeyboardTuning tuning;

	private BufferDetails buffer;
	private Producer<PackedCollection<?>> params;

	public StatelessSourceNoteAudio(StatelessSource source,
									BufferDetails buffer,
									Producer<PackedCollection<?>> params) {
		this.source = source;
		this.buffer = buffer;
		this.params = params;
	}

	@Override
	public void setTuning(KeyboardTuning tuning) { this.tuning = tuning; }

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target) {
		return source.generate(buffer, params, c(tuning.getTone(target).asHertz()));
	}

	@Override
	public double getDuration(KeyPosition<?> target) { return buffer.getDuration(); }

	@Override
	public int getSampleRate() { return buffer.getSampleRate(); }
}
