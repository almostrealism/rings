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
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.collect.PackedCollection;

public class ParameterizedFilterEnvelope extends ParameterizedEnvelope {
	public static final int MAX_SECONDS = 90;

	public static double maxAttack = 0.1;
	public static double maxDecay = 0.05;
	public static double maxSustain = 0.2;
	public static double maxRelease = 5.0;

	private static FilterEnvelopeProcessor processor;

	static {
		processor = new FilterEnvelopeProcessor(44100, MAX_SECONDS);
	}

	public ParameterizedFilterEnvelope() {
		super();
	}

	public ParameterizedFilterEnvelope(ParameterFunction attackSelection, ParameterFunction decaySelection,
									   ParameterFunction sustainSelection, ParameterFunction releaseSelection) {
		super(attackSelection, decaySelection, sustainSelection, releaseSelection);
	}

	@Override
	public PatternNote apply(ParameterSet params, PatternNote note) {
		return PatternNote.create(note, (audio, duration) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> result = new PackedCollection<>(shape(audioData));
			PackedCollection<?> dr = duration.get().evaluate();

			processor.setDuration(dr.toDouble(0));
			processor.setAttack(maxAttack * getAttackSelection().positive().apply(params));
			processor.setDecay(maxDecay * getDecaySelection().positive().apply(params));
			processor.setSustain(maxSustain * getSustainSelection().positive().apply(params));
			processor.setRelease(maxRelease * getReleaseSelection().positive().apply(params));

			processor.process(audioData, result);
			return result;
		});
	}

	public static ParameterizedFilterEnvelope random() {
		return new ParameterizedFilterEnvelope(
				ParameterFunction.random(), ParameterFunction.random(),
				ParameterFunction.random(), ParameterFunction.random());
	}
}
