/*
 * Copyright 2022 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.line;

import org.almostrealism.collect.PackedCollection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class SourceDataOutputLine implements OutputLine {
	private SourceDataLine line;
	
	public SourceDataOutputLine(SourceDataLine line) {
		this.line = line;
	}

	public SourceDataLine getDataLine() {
		return line;
	}

	@Override
	public void write(byte[] b) { line.write(b, 0, b.length); }

	@Override
	public void write(double[][] d) {
		write(LineUtilities.toBytes(d, line.getFormat()));
	}

	/**
	 * Converts the specified sample to a frame using
	 * {@link LineUtilities#toFrame(PackedCollection, AudioFormat)}
	 * and writes those bytes using {@link #write(byte[])}.
	 */
	@Override
	public void write(PackedCollection<?> sample) {
		write(LineUtilities.toFrame(sample, line.getFormat()));
	}
}
