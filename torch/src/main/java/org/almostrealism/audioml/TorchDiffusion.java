/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audioml;

import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteSource;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TorchDiffusion implements ProcessFeatures {
	private static final String AUDIO = "audio";
	
	public void clearDatasets() {
		Stream.of(new File(AUDIO).list()).forEach(run -> run("rm", "-rf", AUDIO + "/" + run));
	}

	public void loadAudio(List<PatternNoteSource> sources) {
		List<PatternNote> audio = sources.stream()
				.flatMap(s -> s.getNotes().stream())
				.filter(PatternNote::isValid)
				.collect(Collectors.toList());
		System.out.println("TorchDiffusion: Saving " + audio.size() + " audio files");
		IntStream.range(0, audio.size()).forEach(i ->
						new WaveData(audio.get(i).getAudio(), OutputLine.sampleRate)
								.save(new File("audio/" + i + ".wav")));
		System.out.println("TorchDiffusion: Done saving audio files");
	}

	public void train() {
		script("train.sh");
	}

	private void script(String script) {
		run("sh", script);
	}
}
