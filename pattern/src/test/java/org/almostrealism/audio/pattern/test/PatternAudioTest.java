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

package org.almostrealism.audio.pattern.test;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ChannelInfo;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.filter.EnvelopeSection;
import org.almostrealism.audio.filter.ParameterizedVolumeEnvelope;
import org.almostrealism.audio.notes.PatternNoteLayer;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.relation.Factor;
import org.junit.Test;

import java.io.File;

public class PatternAudioTest implements EnvelopeFeatures {

	@Test
	public void noteAudio() {
		PatternNoteLayer note = PatternNoteLayer.create("Library/SN_Forever_Future.wav", WesternChromatic.C1);
		note.setTuning(new DefaultKeyboardTuning());
		new WaveData(note.getAudio(WesternChromatic.G2, 1.0, null, null).get().evaluate(),
					note.getSampleRate(WesternChromatic.G2, null))
				.save(new File("results/pattern-note-audio.wav"));
	}

	@Test
	public void envelope() {
		PatternNoteLayer note = PatternNoteLayer.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNoteLayer.create(note, attack(c(0.5)));
		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1, 1.0, null, null).get().evaluate(),
					note.getSampleRate(WesternChromatic.G2, null))
				.save(new File("results/pattern-note-envelope.wav"));
	}

	@Test
	public void conditionalEnvelope() {
		Factor<PackedCollection<?>> factor = in ->
				greaterThanConditional(time(), c(1.0),
						volume(c(0.5)).getResultant(in),
						attack(c(0.5)).getResultant(in));

		Evaluable<PackedCollection<?>> env =
				sampling(OutputLine.sampleRate, ParameterizedVolumeEnvelope.MAX_SECONDS,
						() -> factor.getResultant(v(1, 0))).get();

		PatternNoteLayer note = PatternNoteLayer.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNoteLayer.create(note, (audio, duration, automationLevel) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> dr = duration.get().evaluate();

			return env.evaluate(audioData, dr);
		});

		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1, 1.0, null, null).get().evaluate(),
					note.getSampleRate(WesternChromatic.C1, null))
				.save(new File("results/pattern-note-conditional-envelope.wav"));
	}

	@Test
	public void envelopePassThrough() {
		Factor<PackedCollection<?>> factor = envelope(attack(c(0.5)))
				.andThenDecay(c(0.5), c(1.0), c(0.0)).get();
		Evaluable<PackedCollection<?>> env =
				sampling(OutputLine.sampleRate, ParameterizedVolumeEnvelope.MAX_SECONDS,
					() -> factor.getResultant(v(1, 0))).get();

		PatternNoteLayer note = PatternNoteLayer.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNoteLayer.create(note, (audio, duration, automationLevel) -> () -> args -> {
			PackedCollection<?> audioData = audio.get().evaluate();
			PackedCollection<?> dr = duration.get().evaluate();

			return env.evaluate(audioData, dr);
		});

		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1, 1.0, null, null).get().evaluate(),
					note.getSampleRate(null, null))
				.save(new File("results/pattern-note-envelope-passthrough.wav"));
	}

	@Test
	public void envelopeSections() {
		EnvelopeSection section = envelope(attack(c(0.5)))
									.andThen(c(0.5), sustain(c(3.2)));

		PatternNoteLayer note = PatternNoteLayer.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNoteLayer.create(note, section.get());
		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1, 1.0, null, null).get().evaluate(),
					note.getSampleRate(WesternChromatic.C1, null))
				.save(new File("results/pattern-note-envelope-sections.wav"));
	}

	@Test
	public void parameterizedEnvelope() {
		ParameterizedVolumeEnvelope envelope = ParameterizedVolumeEnvelope.random(ParameterizedVolumeEnvelope.Mode.STANDARD_NOTE);

		PatternNoteLayer note = PatternNoteLayer.create("Library/SN_Forever_Future.wav", WesternChromatic.C1);
		note = envelope.apply(new ParameterSet(), ChannelInfo.Voicing.MAIN, note);
		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1, 1.0, null, null).get().evaluate(),
					note.getSampleRate(WesternChromatic.C1, null))
				.save(new File("results/pattern-note-param-envelope.wav"));
	}
}
