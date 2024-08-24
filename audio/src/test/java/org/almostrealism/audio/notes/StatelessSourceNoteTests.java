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

package org.almostrealism.audio.notes;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.pattern.PatternElement;
import org.almostrealism.audio.pattern.PatternFeatures;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatelessSourceNoteTests implements CellFeatures, SamplingFeatures, PatternFeatures {
	int sampleRate = OutputLine.sampleRate;

	@Test
	public void sine() {
		double duration = 8.0;
		KeyboardTuning tuning = new DefaultKeyboardTuning();
		KeyPosition<WesternChromatic> c = WesternChromatic.C3;

		double amp = 0.25;
		int frames = (int) (2.0 * sampleRate);

		StatelessSource sine = (params, frequency) -> sampling(sampleRate, () -> {
			CollectionProducer<PackedCollection<?>> f =
					multiply(c(tuning.getTone(c).asHertz()), frequency);
			CollectionProducer<PackedCollection<?>> t =
					integers(0, frames).divide(sampleRate);
			return sin(t.multiply(2 * Math.PI).multiply(f)).multiply(amp);
		});

		StatelessSourceNoteAudio audio = new StatelessSourceNoteAudio(sine, c, 2.0);
		PatternNote note = new PatternNote(List.of(audio));
		note.setTuning(tuning);

		AudioSceneContext sceneContext = new AudioSceneContext();
		sceneContext.setFrameForPosition(pos -> (int) (pos * sampleRate));
		sceneContext.setScaleForPosition(pos -> WesternScales.major(WesternChromatic.C3, 1));
		sceneContext.setDestination(new PackedCollection<>((int) (duration * sampleRate)));

		NoteAudioContext audioContext = new NoteAudioContext();
		audioContext.setNextNotePosition(pos -> duration);

		List<PatternElement> elements = new ArrayList<>();
		elements.add(new PatternElement(note, 0.0));
		elements.add(new PatternElement(note, 4.0));
		elements.get(1).setScalePosition(List.of(0.3));

		render(sceneContext, audioContext, elements, true, 0.0);
		new WaveData(sceneContext.getDestination(), sampleRate)
				.save(new File("results/sine-notes.wav"));
	}
}
