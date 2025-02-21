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
import org.almostrealism.util.KeyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

	public AudioLibraryDataWriter(LibraryDestination destination) {
		this.destination = destination;
		this.executor = Executors.newSingleThreadExecutor();
	}

	public AudioLibraryDataWriter(String key, String prefix) {
		this(new LibraryDestination(prefix));
		restart(key);
	}

	public Consumer<Audio.WaveDetailData> queueRecording() {
		return data -> getRecordings().add(data);
	}

	public String start() { return start(KeyUtils.generateKey()); }

	public String start(String key) {
		if (this.key != null) {
			throw new IllegalArgumentException();
		}

		this.key = key;
		return key;
	}

	public void reset() {
		write(recordings);
		this.count = 0;
		this.recordings = null;
		this.key = null;
	}

	public String restart() {
		return restart(KeyUtils.generateKey());
	}

	public String restart(String key) {
		reset();
		return start(key);
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
		String currentKey = Objects.requireNonNull(key);
		log("Writing " + recordings.size() + " recording chunks");
		executor.submit(() -> {
			try {
				AudioLibraryPersistence.saveRecordings(
						List.of(Audio.WaveRecording.newBuilder()
								.setKey(currentKey).setOrderIndex(index)
								.addAllData(recordings).build()),
						destination.out());
				log("Saved " + recordings.size() + " recording chunks (" +
						recordings.stream().filter(Audio.WaveDetailData::getSilent).count() + " silent)");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
