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

import org.almostrealism.audio.OutputLine;
import org.almostrealism.collect.PackedCollection;

public class SharedMemoryOutputLine implements OutputLine {
	private int cursor;
	private PackedCollection<?> destination;

	public SharedMemoryOutputLine(PackedCollection<?> destination) {
		this.destination = destination;
	}

	@Override
	public void write(byte[] b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(double[][] d) {
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
}
