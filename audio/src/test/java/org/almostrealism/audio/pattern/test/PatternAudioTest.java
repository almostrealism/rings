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

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.filter.EnvelopeSection;
import org.almostrealism.audio.pattern.PatternAudio;
import org.almostrealism.audio.pattern.PatternElement;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.junit.Test;

import java.io.File;

public class PatternAudioTest implements EnvelopeFeatures {
	@Test
	public void push() {
		PackedCollection in = new PackedCollection(5);
		Scalar s = new Scalar(in, 0);
		s.setLeft(4);
		s.setRight(6);

		PatternElement e = new PatternElement(PatternNote.create(() -> in), 0.5);
		PatternAudio audio = new PatternAudio(60, 1.0, 1.0, 10);
		audio.push(e);

		Scalar out = new Scalar(audio.getData(), 5);
		System.out.println(out);
	}

	@Test
	public void noteAudio() {
		PatternNote note = PatternNote.create("Library/Monarch_C1.wav", WesternChromatic.C1);
		note.setTuning(new DefaultKeyboardTuning());
		new WaveData(note.getAudio(WesternChromatic.C2).get().evaluate(), note.getSampleRate())
				.save(new File("results/Monarch_C2.wav"));
	}

	@Test
	public void envelope() {
		PatternNote note = PatternNote.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNote.create(note, attack(c(0.5)));
		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1).get().evaluate(), note.getSampleRate())
				.save(new File("results/pattern-note-envelope.wav"));
	}

	@Test
	public void envelopeSections() {
		EnvelopeSection section = envelope(attack(c(0.5)))
									.andThen(c(0.5), sustain(c(3.2)));

		PatternNote note = PatternNote.create("Library/Snare Perc DD.wav", WesternChromatic.C1);
		note = PatternNote.create(note, section.get());
		note.setTuning(new DefaultKeyboardTuning());

		new WaveData(note.getAudio(WesternChromatic.C1).get().evaluate(), note.getSampleRate())
				.save(new File("results/pattern-note-envelope-sections.wav"));
	}
}
