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

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.ByteFunction;
import org.almostrealism.graph.DataReceiver;
import io.almostrealism.relation.Evaluable;

import javax.sound.sampled.AudioFormat;

public class FilterOutputLine implements OutputLine, Evaluable<byte[]>, DataReceiver {
	private ByteFunction<byte[]> filter;
	private OutputLine line;
	
	private byte next[];
	
	public FilterOutputLine(ByteFunction<byte[]> filter, OutputLine line) {
		this.filter = filter;
		this.line = line; 
	}

	@Override
	public void write(byte[] b) {
		this.line.write(this.filter.operate(b));
	}

	@Override
	public void write(double[][] d) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Converts the specified sample to a frame using
	 * {@link LineUtilities#toFrame(PackedCollection, AudioFormat)}
	 * and writes those bytes using {@link #write(byte[])}.
	 *
	 * TODO  We do not have the audio format, maybe we
	 *       can use just the frame size. This method
	 *       currently throws an exception.
	 */
	@Override
	public void write(PackedCollection<?> sample) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void next(byte b[]) {
		this.next = b;
	}

	@Override
	public byte[] evaluate(Object[] args) {
		return this.filter.operate(this.next);
	}
}
