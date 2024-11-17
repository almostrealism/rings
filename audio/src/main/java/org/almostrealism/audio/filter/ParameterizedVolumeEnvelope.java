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
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.data.ChannelInfo;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioFilter;
import org.almostrealism.collect.PackedCollection;

import java.util.List;

public class ParameterizedVolumeEnvelope extends ParameterizedEnvelopeAdapter {

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
	public NoteAudioFilter createFilter(ParameterSet params, ChannelInfo.Voicing voicing) {
		return new Filter(params, voicing);
	}

	@JsonIgnore
	@Override
	public Class getLogClass() {
		return super.getLogClass();
	}

	public class Filter implements NoteAudioFilter {
		private ParameterSet params;
		private ChannelInfo.Voicing voicing;

		public Filter(ParameterSet params, ChannelInfo.Voicing voicing) {
			this.params = params;
			this.voicing = voicing;
		}

		public ChannelInfo.Voicing getVoicing() {
			return voicing;
		}

		public double getAttack() {
			return mode.getMaxAttack(getVoicing()) * getAttackSelection().positive().apply(params);
		}

		public double getDecay() {
			return mode.getMaxDecay(getVoicing()) * getDecaySelection().positive().apply(params);
		}

		public double getSustain() {
			return mode.getMaxSustain(getVoicing()) * getSustainSelection().positive().apply(params);
		}

		public double getRelease() {
			return mode.getMaxRelease(getVoicing()) * getReleaseSelection().positive().apply(params);
		}

		@Override
		public Producer<PackedCollection<?>> apply(Producer<PackedCollection<?>> audio,
												   Producer<PackedCollection<?>> duration,
												   Producer<PackedCollection<?>> automationLevel) {
			PackedCollection<?> a = new PackedCollection<>(1);
			PackedCollection<?> d = new PackedCollection<>(1);
			PackedCollection<?> s = new PackedCollection<>(1);
			PackedCollection<?> r = new PackedCollection<>(1);

			return () -> args -> {
				PackedCollection<?> audioData = audio.get().evaluate();
				PackedCollection<?> dr = duration.get().evaluate();
				PackedCollection<?> al = automationLevel.get().evaluate();

				a.set(0, getAttack());
				d.set(0, getDecay());
				s.set(0, getSustain() * al.toDouble(0));
				r.set(0, getRelease() * al.toDouble(0));

				PackedCollection<?> out = AudioProcessingUtils.getVolumeEnv()
						.evaluate(audioData.traverse(1), dr, a, d, s, r);

				if (out.getShape().getTotalSize() == 1) {
					warn("Envelope produced a value with shape " +
							out.getShape().toStringDetail());
				}

				return out;
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

		public double getMaxAttack(ChannelInfo.Voicing voicing) {
			return switch (this) {
				case NOTE_LAYER -> 2.0;
				default -> voicing == ChannelInfo.Voicing.WET ? 1.5 : 0.5;
			};
		}

		public double getMaxDecay(ChannelInfo.Voicing voicing) {
			switch (this) {
				case NOTE_LAYER:
					return 3.0;
				case STANDARD_NOTE:
				default:
					return 2.0;
			}
		}

		public double getMaxSustain(ChannelInfo.Voicing voicing) {
			switch (this) {
				case NOTE_LAYER:
					return 2.0;
				case STANDARD_NOTE:
				default:
					return voicing == ChannelInfo.Voicing.WET ? 1.0 : 0.8;
			}
		}

		public double getMaxRelease(ChannelInfo.Voicing voicing) {
			switch (this) {
				case NOTE_LAYER:
					return 2.0;
				case STANDARD_NOTE:
				default:
					return voicing == ChannelInfo.Voicing.WET ? 1.2 : 0.5;
			}
		}
	}

	public static ParameterizedVolumeEnvelope random(Mode mode) {
		return new ParameterizedVolumeEnvelope(mode,
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}
}
