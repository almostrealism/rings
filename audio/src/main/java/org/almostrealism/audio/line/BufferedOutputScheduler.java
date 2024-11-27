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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.sources.AudioBuffer;
import org.almostrealism.collect.CollectionFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TimingRegularizer;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferedOutputScheduler implements CellFeatures {
	public static int defaultBufferFrames = 2048;
	public static long timingPad = 5;

	private Consumer<Runnable> executor;
	private Temporal process;
	private OutputLine line;
	private AudioBuffer buffer;

	private TimingRegularizer regularizer;
	private Runnable next;
	private boolean stopped;
	private long start, count;

	protected BufferedOutputScheduler(
			Consumer<Runnable> executor, Temporal process,
			OutputLine line, AudioBuffer buffer) {
		this.executor = executor;
		this.process = process;
		this.line = line;
		this.buffer = buffer;
	}

	public void start() {
		if (next != null) {
			throw new UnsupportedOperationException();
		}

		OperationList operations = new OperationList("BufferedOutputScheduler");
		operations.add(process.tick());
		operations.add(line.write(p(buffer.getBuffer())));
		next = operations.get();
		regularizer = new TimingRegularizer((long) (buffer.getDetails().getDuration() * 10e9));

		executor.accept(this::run);
	}

	public void stop() { stopped = true; }

	public long getRealTime() { return System.currentTimeMillis() - start; }
	public long getRenderedTime() { return count * buffer.getDetails().getFrames() * 1000 / line.getSampleRate(); }
	public long getRenderingGap() { return getRenderedTime() - getRealTime(); }

	protected void run() {
		start = System.currentTimeMillis();
		long lastDuration = 0;

		while (!stopped) {
			long s = System.nanoTime();
			regularizer.addMeasuredDuration(lastDuration);
			next.run();
			count++;

			if (count % 100 == 0) {
				System.out.println("BufferedOutputScheduler: rendering gap = " + getRenderingGap());
			}

			lastDuration = System.nanoTime() - s;

			try {
				long target = (regularizer.getTimingDifference() / (long) 10e6) - timingPad;
				long gap = Math.max(0, getRenderingGap());
				Thread.sleep(target + gap / 2);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line,
										  Function<Producer<PackedCollection<?>>, Temporal> source) {
		return create(executor, line, defaultBufferFrames, source);
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line, int frames,
										  Function<Producer<PackedCollection<?>>, Temporal> source) {
		return create(executor, line, AudioBuffer.create(line.getSampleRate(), frames), source);
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line, AudioBuffer buffer,
										  Function<Producer<PackedCollection<?>>, Temporal> source) {
		return new BufferedOutputScheduler(executor::execute,
				source.apply(CollectionFeatures.getInstance().p(buffer.getBuffer())),
				line, buffer);
	}
}
