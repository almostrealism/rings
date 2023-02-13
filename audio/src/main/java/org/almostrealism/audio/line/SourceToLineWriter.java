package org.almostrealism.audio.line;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
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
 * @author  Michael Murray
 */
public class SourceToLineWriter implements Temporal {
	private Source<PackedCollection<?>> source;
	private OutputLine line;
	private List<Temporal> dependencies;

	public SourceToLineWriter(Source<PackedCollection<?>> source, OutputLine line) {
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

		Producer<PackedCollection<?>> next = source.next();

		tick.add(() -> () -> {
			PackedCollection<?> n = next.get().evaluate();
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
