/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.function.Supplier;

import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.lifecycle.Lifecycle;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.Ops;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.CollectionFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.almostrealism.io.Console;
import org.almostrealism.CodeFeatures;

public class WaveOutput implements Receptor<PackedCollection<?>>, Lifecycle, Destroyable, CodeFeatures {
	public static boolean enableVerbose = false;

	public static int defaultTimelineFrames = (int) (OutputLine.sampleRate * 230);

	public static ContextSpecific<PackedCollection<PackedCollection<?>>> timeline;

	static {
		Supplier<PackedCollection<PackedCollection<?>>> timelineSupply = () -> {
			if (enableVerbose) {
				CellFeatures.console.features(WaveOutput.class).log("Generating timeline");
			}

			PackedCollection data = new PackedCollection<>(defaultTimelineFrames).traverseEach();
			Ops.o().integers(0, defaultTimelineFrames).divide(Ops.o().c(OutputLine.sampleRate)).into(data).evaluate();

			if (enableVerbose) {
				CellFeatures.console.features(WaveOutput.class).log("Finished generating timeline");
			}

			return data;
		};

		timeline = new DefaultContextSpecific<>(timelineSupply, PackedCollection::destroy);
		timeline.init();
	}

	private Supplier<File> file;
	private int bits;
	private long sampleRate;

	private WavFile wav;
	private PackedCollection<?> cursor;
	private Producer<PackedCollection<?>> data;

	public WaveOutput() { this((File) null); }

	public WaveOutput(int maxFrames) {
		this(null, 24, maxFrames);
	}

	public WaveOutput(File f) {
		this(f, 24);
	}

	public WaveOutput(File f, int bits) {
		this(() -> f, bits);
	}

	public WaveOutput(Supplier<File> f, int bits) {
		this(f, bits, -1);
	}

	public WaveOutput(Supplier<File> f, int bits, int maxFrames) {
		this(f, bits, OutputLine.sampleRate, maxFrames);
	}

	public WaveOutput(Supplier<File> f, int bits, long sampleRate, int maxFrames) {
		this(f, bits, sampleRate, new PackedCollection<>(maxFrames <= 0 ? defaultTimelineFrames : maxFrames));
	}

	public WaveOutput(PackedCollection<?> data) {
		this(null, 24, OutputLine.sampleRate, data);
	}

	public WaveOutput(Producer<PackedCollection<?>> data) {
		this(null, 24, OutputLine.sampleRate, data);
	}

	public WaveOutput(Supplier<File> f, int bits, long sampleRate, PackedCollection<?> data) {
		this(f, bits, sampleRate, CollectionFeatures.getInstance().p(data));
	}

	public WaveOutput(Supplier<File> f, int bits, long sampleRate, Producer<PackedCollection<?>> data) {
		this.file = f;
		this.bits = bits;
		this.sampleRate = sampleRate;
		this.cursor = new PackedCollection<>(1);
		this.data = c(data).traverseEach();
	}

	public PackedCollection<?> getCursor() { return cursor; }

	public PackedCollection<?> getData() { return data.get().evaluate(); }

	public WaveData getWaveData() {
		return new WaveData(getData()
				.range(shape((int) getCursor().toDouble(0))).traverseEach(),
				OutputLine.sampleRate);
	}

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		String description = "WaveOutput Push";
		if (file != null) description += " (to file)";
		OperationList push = new OperationList(description);

		if (shape(protein).getSize() == 2) {
			protein = c(protein, 0);
		}

		Producer slot = c(shape(1), data, p(cursor));

		push.add(a("WaveOutput Insert", slot, (Producer) protein));
		push.add(a("WaveOutput Cursor Increment", cp(cursor), cp(cursor).add(1)));
		return push;
	}

	public Supplier<Runnable> export(PackedCollection<?> destination) {
		TraversalPolicy shape = shape(data);
		int len = destination.getMemLength();
		if (shape.getTotalSize() > 1 && shape.getTotalSize() > len)
			len = shape.getTotalSize();

		Evaluable<PackedCollection<?>> d = this.data.get();
		return new MemoryDataCopy("WaveOutput Export", d::evaluate, () -> destination, len);
	}

	public Supplier<Runnable> write() {
		// TODO  Write frames in larger batches than 1
		return () -> {
			Evaluable<PackedCollection<?>> d = data.get();

			return () -> {
				PackedCollection<?> o = d.evaluate();
				int frames = (int) cursor.toDouble(0) - 1;

				if (frames > 0) {
					// log("Writing " + frames + " frames");
				} else {
					log("No frames to write");
					return;
				}

				long start = System.currentTimeMillis();

				try {
					this.wav = WavFile.newWavFile(file.get(), 2, frames, bits, sampleRate);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				double frameData[] = o.toArray(0, frames);

				for (int i = 0; i < frames; i++) {
					double value = frameData[i];

					try {
						wav.writeFrames(new double[][]{{value}, {value}}, 1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					wav.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				if (enableVerbose)
					log(" Wrote " + frames + " frames in " + (System.currentTimeMillis() - start) + " msec");
			};
		};
	}

	public Supplier<Runnable> writeCsv(File file) {
		return () -> {
			Evaluable<PackedCollection<?>> d = data.get();

			return () -> {
				PackedCollection<?> o = d.evaluate();
				StringBuffer buf = new StringBuffer();

				int frames = (int) cursor.toDouble(0);

				for (int i = 0; i < frames; i++) {
					double value = o.toDouble(i);
					buf.append(i + "," + value + "\n");
				}

				try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
					out.println(buf);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			};
		};
	}

	@Override
	public void reset() {
		Lifecycle.super.reset();
		cursor.setMem(0.0);

		// TODO  The data should be cleared, but this can cause problems for
		// TODO  systems that are resetting the WaveOutput prior to writing
		// collection.clear();
	}

	@Override
	public void destroy() {
		if (cursor != null) cursor.destroy();
		if (data != null) data.destroy();
	}

	@Override
	public Console console() { return CellFeatures.console; }
}
