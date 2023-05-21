/*
 * Copyright 2020 Michael Murray
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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.almostrealism.code.ComputeRequirement;
import io.almostrealism.relation.Provider;
import io.almostrealism.uml.Lifecycle;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.computations.PairFromPairBank;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.graph.Receptor;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.almostrealism.time.CursorPair;
import org.almostrealism.CodeFeatures;

public class WaveOutput implements Receptor<PackedCollection<?>>, Lifecycle, CodeFeatures {
	public static boolean enableVerbose = false;
	public static boolean enableKernelExport = true;

	public static int defaultTimelineFrames = (int) (OutputLine.sampleRate * 180);

	public static ContextSpecific<PackedCollection<PackedCollection<?>>> timeline;
	public static ContextSpecific<ScalarBank> timelineScalar;
	private static KernelizedEvaluable<PackedCollection<?>> exportKernel;
	private static Provider<ScalarBank> exportSource;

	static {
		timelineScalar = new DefaultContextSpecific<>(
				() -> {
					ScalarBank data = new ScalarBank(defaultTimelineFrames);
					double values[] = IntStream.range(0, defaultTimelineFrames)
							.mapToObj(i -> i / (double) OutputLine.sampleRate)
							.flatMap(v -> Stream.of(v, 1.0))
							.mapToDouble(Double::doubleValue).toArray();
					// for (int i = 0; i < values.size(); i++) data.set(i, values.get(i));
					data.setMem(values);
					return data;
				}, ScalarBank::destroy);
		timelineScalar.init();

		timeline = new DefaultContextSpecific<>(
				() -> {
					PackedCollection data = new PackedCollection<>(defaultTimelineFrames).traverseEach();
					List<Double> values = IntStream.range(0, defaultTimelineFrames)
							.mapToObj(i -> i / (double) OutputLine.sampleRate).collect(Collectors.toList());
					for (int i = 0; i < values.size(); i++) data.set(i, values.get(i));
					return data;
				}, PackedCollection::destroy);
		timeline.init();

		PairFromPairBank pairAt = new PairFromPairBank((Producer) Ops.ops().v(Ops.ops().shape(2), 0),
				Ops.ops().v(OutputLine.sampleRate).multiply(Ops.ops().v(Ops.ops().shape(2), 1)).add(Ops.ops().v(1.0)));
//		PairFromPairBank pairAt = new PairFromPairBank((Producer) () -> exportSource,
//				Ops.ops().v(OutputLine.sampleRate).multiply(Ops.ops().v(Scalar.class, 1)).add(Ops.ops().v(1.0)));
		ExpressionComputation r = new ExpressionComputation<>(List.of(args -> args.get(1).getValue(0)), (Producer) pairAt.r());
		exportKernel = r.get();
	}

	private Supplier<File> file;
	private int bits;
	private long sampleRate;

	private WavFile wav;
	private CursorPair cursor;
	private AcceleratedTimeSeries data;
	private Runnable reset;

	public WaveOutput() { this(null); }

	public WaveOutput(File f) {
		this(f, 24);
	}

	public WaveOutput(File f, int bits) {
		this(() -> f, 240 * OutputLine.sampleRate, bits);
	}

	public WaveOutput(Supplier<File> f, int bits) {
		this(f, -1, bits);
	}

	public WaveOutput(Supplier<File> f, int maxFrames, int bits) {
		this(f, maxFrames, bits, OutputLine.sampleRate);
	}

	public WaveOutput(Supplier<File> f, int maxFrames, int bits, long sampleRate) {
		this.file = f;
		this.bits = bits;
		this.sampleRate = sampleRate;
		this.cursor = new CursorPair();
		this.data = maxFrames <= 0 ? new AcceleratedTimeSeries() : new AcceleratedTimeSeries(maxFrames);

		this.reset = a(2, p(cursor), pair(0.0, 1.0)).get();
	}

	public CursorPair getCursor() { return cursor; }

	public AcceleratedTimeSeries getData() { return data; }

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		OperationList push = new OperationList("WaveOutput Push");
		push.add(data.add(temporal(l(p(cursor)), (Producer) protein)));

		// push.add(new MetricComputation<>("Incrementing cursor", 100000, p(cursor), 0, 2));
		push.add(cursor.increment(v(1.0)));
		// push.add(new MetricComputation<>("Incremented cursor", 100000, p(cursor), 0, 2));

		return push;
	}

	public Supplier<Runnable> export(PackedCollection<?> destination) {
		if (enableKernelExport) {
			Runnable export = () -> {
				long start = System.currentTimeMillis();

				exportSource = new Provider(data);
				exportKernel.into(destination).evaluate(data, timelineScalar.getValue().range(0, destination.getMemLength()));
				if (enableVerbose)
					System.out.println("WaveOutput: Wrote " + destination.getCount() + " frames in " + (System.currentTimeMillis() - start) + " msec");
			};

			return () -> () -> {
				if (Hardware.getLocalHardware().getComputeContext().isKernelSupported()) {
					export.run();
				} else {
					System.out.println("WaveOutput: Kernels not supported by " + Hardware.getLocalHardware().getComputeContext() + " - enabling new context");
					cc(export, ComputeRequirement.CL);
				}
			};
		} else {
			return () -> () -> {
				int frames = (int) cursor.left() - 1;

				if (frames > 0) {
					// System.out.println("Writing " + frames + " frames");
				} else {
					System.out.println("WaveOutput: No frames to write");
					return;
				}

				long start = System.currentTimeMillis();

				for (int i = 0; i < frames; i++) {
					// double value = data.valueAt(i).getValue();
					double value = data.get(i + 1).getValue();
					destination.set(i, value);
				}

				if (enableVerbose)
					System.out.println("WaveOutput: Wrote " + frames + " frames in " + (System.currentTimeMillis() - start) + " msec");
			};
		}
	}

	public Supplier<Runnable> write() {
		// TODO  Write frames in larger batches than 1
		return () -> () -> {
			int frames = (int) cursor.left() - 1;

			if (frames > 0) {
				// System.out.println("Writing " + frames + " frames");
			} else {
				System.out.println("WaveOutput: No frames to write");
				return;
			}

			long start = System.currentTimeMillis();

			try {
				this.wav = WavFile.newWavFile(file.get(), 2, frames, bits, sampleRate);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			double frameData[] = data.toArray(data.getAtomicMemLength(), data.getAtomicMemLength() * frames);

			for (int i = 0; i < frames; i++) {
				// double value = data.valueAt(i).getValue();
				// double value = data.get(i + 1).getValue();
				double value = frameData[2 * i + 1];

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

			if (enableVerbose) System.out.println("WaveOutput: Wrote " + frames + " frames in " + (System.currentTimeMillis() - start) + " msec");
		};
	}

	public Supplier<Runnable> writeCsv(File file) {
		return () -> () -> {
			StringBuffer buf = new StringBuffer();

			int frames = (int) cursor.left();

			for (int i = 0; i < frames; i++) {
				double value = data.get(i).getValue();
				buf.append(i + "," + value + "\n");
			}

			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
				out.println(buf);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		};
	}

	@Override
	public void reset() {
		Lifecycle.super.reset();
		cursor.setCursor(0);
		cursor.setDelayCursor(1);
//		reset.run();
		data.reset();
	}
}
