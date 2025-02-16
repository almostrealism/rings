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

package org.almostrealism.audio.persistence;

import org.almostrealism.audio.api.Audio;
import org.almostrealism.io.ConsoleFeatures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AudioLibraryDataWriter implements ConsoleFeatures {
	public static final int RECORD_COUNT = 4;

	private String key;
	private LibraryDestination destination;
	private ExecutorService executor;

	private List<Audio.WaveDetailData> recordings;
	private int count;

	public AudioLibraryDataWriter(String key, String prefix) {
		this.key = key;
		this.destination = new LibraryDestination(prefix);
		this.executor = Executors.newSingleThreadExecutor();
	}

	public Consumer<Audio.WaveDetailData> queueRecording() {
		return data -> getRecordings().add(data);
	}

	protected List<Audio.WaveDetailData> getRecordings() {
		if (recordings == null || recordings.size() >= RECORD_COUNT) {
			write(recordings);
			recordings = new ArrayList<>();
		}

		return recordings;
	}

	protected void write(List<Audio.WaveDetailData> recordings) {
		if (recordings == null) return;

		int index = count++;
		log("Writing " + recordings.size() + " recording chunks");
		executor.submit(() -> {
			try {
				AudioLibraryPersistence.saveRecordings(
						List.of(Audio.WaveRecording.newBuilder()
								.setKey(key).setOrderIndex(index)
								.addAllData(recordings).build()),
						destination.out());
				log("Saved " + recordings.size() + " recording chunks");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
