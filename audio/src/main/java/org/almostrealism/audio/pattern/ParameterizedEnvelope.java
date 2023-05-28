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

import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.notes.PatternNote;

public class ParameterizedEnvelope implements EnvelopeFeatures {
	public PatternNote apply(PatternNote note) {
//		return PatternNote.create(note, attack(c(0.2)));
		return PatternNote.create(note, in -> multiply(in, c(1.1)));
	}

	public static ParameterizedEnvelope random() {
		return new ParameterizedEnvelope();
	}
}
