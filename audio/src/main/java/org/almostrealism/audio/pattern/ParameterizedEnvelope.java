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

package org.almostrealism.audio.pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;

public class ParameterizedEnvelope implements EnvelopeFeatures {
	public static final int MAX_SECONDS = 180;

	public static double maxAttack = 0.5; // 1.0;
	public static double maxDecay = 2.0;
	public static double maxSustain = 0.8;
	public static double maxRelease = 0.5; // 3.0;

	private ParameterFunction attackSelection;
	private ParameterFunction decaySelection;
	private ParameterFunction sustainSelection;
	private ParameterFunction releaseSelection;

	@JsonIgnore
	// TODO  Can't this be static, and reused by all envelopes?
	private Evaluable<PackedCollection<?>> env;

	public ParameterizedEnvelope() {
		Factor<PackedCollection<?>> factor =
				envelope(v(1, 1),
						v(1, 2), v(1, 3),
						v(1, 4), v(1, 5)).get();
		env = sampling(OutputLine.sampleRate, MAX_SECONDS,
				() -> factor.getResultant(v(1, 0))).get();
	}

	public ParameterizedEnvelope(ParameterFunction attackSelection, ParameterFunction decaySelection,
								 ParameterFunction sustainSelection, ParameterFunction releaseSelection) {
		this();
		this.attackSelection = attackSelection;
		this.decaySelection = decaySelection;
		this.sustainSelection = sustainSelection;
		this.releaseSelection = releaseSelection;
	}

	public ParameterFunction getAttackSelection() { return attackSelection; }
	public void setAttackSelection(ParameterFunction attackSelection) { this.attackSelection = attackSelection; }

	public ParameterFunction getDecaySelection() { return decaySelection; }
	public void setDecaySelection(ParameterFunction decaySelection) { this.decaySelection = decaySelection; }

	public ParameterFunction getSustainSelection() { return sustainSelection; }
	public void setSustainSelection(ParameterFunction sustainSelection) { this.sustainSelection = sustainSelection; }

	public ParameterFunction getReleaseSelection() { return releaseSelection; }
	public void setReleaseSelection(ParameterFunction releaseSelection) { this.releaseSelection = releaseSelection; }

	public PatternNote apply(ParameterSet params, PatternNote note) {
		PackedCollection<?> a = new PackedCollection<>(1);
		a.set(0, maxAttack * attackSelection.positive().apply(params));

		PackedCollection<?> d = new PackedCollection<>(1);
		d.set(0, maxDecay * decaySelection.positive().apply(params));

		PackedCollection<?> s = new PackedCollection<>(1);
		s.set(0, maxSustain * sustainSelection.positive().apply(params));

		PackedCollection<?> r = new PackedCollection<>(1);
		r.set(0, maxRelease * releaseSelection.positive().apply(params));

//		System.out.println("a: " + a.toDouble(0) + ", d: " + d.toDouble(0) +
//				", s: " + s.toDouble(0) + ", r: " + r.toDouble(0));

		return PatternNote.create(note, (audio, duration) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> dr = duration.get().evaluate();

			return env.evaluate(audioData, dr, a, d, s, r);
		});
	}

	public static ParameterizedEnvelope random() {
		return new ParameterizedEnvelope(ParameterFunction.random(), ParameterFunction.random(),
										ParameterFunction.random(), ParameterFunction.random());
	}
}
