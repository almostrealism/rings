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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioFilter;
import org.almostrealism.collect.PackedCollection;

import java.util.List;

public class ParameterizedFilterEnvelope extends ParameterizedEnvelopeAdapter {
	public static final int MAX_SECONDS = 90;

	public static boolean enableMultiOrderFilter = true;

	private static EnvelopeProcessor processor;

	static {
		if (enableMultiOrderFilter) {
			processor = new MultiOrderFilterEnvelopeProcessor(44100, MAX_SECONDS);
		} else {
			processor = new FilterEnvelopeProcessor(44100, MAX_SECONDS);
		}
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
		return new Filter(params);
	}

	public class Filter implements NoteAudioFilter {
		private ParameterSet params;

		public Filter(ParameterSet params) {
			this.params = params;
		}

		public double getAttack() {
			return mode.getMaxAttack() * getAttackSelection().positive().apply(params);
		}

		public double getDecay() {
			return mode.getMaxDecay() * getDecaySelection().positive().apply(params);
		}

		public double getSustain() {
			return mode.getMaxSustain() * getSustainSelection().positive().apply(params);
		}

		public double getRelease() {
			return mode.getMaxRelease() * getReleaseSelection().positive().apply(params);
		}

		@Override
		public Producer<PackedCollection<?>> apply(Producer<PackedCollection<?>> audio,
												   Producer<PackedCollection<?>> duration) {
			return () -> args -> {
				PackedCollection<?> audioData = audio.get().evaluate();
				PackedCollection<?> result = new PackedCollection<>(shape(audioData));
				PackedCollection<?> dr = duration.get().evaluate();

				processor.setDuration(dr.toDouble(0));
				processor.setAttack(getAttack());
				processor.setDecay(getDecay());
				processor.setSustain(getSustain());
				processor.setRelease(getRelease());
				processor.process(audioData, result);
				return result;
			};
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			Filter filter = (Filter) obj;

			if (filter.getAttack() != getAttack()) return false;
			if (filter.getDecay() != getDecay()) return false;
			if (filter.getSustain() != getSustain()) return false;
			if (filter.getRelease() != getRelease()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return List.of(getAttack(), getDecay(), getSustain(), getRelease()).hashCode();
		}
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

	public static ParameterizedFilterEnvelope random(Mode mode) {
		return new ParameterizedFilterEnvelope(mode,
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}
}
