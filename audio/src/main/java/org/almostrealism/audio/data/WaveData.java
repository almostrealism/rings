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

package org.almostrealism.audio.data;

import io.almostrealism.code.ComputeRequirement;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Factor;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.graph.temporal.WaveCellData;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class WaveData implements SamplingFeatures {
	public static final int FFT_BINS = 1024;
	public static final int FFT_POOL = 8;
	public static final int FFT_POOL_BINS = FFT_BINS / FFT_POOL;

	public static final int POOL_BATCH_IN = FFT_BINS; // TODO  Should be smaller to avoid cutting off the end of the wave
	public static final int POOL_BATCH_OUT = POOL_BATCH_IN / FFT_POOL;

	private static Evaluable<PackedCollection<?>> magnitude;
	private static Evaluable<PackedCollection<?>> fft;
	private static Evaluable<PackedCollection<?>> pool2d;

	static {
		fft = Ops.op(o ->
				o.fft(FFT_BINS, o.v(o.shape(FFT_BINS, 2), 0))).get();
		magnitude = Ops.op(o ->
				o.complexFromParts(
						o.v(o.shape(FFT_BINS), 0),
						o.v(o.shape(FFT_BINS), 1)).magnitude()).get();
		pool2d = Ops.op(o ->
				o.c(o.v(o.shape(POOL_BATCH_IN, POOL_BATCH_IN, 1), 0))
						.enumerate(2, 1)
						.enumerate(2, FFT_POOL)
						.enumerate(2, FFT_POOL)
						.traverse(3)
						.max()
						.reshape(POOL_BATCH_OUT, POOL_BATCH_OUT, 1)).get();
	}

	private PackedCollection collection;
	private int sampleRate;

	public WaveData(PackedCollection wave, int sampleRate) {
		if (wave == null) {
			System.out.println("WARN: Wave data is null");
		} else if (wave.getCount() == 1) {
			warn("Wave data appears to be the wrong shape");
		}

		this.collection = wave;
		this.sampleRate = sampleRate;
	}

	public PackedCollection getCollection() {
		return collection;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double getDuration() {
		return getCollection().getMemLength() / (double) sampleRate;
	}

	public WaveData range(double start, double length) {
		return range((int) (start * sampleRate), (int) (length * sampleRate));
	}

	public WaveData range(int start, int length) {
		return new WaveData(getCollection().range(new TraversalPolicy(length), start), sampleRate);
	}

	public WaveData sample(Factor<PackedCollection<?>> processor) {
		return sample(() -> processor);
	}

	public WaveData sample(Supplier<Factor<PackedCollection<?>>> processor) {
		PackedCollection<?> result = new PackedCollection<>(getCollection().getShape());
		sampling(getSampleRate(), getDuration(),
					() -> processor.get().getResultant(c(p(getCollection()), frame())))
				.get().into(result).evaluate();
		return new WaveData(result, getSampleRate());
	}

	public PackedCollection<?> fft() {
		PackedCollection<?> inRoot = PackedCollection.factory().apply(FFT_BINS * FFT_BINS);
		PackedCollection<?> outRoot = PackedCollection.factory().apply(POOL_BATCH_OUT * POOL_BATCH_OUT);

		int count = getCollection().getMemLength() / FFT_BINS;
		int resultSize = count / FFT_POOL;

		PackedCollection<?> out =  PackedCollection.factory().apply(count * FFT_BINS).reshape(count, FFT_BINS, 1);
		PackedCollection<?> pool =  PackedCollection.factory().apply(resultSize * FFT_POOL_BINS)
				.reshape(resultSize, FFT_POOL_BINS, 1);

		cc(() -> {
			PackedCollection<?> frameIn = inRoot.range(shape(FFT_BINS, 2));
			PackedCollection<?> frameOut = outRoot.range(shape(FFT_BINS, 2));

			for (int i = 0; i < count; i++) {
				frameIn.setMem(0, getCollection(), i * FFT_BINS, FFT_BINS);
				fft.into(frameOut).evaluate(frameIn);
				magnitude
						.into(out.range(shape(FFT_BINS, 1), i * FFT_BINS).traverseEach())
						.evaluate(
								frameOut.range(shape(FFT_BINS), 0).traverseEach(),
								frameOut.range(shape(FFT_BINS), FFT_BINS).traverseEach());
			}
		}, ComputeRequirement.JNI);

		int window = POOL_BATCH_IN * POOL_BATCH_IN;
		int poolWindow = POOL_BATCH_OUT * POOL_BATCH_OUT;
		int pcount = resultSize / POOL_BATCH_OUT;
		if (resultSize % POOL_BATCH_OUT != 0) pcount++;

		PackedCollection<?> poolIn = inRoot.range(shape(POOL_BATCH_IN, POOL_BATCH_IN, 1));
		PackedCollection<?> poolOut = outRoot.range(shape(POOL_BATCH_OUT, POOL_BATCH_OUT, 1));

		// double frequencyTimeScale = WaveData.FFT_BINS / (double) OutputLine.sampleRate;
		// log("FFT pool duration will be " + (pcount * POOL_BATCH_IN) * frequencyTimeScale + " seconds");

		for (int i = 0; i < pcount; i++) {
			int remaining = out.getMemLength() - i * window;
			poolIn.setMem(0, out, i * window, Math.min(window, remaining));

			pool2d.into(poolOut.traverseEach()).evaluate(poolIn.traverseEach());

			remaining = pool.getMemLength() - i * poolWindow;
			pool.setMem(i * poolWindow, poolOut, 0, Math.min(poolWindow, remaining));
		}

		// int outFrames = getCollection().getMemLength() / (FFT_BINS * FFT_POOL);
		// frequencyTimeScale = WaveData.FFT_POOL * WaveData.FFT_BINS / (double) OutputLine.sampleRate;
		// log("FFT result duration will be " + outFrames * frequencyTimeScale + " seconds");

		return pool.range(shape(getCollection().getMemLength() / (FFT_BINS * FFT_POOL), FFT_POOL_BINS, 1));
	}

	public void save(File file) {
		PackedCollection w = getCollection();

		// TODO  This actually *should* be getCount, but it is frequently
		// TODO  wrong because the traversal axis is not set correctly.
		// TODO  To support stereo or other multi-channel audio, we need
		// TODO  to fix this.
		int frames = w.getMemLength(); // w.getCount();

		try (WavFile wav = WavFile.newWavFile(file, 2, frames, 24, sampleRate)) {
			for (int i = 0; i < frames; i++) {
				double value = w.toArray(i, 1)[0];

				try {
					wav.writeFrames(new double[][]{{value}, {value}}, 1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public WaveCell toCell(Producer<Scalar> frame) {
		return new WaveCell(getCollection(), getSampleRate(), frame);
	}

	public Function<WaveCellData, WaveCell> toCell(double amplitude, Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat) {
		return data -> new WaveCell(data, getCollection(), getSampleRate(), amplitude, Ops.o().toScalar(offset),
				Ops.o().toScalar(repeat), Ops.o().v(0.0), Ops.o().v(getCollection().getMemLength()));
	}

	public static WaveData load(File f) throws IOException {
		try (WavFile w = WavFile.openWavFile(f)) {
			double[][] wave = new double[w.getNumChannels()][(int) w.getFramesRemaining()];
			w.readFrames(wave, 0, (int) w.getFramesRemaining());

			int channelCount = w.getNumChannels();

			assert channelCount > 0;
			int channel = 0;

			return new WaveData(WavFile.channel(wave, channel), (int) w.getSampleRate());
		}
	}

	// TODO  This returns a collection with traversalAxis 0, which is usually not desirable
	public static PackedCollection<?> allocateCollection(int count) {
		return new PackedCollection(count);
	}
}
