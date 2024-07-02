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
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.util.ProcessFeatures;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TorchDiffusion implements ProcessFeatures {
	public static boolean enableVirtualEnv = true;

	private static final String AUDIO = "audio";
	private static final String MODELS = "models";

	public void clearDatasets() {
		Stream.of(new File(AUDIO).list()).forEach(run -> run("rm", "-rf", AUDIO + "/" + run));
	}

	public void clearModel() {
		run("rm", "-rf", MODELS + "/latest.zip");
		run("rm", "-rf", MODELS + "/latest");
	}

	public void loadAudio(List<NoteAudioSource> sources) {
		List<NoteAudioProvider> audio = sources.stream()
				.flatMap(s -> s.getNotes().stream())
				.filter(NoteAudioProvider::isValid)
				.collect(Collectors.toList());
		System.out.println("TorchDiffusion: Saving " + audio.size() + " audio files");
		IntStream.range(0, audio.size()).forEach(i ->
						new WaveData(audio.get(i).getAudio(), OutputLine.sampleRate)
								.save(new File("audio/" + i + ".wav")));
		System.out.println("TorchDiffusion: Done saving audio files");
	}

	public void train() {
		if (!script(enableVirtualEnv ? "train_venv.sh" : "train.sh"))
			throw new RuntimeException();
	}

	public void generate(int count) {
		if (!script("generate.sh", String.valueOf(count)))
			throw new RuntimeException();
	}

	private boolean script(String script, String... args) {
		return 0 == run(Stream.concat(Stream.of("sh", script), Stream.of(args)).toArray(String[]::new));
	}
}
