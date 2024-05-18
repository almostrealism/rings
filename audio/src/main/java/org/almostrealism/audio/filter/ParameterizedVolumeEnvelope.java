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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioFilter;
import org.almostrealism.audio.notes.PatternNoteLayer;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;

public class ParameterizedVolumeEnvelope extends ParameterizedEnvelope {
	public static final int MAX_SECONDS = 180;

	private static Evaluable<PackedCollection<?>> env;

	static {
		EnvelopeFeatures o = EnvelopeFeatures.getInstance();

		Factor<PackedCollection<?>> factor =
				o.envelope(o.v(1, 1),
						o.v(1, 2), o.v(1, 3),
						o.v(1, 4), o.v(1, 5)).get();
		env = o.sampling(OutputLine.sampleRate, MAX_SECONDS,
				() -> factor.getResultant(o.v(1, 0))).get();
	}

	private Mode mode;

	public ParameterizedVolumeEnvelope() {
		super();
		mode = Mode.STANDARD_NOTE;
	}

	public ParameterizedVolumeEnvelope(Mode mode, ParameterFunction attackSelection, ParameterFunction decaySelection,
									   ParameterFunction sustainSelection, ParameterFunction releaseSelection) {
		super(attackSelection, decaySelection, sustainSelection, releaseSelection);
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public NoteAudioFilter createFilter(ParameterSet params) {
		PackedCollection<?> a = new PackedCollection<>(1);
		a.set(0, mode.getMaxAttack() * getAttackSelection().positive().apply(params));

		PackedCollection<?> d = new PackedCollection<>(1);
		d.set(0, mode.getMaxDecay() * getDecaySelection().positive().apply(params));

		PackedCollection<?> s = new PackedCollection<>(1);
		s.set(0, mode.getMaxSustain() * getSustainSelection().positive().apply(params));

		PackedCollection<?> r = new PackedCollection<>(1);
		r.set(0, mode.getMaxRelease() * getReleaseSelection().positive().apply(params));

		return (audio, duration) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> dr = duration.get().evaluate();

			PackedCollection<?> out = env.evaluate(audioData, dr, a, d, s, r);
			return out;
		};
	}

	@JsonIgnore
	@Override
	public Class getLogClass() {
		return super.getLogClass();
	}

	public static ParameterizedVolumeEnvelope random(Mode mode) {
		return new ParameterizedVolumeEnvelope(mode,
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}

	public enum Mode {
		STANDARD_NOTE, NOTE_LAYER;

		public double getMaxAttack() {
			switch (this) {
				case NOTE_LAYER:
					return 2.0;
				case STANDARD_NOTE:
				default:
					return 0.5;
			}
		}

		public double getMaxDecay() {
			switch (this) {
				case NOTE_LAYER:
					return 3.0;
				case STANDARD_NOTE:
				default:
					return 2.0;
			}
		}

		public double getMaxSustain() {
			switch (this) {
				case NOTE_LAYER:
					return 2.0;
				case STANDARD_NOTE:
				default:
					return 0.8;
			}
		}

		public double getMaxRelease() {
			switch (this) {
				case NOTE_LAYER:
					return 2.0;
				case STANDARD_NOTE:
				default:
					return 0.5;
			}
		}
	}
}
