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

import io.almostrealism.relation.Factor;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuned;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.IdentityFactor;

import java.util.function.DoubleFunction;

// TODO  This is basically a clone of StatelessSourceNoteAudio
@Deprecated
public class StatelessSourcePatternNote implements PatternNoteAudio, KeyboardTuned, CodeFeatures {
	private final StatelessSource source;
	private final KeyPosition<?> root;
	private final double duration;

	private KeyboardTuning tuning;
	private Factor<PackedCollection<?>> parameters;

	public StatelessSourcePatternNote(StatelessSource source, KeyPosition<?> root, double duration) {
		this.source = source;
		this.root = root;
		this.duration = duration;
		this.parameters = new IdentityFactor<>();
	}

	public KeyboardTuning getTuning() {
		return tuning;
	}

	@Override
	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;
	}

	public Factor<PackedCollection<?>> getParameters() {
		return parameters;
	}

	public void setParameters(Factor<PackedCollection<?>> parameters) {
		this.parameters = parameters;
	}

	@Override
	public int getSampleRate(KeyPosition<?> target, DoubleFunction<PatternNoteAudio> audioSelection) {
		return OutputLine.sampleRate;
	}

	@Override
	public double getDuration(KeyPosition<?> target, DoubleFunction<PatternNoteAudio> audioSelection) {
		return duration;
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, double noteDuration,
												  Producer<PackedCollection<?>> automationLevel,
												  DoubleFunction<PatternNoteAudio> audioSelection) {
		return source.generate(getBufferDetails(target, audioSelection),
				getParameters().getResultant(automationLevel),
				c(tuning.getRelativeFrequency(root, target).asHertz()));
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target,
												  DoubleFunction<PatternNoteAudio> audioSelection) {
		return source.generate(getBufferDetails(target, audioSelection),
				getParameters().getResultant(c(1.0)),
				c(tuning.getRelativeFrequency(root, target).asHertz()));
	}
}
