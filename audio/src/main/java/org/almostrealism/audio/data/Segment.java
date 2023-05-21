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

package org.almostrealism.audio.data;

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.OperationList;

import java.util.function.Supplier;

@Deprecated
public class Segment {
	private String sourceText;
	private PackedCollection<?> source;
	private int pos, len;

	private Supplier<Runnable> setup;

	public Segment(String sourceText, PackedCollection<?> source, int pos, int len) {
		this(sourceText, source, pos, len, new OperationList());
	}

	public Segment(String sourceText, PackedCollection<?> source, int pos, int len, Supplier<Runnable> setup) {
		if (len == -1) {
			pos = 0;
			len = source.getCount();
		}

		if (pos + len > source.getCount()) {
			throw new IllegalArgumentException("Cannot create Segment from " + pos + " to " + (pos + len) + " when source length is " + source.getCount());
		}

		this.sourceText = sourceText;
		this.source = (PackedCollection<?>) source.getRootDelegate();
		this.pos = (source.getOffset() / 2) + pos;
		this.len = len;
		this.setup = setup;
	}

	public PackedCollection<?> getSource() { return source; }
	public int getPosition() { return pos; }
	public int getLength() { return len; }
	public PackedCollection<?> range() { return source.range(new TraversalPolicy(len), pos); }
}
