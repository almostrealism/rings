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

package org.almostrealism.audio.pattern;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.notes.NoteAudioContext;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public enum ScaleTraversalStrategy {
	CHORD, SEQUENCE;

	public List<PatternNoteAudio> getNoteDestinations(PatternElement element,
													  boolean melodic, double offset,
													  AudioSceneContext context,
													  NoteAudioContext audioContext) {
		List<PatternNoteAudio> destinations = new ArrayList<>();

		for (int i = 0; i < element.getRepeatCount(); i++) {
			double relativePosition = element.getPosition() + i * element.getRepeatDuration();
			double actualPosition = offset + relativePosition;

			List<KeyPosition<?>> keys = new ArrayList<>();
			context.getScaleForPosition().apply(actualPosition).forEach(keys::add);

			if (this == CHORD) {
				p: for (double p : element.getScalePositions()) {
					if (keys.isEmpty()) break p;
					int keyIndex = (int) (p * keys.size());

					Producer<PackedCollection<?>> note =
							element.getNoteAudio(melodic,
								keys.get(keyIndex), relativePosition,
								audioContext.nextNotePosition(relativePosition),
								audioContext.getAudioSelection(),
								context.getTimeForDuration());
					destinations.add(new PatternNoteAudio(note,
							context.frameForPosition(actualPosition)));

					keys.remove(keyIndex);
				}
			} else if (this == SEQUENCE) {
				double p = element.getScalePositions().get(i % element.getScalePositions().size());
				if (keys.isEmpty()) break;

				int keyIndex = (int) (p * keys.size());
				Producer<PackedCollection<?>> note = element.getNoteAudio(
							melodic, keys.get(keyIndex), relativePosition,
							audioContext.nextNotePosition(relativePosition),
							audioContext.getAudioSelection(),
							context.getTimeForDuration());
				destinations.add(new PatternNoteAudio(note, context.getFrameForPosition().applyAsInt(actualPosition)));
			} else {
				throw new UnsupportedOperationException("Unknown ScaleTraversalStrategy (" + this + ")");
			}
		}

		return destinations;
	}
}
