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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.test;

import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.api.Audio;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.persistence.AudioLibraryPersistence;
import org.almostrealism.audio.AudioLibrary;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.audio.stream.AudioServer;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AudioLibraryTests implements TestFeatures {
	public static String LIBRARY = "Library";

	static {
		String env = System.getenv("AR_RINGS_LIBRARY");
		if (env != null) LIBRARY = env;

		String arg = System.getProperty("AR_RINGS_LIBRARY");
		if (arg != null) LIBRARY = arg;
	}

	@Test
	public void loadDetails() {
		AudioLibrary library = AudioLibrary.load(new File(LIBRARY), OutputLine.sampleRate);
		WaveDetails details = library.getDetails(new FileWaveDataProvider("/Users/michael/Music/Samples/Essential WAV From Mars/Drums/02. Kits/707 From Mars/03. Mod Kit 1/Ride 707 Mod 35.wav"), true);
		System.out.println(details.getFreqSampleRate());
	}

	@Test
	public void loadRecording() throws IOException {
		List<String> keys = new ArrayList<>(AudioLibraryPersistence.listRecordings("recordings"));

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			List<Audio.WaveDetailData> data = AudioLibraryPersistence.loadRecording(key, "recordings");
			log("Recording " + i + " contains " + data.size() + " parts " +
							Arrays.toString(data.stream().mapToInt(d -> d.getSilent() ? 0 : 1).toArray()));
			WaveData wave = AudioLibraryPersistence.toWaveData(data);
			WavFile.write(wave, new File("results/recording_" + i + ".wav"));
		}
	}

	@Test
	public void streamRecording() throws IOException, InterruptedException {
		String key = AudioLibraryPersistence.listRecordings("recordings")
				.stream().findFirst().orElse(null);
		List<Audio.WaveDetailData> data =
				AudioLibraryPersistence.loadRecording(key, "recordings");
		WaveData wave = AudioLibraryPersistence.toWaveData(data);

		AudioServer server = new AudioServer(7800);
		String channel = server.addStream("test", wave);
		log("Channel: " + channel);
		server.start();

		Thread.sleep(60 * 60 * 1000);
	}

	@Test
	public void libraryRefresh() {
		AudioLibrary library = AudioLibrary.load(new File(LIBRARY), OutputLine.sampleRate);
		library.refresh();

		AudioLibraryPersistence.saveLibrary(library, "library");
	}

	@Test
	public void similarities() {
		AudioLibrary library = AudioLibraryPersistence.loadLibrary(new File(LIBRARY), OutputLine.sampleRate, "library");

//		String f = "/Users/michael/Music/Samples/Essential WAV From Mars/Drums/02. Kits/707 From Mars/03. Mod Kit 1/Clap 707 Mod 37.wav";
		String f = "/Users/michael/Music/Samples/Essential WAV From Mars/Drums/01. Individual Hits/12. Various Percussion/Bongo 727 Clean Lo.wav";

		log("Similarities for " + f);

		Map<String, Double> similarities = library.getSimilarities(f);
		similarities.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.map(library::find)
				.map(WaveDataProvider::getKey)
				.limit(20)
				.forEach(System.out::println);
	}
}

