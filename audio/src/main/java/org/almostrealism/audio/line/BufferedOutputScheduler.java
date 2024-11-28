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
	public static final int batches = 2;
	public static final long timingPad = -3;

	public static int defaultBufferFrames = 8 * 1024;
	public static double defaultBufferingRate = 2.0;

	private Consumer<Runnable> executor;
	private Temporal process;
	private OutputLine line;
	private AudioBuffer buffer;

	private TimingRegularizer regularizer;
	private Runnable next;

	private long start;
	private boolean stopped;
	private long count;

	private double rate;
	private int batchSize;
	private long batchStart;

	private boolean paused;
	private long lastPause, totalPaused;

	protected BufferedOutputScheduler(
			Consumer<Runnable> executor, Temporal process,
			OutputLine line, AudioBuffer buffer) {
		this.executor = executor;
		this.process = process;
		this.line = line;
		this.buffer = buffer;
		this.rate = defaultBufferingRate;
		this.batchSize = line.getBufferSize() / batches;
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

	public void pause() {
		if (paused) {
			throw new UnsupportedOperationException();
		}

		synchronized (this) {
			double avg = regularizer.getAverageDuration() / 10e9;
			double tot = (System.currentTimeMillis() - batchStart) / 1000.0;
			double dur = batchSize / (double) line.getSampleRate();
			log("Pausing at " + count + " - " + tot + "(x" + dur / tot + ")");
			lastPause = System.currentTimeMillis();
			paused = true;
			notifyAll();
		}
	}

	public void resume() throws InterruptedException {
		if (!paused) {
			wait();
		}

		if (lastPause > 0)
			totalPaused = totalPaused + (System.currentTimeMillis() - lastPause);

		log("Resumed at " + getRenderedCount() +
				" | rendering gap = " + getRenderingGap() + ")");

		lastPause = 0;
		batchStart = System.currentTimeMillis();
		paused = false;
	}

	public void stop() { stopped = true; }

	public AudioBuffer getBuffer() { return buffer; }
	public OutputLine getOutputLine() { return line; }

	protected long toRealTime(double t) { return (long) (t * rate); }
	protected long fromRealTime(double t) { return (long) (t / rate); }

	public long getRenderedCount() { return count; }
	public long getRenderedFrames() { return count * buffer.getDetails().getFrames(); }
	public long getRealTime() { return toRealTime(System.currentTimeMillis() - start - totalPaused); }
	public long getRenderedTime() { return getRenderedFrames() * 1000 / line.getSampleRate(); }
	public long getRenderingGap() { return getRenderedTime() - getRealTime(); }

	protected long getTarget() {
		long target = fromRealTime(regularizer.getTimingDifference() / 10e6);

		if (paused) {
			target = target / 4;
		} else {
			long gap = Math.max(0, getRenderingGap()) / 2;
			target = target + gap + timingPad;
		}

		return target;
	}

	protected void run() {
		start = System.currentTimeMillis();
		long lastDuration = 0;

		while (!stopped) {
			long target;

			if (!paused) {
				long s = System.nanoTime();
				regularizer.addMeasuredDuration(lastDuration);
				next.run();
				count++;

				if (getRenderedFrames() % batchSize == 0) {
					pause();
				}

				target = getTarget();
				lastDuration = System.nanoTime() - s;
			} else {
				target = getTarget();
			}

			try {
				Thread.sleep(target);
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
