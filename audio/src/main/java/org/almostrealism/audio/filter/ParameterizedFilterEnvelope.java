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

package org.almostrealism.audio.filter;

import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioFilter;
import org.almostrealism.audio.notes.PatternNoteLayer;
import org.almostrealism.collect.PackedCollection;

public class ParameterizedFilterEnvelope extends ParameterizedEnvelope {
	public static final int MAX_SECONDS = 90;

	private static FilterEnvelopeProcessor processor;

	static {
		processor = new FilterEnvelopeProcessor(44100, MAX_SECONDS);
	}

	private Mode mode;

	public ParameterizedFilterEnvelope() {
		super();
		mode = Mode.STANDARD_NOTE;
	}

	public ParameterizedFilterEnvelope(Mode mode, ParameterFunction attackSelection, ParameterFunction decaySelection,
									   ParameterFunction sustainSelection, ParameterFunction releaseSelection) {
		super(attackSelection, decaySelection, sustainSelection, releaseSelection);
		this.mode = mode;
	}

	public Mode getMode() { return mode; }

	public void setMode(Mode mode) { this.mode = mode; }

	@Override
	public NoteAudioFilter createFilter(ParameterSet params) {
		return (audio, duration) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> result = new PackedCollection<>(shape(audioData));
			PackedCollection<?> dr = duration.get().evaluate();

			processor.setDuration(dr.toDouble(0));
			processor.setAttack(mode.getMaxAttack() * getAttackSelection().positive().apply(params));
			processor.setDecay(mode.getMaxDecay() * getDecaySelection().positive().apply(params));
			processor.setSustain(mode.getMaxSustain() * getSustainSelection().positive().apply(params));
			processor.setRelease(mode.getMaxRelease() * getReleaseSelection().positive().apply(params));

			processor.process(audioData, result);
			return result;
		};
	}

	public static ParameterizedFilterEnvelope random(Mode mode) {
		return new ParameterizedFilterEnvelope(mode,
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}

	public enum Mode {
		STANDARD_NOTE, NOTE_LAYER;

		public double getMaxAttack() {
			switch (this) {
				case NOTE_LAYER:
					return 0.5;
				case STANDARD_NOTE:
				default:
					return 0.1;
			}
		}

		public double getMaxDecay() {
			switch (this) {
				case NOTE_LAYER:
					return 0.5;
				case STANDARD_NOTE:
				default:
					return 0.05;
			}
		}

		public double getMaxSustain() {
			switch (this) {
				case NOTE_LAYER:
					return 1.0;
				case STANDARD_NOTE:
				default:
					return 0.2;
			}
		}

		public double getMaxRelease() {
			switch (this) {
				case NOTE_LAYER:
					return 6.0;
				case STANDARD_NOTE:
				default:
					return 5.0;
			}
		}
	}
}
