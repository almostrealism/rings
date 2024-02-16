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

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;

public class ParameterizedVolumeEnvelope extends ParameterizedEnvelope {
	public static final int MAX_SECONDS = 180;

	public static double maxAttack = 0.5;
	public static double maxDecay = 2.0;
	public static double maxSustain = 0.8;
	public static double maxRelease = 0.5;

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

	public ParameterizedVolumeEnvelope() {
		super();
	}

	public ParameterizedVolumeEnvelope(ParameterFunction attackSelection, ParameterFunction decaySelection,
									   ParameterFunction sustainSelection, ParameterFunction releaseSelection) {
		super(attackSelection, decaySelection, sustainSelection, releaseSelection);
	}

	@Override
	public PatternNote apply(ParameterSet params, PatternNote note) {
		PackedCollection<?> a = new PackedCollection<>(1);
		a.set(0, maxAttack * getAttackSelection().positive().apply(params));

		PackedCollection<?> d = new PackedCollection<>(1);
		d.set(0, maxDecay * getDecaySelection().positive().apply(params));

		PackedCollection<?> s = new PackedCollection<>(1);
		s.set(0, maxSustain * getSustainSelection().positive().apply(params));

		PackedCollection<?> r = new PackedCollection<>(1);
		r.set(0, maxRelease * getReleaseSelection().positive().apply(params));

		return PatternNote.create(note, (audio, duration) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> dr = duration.get().evaluate();

			return env.evaluate(audioData, dr, a, d, s, r);
		});
	}

	public static ParameterizedVolumeEnvelope random() {
		return new ParameterizedVolumeEnvelope(
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}
}
