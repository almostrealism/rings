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

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Source;
import io.almostrealism.relation.Producer;
import org.almostrealism.time.Temporal;
import org.almostrealism.hardware.OperationList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link SourceToLineWriter} is responsible for writing the data
 * from a {@link Source} to an {@link OutputLine}. It is a
 * {@link Temporal} implementor, so the write from the {@link Source}
 * to the {@link OutputLine} occurs whenever the {@link #tick()}
 * method is called.
 *
 * @deprecated {@link BufferedOutputScheduler} should be used instead.
 *
 * @author  Michael Murray
 */
@Deprecated
public class SourceToLineWriter implements Temporal {
	private Source<PackedCollection> source;
	private OutputLine line;
	private List<Temporal> dependencies;

	public SourceToLineWriter(Source<PackedCollection> source, OutputLine line) {
		this.source = source;
		this.line = line;
		this.dependencies = new ArrayList<>();
	}

	public void addDependency(Temporal dependent) {
		dependencies.add(dependent);
	}

	/**
	 * Returns true if the line is ready to accept more data,
	 * false otherwise.
	 */
	public boolean isReady() {
		return true;  // TODO  Delegate to the OutputLine
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("SourceToLineWriter Tick");

		dependencies.stream().map(Temporal::tick).forEach(tick::add);

		Producer<PackedCollection> next = source.next();

		tick.add(() -> () -> {
			PackedCollection n = next.get().evaluate();
			if (n == null) {
				throw new RuntimeException("No next value from source");
			}

			line.write(n);
		});

		return () -> () -> {
			if (!isReady()) return;
			tick.get().run();
		};
	}
}
