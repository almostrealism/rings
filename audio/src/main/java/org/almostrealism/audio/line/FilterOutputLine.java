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
import org.almostrealism.graph.ByteFunction;
import org.almostrealism.graph.DataReceiver;
import io.almostrealism.relation.Evaluable;

import javax.sound.sampled.AudioFormat;

/**
 * @deprecated This class uses the old byte[] approach and does not support
 * Producer-based buffered writes with BufferedOutputScheduler. Do not use.
 */
@Deprecated
public class FilterOutputLine implements Evaluable<byte[]>, DataReceiver {
	private ByteFunction<byte[]> filter;
	private OutputLine line;
	
	private byte next[];
	
	public FilterOutputLine(ByteFunction<byte[]> filter, OutputLine line) {
		this.filter = filter;
		this.line = line;
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
