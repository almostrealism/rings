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

import io.almostrealism.relation.Delegated;
import org.almostrealism.collect.PackedCollection;

public class DelegatedOutputLine implements OutputLine, Delegated<OutputLine> {
	private OutputLine delegate;
	private int bufferSize;

	public DelegatedOutputLine() { this(null, BufferDefaults.defaultBufferSize); }

	public DelegatedOutputLine(OutputLine delegate, int bufferSize) {
		this.delegate = delegate;
		this.bufferSize = bufferSize;
	}

	@Override
	public OutputLine getDelegate() { return delegate; }

	public void setDelegate(OutputLine delegate) {
		this.delegate = delegate;
	}

	@Override
	public int getReadPosition() {
		return delegate == null ? 0 : delegate.getReadPosition();
	}

	@Override
	public int getBufferSize() {
		if (delegate != null && bufferSize != delegate.getBufferSize()) {
			throw new UnsupportedOperationException();
		}

		return bufferSize;
	}

	@Override
	public void write(PackedCollection<?> sample) {
		if (delegate != null) {
			delegate.write(sample);
		}
	}
}
