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
import io.almostrealism.util.NumberFormats;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.sources.AudioBuffer;
import org.almostrealism.collect.CollectionFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.time.TimingRegularizer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferedOutputScheduler implements CellFeatures {
	public static final long timingPad = -3;

	public static boolean enableVerbose = false;

	private Consumer<Runnable> executor;
	private TemporalRunner process;
	private OutputLine line;
	private AudioBuffer buffer;

	private TimingRegularizer regularizer;
	private Runnable next;

	private long start;
	private boolean stopped;
	private long count;

	private double rate;
	private int groupSize;
	private int lastReadPosition;
	private long groupStart;

	private boolean paused;
	private long lastPause, totalPaused;

	protected BufferedOutputScheduler(
			Consumer<Runnable> executor, TemporalRunner process,
			OutputLine line, AudioBuffer buffer) {
		this.executor = executor;
		this.process = process;
		this.line = line;
		this.buffer = buffer;
		this.rate = BufferDefaults.bufferingRate;
		this.groupSize = line.getBufferSize() / BufferDefaults.groups;
	}

	public void start() {
		if (next != null) {
			throw new UnsupportedOperationException();
		}

		process.setup().get().run();

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
			int lastRead = lastReadPosition;
			lastReadPosition = line.getReadPosition();
			int diff = lastReadPosition - lastRead;
			if (diff < 0) diff = diff + line.getBufferSize();

			double avg = regularizer.getAverageDuration() / 10e9;
			double tot = (System.currentTimeMillis() - groupStart) / 1000.0;
			double dur = groupSize / (double) line.getSampleRate();

			if (enableVerbose || count % 1024 == 0) {
				log("Pausing at " + count + " - " + tot + " (x" +
						NumberFormats.formatNumber(dur / tot) + ") | group " + getLastGroup());
				log("Frames = " + diff + " vs " + groupSize);
				log("Sleep target = " + NumberFormats.formatNumber(getTarget(true) / 1000.0));
			}

			lastPause = System.currentTimeMillis();
			paused = true;
			notifyAll();
		}
	}

	public void resume() throws InterruptedException {
		if (!paused) {
			log("Waiting");
			wait();
		}

		if (lastPause > 0)
			totalPaused = totalPaused + (System.currentTimeMillis() - lastPause);

		lastPause = 0;
		groupStart = System.currentTimeMillis();
		paused = false;
	}

	public int getWritePosition() {
		return (int) ((count * buffer.getDetails().getFrames()) % line.getBufferSize());
	}

	public int getLastGroup() {
		return lastReadPosition / groupSize;
	}

	protected void attemptAutoResume() {
		if (!paused) return;

		boolean safe = BufferDefaults.isSafeGroup(
				getWritePosition(), line.getReadPosition(),
				groupSize, line.getBufferSize());

		if (safe) {
			try {
				resume();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
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
		return getTarget(paused);
	}

	protected long getTarget(boolean paused) {
		long target = fromRealTime(regularizer.getTimingDifference() / 10e6);

		if (paused) {
			target = target / 4;
		} else {
			long gap = Math.max(0, getRenderingGap()) / 2;
			target = target + gap + timingPad;
		}

		return target < 1 ? 1 : target;
	}

	protected void run() {
		start = System.currentTimeMillis();
		long lastDuration = 0;

		while (!stopped) {
			long target;

			attemptAutoResume();

			if (!paused) {
				long s = System.nanoTime();
				regularizer.addMeasuredDuration(lastDuration);
				next.run();
				count++;

				if (getRenderedFrames() % groupSize == 0) {
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

		log("Stopped");
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line,
										  Function<Producer<PackedCollection<?>>, TemporalRunner> source) {
		return create(executor, line, line.getBufferSize() / BufferDefaults.batchCount, source);
	}

	public static BufferedOutputScheduler create(OutputLine line, CellList source) {
		return create(line, line.getBufferSize() / BufferDefaults.batchCount, source::buffer);
	}

	public static BufferedOutputScheduler create(OutputLine line, int frames,
												 CellList source) {
		return create(Executors.newSingleThreadExecutor(), line, frames, source::buffer);
	}

	public static BufferedOutputScheduler create(OutputLine line, int frames,
												 Function<Producer<PackedCollection<?>>, TemporalRunner> source) {
		return create(Executors.newSingleThreadExecutor(), line, frames, source);
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line, int frames,
										  Function<Producer<PackedCollection<?>>, TemporalRunner> source) {
		return create(executor, line, AudioBuffer.create(line.getSampleRate(), frames), source);
	}

	public static BufferedOutputScheduler create(ExecutorService executor, OutputLine line, AudioBuffer buffer,
										  Function<Producer<PackedCollection<?>>, TemporalRunner> source) {
		return new BufferedOutputScheduler(executor::execute,
				source.apply(CollectionFeatures.getInstance().p(buffer.getBuffer())),
				line, buffer);
	}
}
