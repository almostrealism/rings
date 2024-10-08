/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio;

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.SystemUtils;

public interface OutputLine {
	int sampleRate = SystemUtils.getInt("AR_AUDIO_SAMPLE_RATE").orElse(44100);

	/**
	 * Write the specified bytes. Using this method, the caller must
	 * be aware of the number of bytes in a sample to write a valid
	 * set of samples.
	 */
	void write(byte b[]);

	/**
	 * Write the specified frames.
	 */
	void write(double d[][]);

	/**
	 * Write one sample, coercing the {@link PackedCollection} value into whatever
	 * necessary to get one sample worth of bytes.
	 */
	void write(PackedCollection<?> sample);
}
