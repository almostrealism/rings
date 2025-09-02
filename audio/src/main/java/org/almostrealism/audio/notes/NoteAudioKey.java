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

import org.almostrealism.audio.tone.KeyPosition;

import java.util.Objects;

public class NoteAudioKey {
	private KeyPosition<?> position;
	private int audioChannel;

	public NoteAudioKey(KeyPosition<?> position, int audioChannel) {
		this.position = position;
		this.audioChannel = audioChannel;
	}

	public KeyPosition<?> getPosition() {
		return position;
	}

	public int getAudioChannel() {
		return audioChannel;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NoteAudioKey that)) return false;
		return audioChannel == that.audioChannel &&
				Objects.equals(position, that.position);
	}

	@Override
	public int hashCode() { return position.hashCode(); }
}
