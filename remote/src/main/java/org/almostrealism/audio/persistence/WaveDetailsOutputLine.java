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
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.audio.line.BufferDefaults;
import org.almostrealism.audio.line.BufferedAudio;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.util.KeyUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WaveDetailsOutputLine implements OutputLine, ConsoleFeatures {
	private int sampleRate;
	private boolean altBuffer;
	private int cursor;

	private PackedCollection<?> bufferA;
	private PackedCollection<?> bufferB;

	private ExecutorService executor;
	private Consumer<Audio.WaveDetailData> consumer;

	public WaveDetailsOutputLine(AudioLibraryDataWriter writer) {
		this(writer.queueRecording());
	}

	public WaveDetailsOutputLine(Consumer<Audio.WaveDetailData> consumer) {
		this(BufferedAudio.sampleRate, consumer);
	}

	public WaveDetailsOutputLine(int sampleRate, Consumer<Audio.WaveDetailData> consumer) {
		this(sampleRate, 8 * BufferDefaults.defaultBufferSize, consumer);
	}

	public WaveDetailsOutputLine(int sampleRate, int bufferSize,
								 Consumer<Audio.WaveDetailData> consumer) {
		this.sampleRate = sampleRate;
		this.bufferA = new PackedCollection<>(bufferSize);
		this.bufferB = new PackedCollection<>(bufferSize);

		this.executor = Executors.newSingleThreadExecutor();
		this.consumer = consumer;
	}

	@Override
	public int getSampleRate() { return sampleRate; }

	@Override
	public int getBufferSize() { return bufferA.getMemLength(); }

	@Override
	public void write(PackedCollection<?> sample) {
		PackedCollection<?> output = getActiveBuffer();

		if (sample.getMemLength() > output.getMemLength() - cursor) {
			throw new IllegalArgumentException("Sample is too large for destination");
		}

		output.setMem(cursor, sample);
		cursor = (cursor + sample.getMemLength()) % output.getMemLength();

		if (cursor == 0) {
			altBuffer = !altBuffer;
			publish();
		}
	}

	protected PackedCollection<?> getActiveBuffer() {
		return altBuffer ? bufferB : bufferA;
	}

	protected PackedCollection<?> getInactiveBuffer() {
		return altBuffer ? bufferA : bufferB;
	}

	protected void publish() {
		WaveDetails details = new WaveDetails(KeyUtils.generateKey());
		details.setSampleRate(getSampleRate());
		details.setChannelCount(1);
		details.setFrameCount(getBufferSize());
		details.setData(getInactiveBuffer());

		// TODO  If AudioLibraryPersistence.encode is going to happen on another thread
		// TODO  it needs to have a strict time limit or it may still be reading when
		// TODO  the buffer becomes active (on slower machines)
		executor.submit(() ->
				consumer.accept(AudioLibraryPersistence.encode(details, true)));
	}

	@Override
	public void destroy() {
		bufferA.destroy();
		bufferB.destroy();
		executor.shutdown();
		bufferA = null;
		bufferB = null;
		executor = null;
	}
}
