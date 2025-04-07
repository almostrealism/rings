/*
 * Copyright 2025 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express odfdfr implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.generate.test;

import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.notes.StatelessSourceNoteAudio;
import org.almostrealism.audio.persistence.GeneratedSourceLibrary;
import org.almostrealism.audio.persistence.LibraryDestination;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.synth.InterpolatedAudioSynthesisModel;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class AudioSynthesizerTests implements TestFeatures {
	private LibraryDestination library = new LibraryDestination("model");

	@Test
	public void generate() {
		double lfo1 = 0.5;
		double lfo2 = 1.1;
		PackedCollection<?> levelData = new PackedCollection<>(shape(2, 10 * OutputLine.sampleRate));
		levelData.fill(pos -> {
			int i = pos[0];
			double j = pos[1];
			double t = (j / OutputLine.sampleRate) + (i == 0 ? 0 : 0.3);
			return Math.sin(2 * Math.PI * (i == 0 ? lfo1 : lfo2) * t);
		});

		String key = "test-synth";
		GeneratedSourceLibrary models = new GeneratedSourceLibrary(library);
		models.add(key, new InterpolatedAudioSynthesisModel(
				new double[] {1.0, 4.0}, OutputLine.sampleRate, levelData));

		StatelessSource source = models.getSource(key);
		StatelessSourceNoteAudio synth = new StatelessSourceNoteAudio(source,
				new BufferDetails(OutputLine.sampleRate, 10.0), null);
		synth.setTuning(new DefaultKeyboardTuning());

		PackedCollection<?> audio = synth.getAudio(WesternChromatic.G3).evaluate();
		new WaveData(audio, synth.getSampleRate())
				.save(new File("results/test-synth.wav"));
	}
}
