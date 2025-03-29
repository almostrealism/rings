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

package org.almostrealism.audio.line;

import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.SystemUtils;

import java.util.function.Supplier;

public interface OutputLine extends BufferedAudio, Destroyable {

	/**
	 * The position in the buffer of the last frame that was sent to the device.
	 */
	default int getReadPosition() {
		return 0;
	}

	/**
	 * Write the specified bytes. Using this method, the caller must
	 * be aware of the number of bytes in a sample to write a valid
	 * set of samples.
	 */
	@Deprecated
	default void write(byte b[]) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Write the specified frames.
	 */
	@Deprecated
	default void write(double d[][]) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Write all the samples from the specified {@link PackedCollection},
	 * which cannot be larger than the buffer size.
	 *
	 * @see  BufferedAudio#getBufferSize()
	 */
	void write(PackedCollection<?> sample);

	default Supplier<Runnable> write(Producer<PackedCollection<?>> frames) {
		return () -> {
			Evaluable<PackedCollection<?>> sample = frames.get();
			return () -> write(sample.evaluate());
		};
	}
}
