/*
 * Copyright 2024 Michael Murray
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

package com.almostrealism.audio.test;

import com.almostrealism.audio.AudioLibraryPersistence;
import org.almostrealism.audio.AudioLibrary;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
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
		WaveDetails details = library.getDetails(new FileWaveDataProvider("/Users/michael/Music/Samples/Essential WAV From Mars/Drums/02. Kits/707 From Mars/03. Mod Kit 1/Ride 707 Mod 35.wav"));
		System.out.println(details.getFreqSampleRate());
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

