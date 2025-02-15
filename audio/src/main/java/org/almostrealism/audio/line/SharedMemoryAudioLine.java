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

package org.almostrealism.audio.line;

import io.almostrealism.code.ComputeContext;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.io.ConsoleFeatures;

public class SharedMemoryAudioLine implements AudioLine, ConsoleFeatures {
	public static final int controlSize = 8;

	private int cursor;
	private PackedCollection<?> controls;
	private PackedCollection<?> destination;

	public SharedMemoryAudioLine(String location) {
		this(createControls(location), createDestination(location));
	}

	public SharedMemoryAudioLine(PackedCollection<?> controls,
								 PackedCollection<?> destination) {
		this.controls = controls;
		this.destination = destination;
	}

	@Override
	public int getWritePosition() { return cursor; }

	@Override
	public int getReadPosition() {
		return Math.toIntExact((long) controls.toDouble(0));
	}

	@Override
	public int getBufferSize() { return destination.getMemLength(); }

	@Override
	public void read(PackedCollection<?> sample) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(PackedCollection<?> sample) {
		if (sample.getMemLength() > destination.getMemLength() - cursor) {
			throw new IllegalArgumentException("Sample is too large for destination");
		}

		destination.setMem(cursor, sample);
		cursor = (cursor + sample.getMemLength()) % destination.getMemLength();
	}

	@Override
	public void destroy() {
		AudioLine.super.destroy();
		destination.destroy();
		destination = null;
	}

	private static PackedCollection<?> createControls(String location) {
		String shared = location + "_ctl";
		return createCollection(shared, controlSize);
	}

	private static PackedCollection<?> createDestination(String location) {
		String shared = location + "_shm";
		return createCollection(shared, BufferDefaults.defaultBufferSize);
	}

	private static PackedCollection<?> createCollection(String file, int size) {
		ComputeContext<?> ctx = Hardware.getLocalHardware().getComputeContext();
		return ctx.getDataContext().sharedMemory(len -> file,
				() -> new PackedCollection<>(size));
	}
}
