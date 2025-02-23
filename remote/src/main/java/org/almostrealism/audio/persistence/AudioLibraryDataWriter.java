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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AudioLibraryDataWriter implements ConsoleFeatures {
	public static final int RECORD_SIZE = 64;
	public static final int WRITE_SIZE = 32;

	private String groupKey;
	private LibraryDestination destination;
	private ExecutorService executor;

	private String sampleKey;
	private List<Audio.WaveDetailData> buffer;
	private int sampleCount;
	private Consumer<String> sampleListener;

	private BlockingQueue<Audio.WaveRecording> queue;
	private int groupCount;

	public AudioLibraryDataWriter(LibraryDestination destination) {
		this.destination = destination;
		this.executor = Executors.newSingleThreadExecutor();
		this.queue = new ArrayBlockingQueue<>(WRITE_SIZE * 2);
	}

	public AudioLibraryDataWriter(String groupKey, String prefix) {
		this(new LibraryDestination(prefix));
		restart(groupKey);
	}

	public Consumer<String> getSampleListener() {
		return sampleListener;
	}

	public void setSampleListener(Consumer<String> sampleListener) {
		this.sampleListener = sampleListener;
	}

	public String start() { return start(KeyUtils.generateKey()); }

	public String start(String groupKey) {
		if (this.groupKey != null) {
			throw new IllegalArgumentException();
		}

		this.groupKey = groupKey;
		return groupKey;
	}

	public void reset() {
		flushBuffer();
		flushQueue();
		this.buffer = null;
		this.groupKey = null;
		this.sampleKey = null;
		this.groupCount = 0;
		this.sampleCount = 0;
	}

	public String restart() {
		return restart(KeyUtils.generateKey());
	}

	public String restart(String key) {
		reset();
		return start(key);
	}

	public void queueData(Audio.WaveDetailData data) {
		if (buffer == null || buffer.size() >= RECORD_SIZE) {
			flushBuffer();
		}

		if (data.getSilent()) {
			// Sample is over when silence is detected
			endSample();
		} else if (sampleKey == null) {
			// Start a new sample when sound is detected,
			// if there is not already a sample in progress
			startSample();
		}

		buffer.add(data);
	}

	public void startSample() {
		if (sampleKey != null) {
			throw new UnsupportedOperationException();
		}

		flushBuffer();
		sampleKey = KeyUtils.generateKey();
	}

	public void endSample() {
		if (sampleKey != null) {
			flushBuffer();
			sampleListener.accept(sampleKey);
		}

		sampleKey = null;
		sampleCount = 0;
	}

	protected void flushBuffer() {
		if (buffer == null) {
			buffer = new ArrayList<>();
		} else if (!buffer.isEmpty()) {
			queueRecording(buffer);
			buffer = new ArrayList<>();
		}
	}

	protected void queueRecording(List<Audio.WaveDetailData> buffer) {
		Audio.WaveRecording.Builder r = Audio.WaveRecording.newBuilder()
				.setGroupKey(groupKey).setGroupOrderIndex(groupCount++)
				.addAllData(buffer);
		if (sampleKey != null) {
			r.setKey(sampleKey).setOrderIndex(sampleCount++);
		}

		queueRecording(r.build());
	}

	protected void queueRecording(Audio.WaveRecording recording) {
		queue.add(recording);

		if (queue.size() >= WRITE_SIZE) {
			flushQueue();
		}
	}

	protected void flushQueue() {
		List<Audio.WaveRecording> recordings = new ArrayList<>();
		queue.drainTo(recordings);
		executor.submit(() -> {
			try {
				AudioLibraryPersistence.saveRecordings(recordings, destination.out());
//				totalData += recordings.stream()
//						.mapToInt(Audio.WaveRecording::getDataCount)
//						.sum();
//				log("Saved " + totalData + " recording chunks so far (" +
//						Arrays.toString(recordings.stream()
//								.map(Audio.WaveRecording::getKey)
//								.toArray()) + ")");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
