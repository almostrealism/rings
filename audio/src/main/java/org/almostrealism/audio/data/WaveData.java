/*
 * Copyright 2025 Michael Murray
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

import io.almostrealism.compute.ComputeRequirement;
import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.feature.FeatureComputer;
import org.almostrealism.audio.feature.FeatureSettings;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Factor;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.graph.temporal.WaveCellData;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class WaveData implements Destroyable, SamplingFeatures {
	public static final boolean enableGpu = false;

	public static final int FFT_BINS = enableGpu ? 256 : 1024;
	public static final int FFT_POOL = 4;
	public static final int FFT_POOL_BINS = FFT_BINS / FFT_POOL / 2;

	public static final int POOL_BATCH_IN = FFT_BINS / 2;
	public static final int POOL_BATCH_OUT = POOL_BATCH_IN / FFT_POOL;

	public static boolean enableMonoLoad = false;

	private static Evaluable<PackedCollection<?>> magnitude;
	private static Evaluable<PackedCollection<?>> fft;
	private static Evaluable<PackedCollection<?>> pool2d;
	private static Evaluable<PackedCollection<?>> scaledAdd;

	private static FeatureComputer mfcc;

	static {
		fft = Ops.op(o ->
				o.fft(FFT_BINS, o.v(o.shape(FFT_BINS, 2), 0),
						enableGpu ? ComputeRequirement.GPU : ComputeRequirement.CPU)).get();
		magnitude = Ops.op(o ->
				o.complexFromParts(
						o.v(o.shape(FFT_BINS / 2), 0),
						o.v(o.shape(FFT_BINS / 2), 1)).magnitude()).get();
		pool2d = Ops.op(o ->
				o.c(o.v(o.shape(POOL_BATCH_IN, POOL_BATCH_IN, 1), 0))
						.enumerate(2, 1)
						.enumerate(2, FFT_POOL)
						.enumerate(2, FFT_POOL)
						.traverse(3)
						.max()
						.reshape(POOL_BATCH_OUT, POOL_BATCH_OUT, 1)).get();
		scaledAdd = Ops.op(o -> o.add(o.v(o.shape(1), 0),
					o.multiply(o.v(o.shape(1), 1), o.v(o.shape(1), 2))))
				.get();
		mfcc = new FeatureComputer(getDefaultFeatureSettings());
	}

	private PackedCollection collection;
	private int sampleRate;

	public WaveData(PackedCollection wave, int sampleRate) {
		if (wave == null) {
			System.out.println("WARN: Wave data is null");
		}

		this.collection = wave;
		this.sampleRate = sampleRate;
	}

	public PackedCollection<?> getData() { return collection; }

	public PackedCollection<?> getCollection() {
		if (getChannelCount() == 1) {
			return getData();
		} else {
			warn("Wave data appears to be multi-channel, returning first channel only");
			return getData().range(shape(getFrameCount()).traverseEach(), 0);
		}
	}

	public int getSampleRate() { return sampleRate; }
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double getDuration() {
		return getFrameCount() / (double) sampleRate;
	}

	public int getChannelCount() {
		if (getData().getShape().getDimensions() == 1) {
			return 1;
		}

		return getData().getShape().length(0);
	}

	public int getFrameCount() {
		if (getData().getShape().getDimensions() == 1) {
			return getData().getMemLength();
		}

		return getData().getShape().length(1);
	}

	public BufferDetails getBufferDetails() {
		return new BufferDetails(getSampleRate(), getDuration());
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

	/**
	 * Return the fourier transform of this {@link WaveData}.
	 *
	 * @return  The fourier transform of this {@link WaveData} over time in the
	 *          {@link TraversalPolicy shape} (time slices, bins, 1).
	 */
	public PackedCollection<?> fft() {
		return fft(false);
	}

	/**
	 * Return the fourier transform of this {@link WaveData}.
	 *
	 * @param pooling  If true, the fourier transform will be pooled to reduce its size
	 *                 by {@value FFT_POOL} in each dimension.
	 * @return  The fourier transform of this {@link WaveData} over time in the
	 *          {@link TraversalPolicy shape} (time slices, bins, 1).
	 */
	public PackedCollection<?> fft(boolean pooling) {
		return fft(pooling, false, false);
	}

	/**
	 * Return the aggregated fourier transform of this {@link WaveData}.
	 *
	 * @param includeAll  If true, all {@value WaveData#FFT_BINS} bins of transform data
	 *                    will be included in the result.  If false, only the first half
	 *                    of the bins will be included.
	 * @return  The aggregated fourier transform of this {@link WaveData} in the
	 *          {@link TraversalPolicy shape} (bins, 1).
	 */
	public PackedCollection<?> aggregatedFft(boolean includeAll) {
		return fft(false, true, includeAll);
	}

	public PackedCollection<?> fft(boolean pooling, boolean sum, boolean includeAll) {
		PackedCollection<?> inRoot = new PackedCollection<>(FFT_BINS * FFT_BINS);
		PackedCollection<?> outRoot = new PackedCollection<>(POOL_BATCH_OUT * POOL_BATCH_OUT);

		int count = getCollection().getMemLength() > FFT_BINS ? getCollection().getMemLength() / FFT_BINS : 1;
		int finalBins = includeAll ? FFT_BINS : (FFT_BINS / 2);
		PackedCollection<?> out =
				new PackedCollection<>(count * finalBins)
				.reshape(count, finalBins, 1);

		try {
			if (enableGpu && count > 1) {
				PackedCollection<?> frameIn = getCollection().range(shape(count, FFT_BINS, 2));
				PackedCollection<?> frameOut = new PackedCollection<>(shape(count, FFT_BINS, 2));

				fft.into(frameOut.traverse()).evaluate(frameIn.traverse());

				for (int i = 0; i < count; i++) {
					int offset = i * FFT_BINS;

					magnitude
							.into(out.range(shape(finalBins, 1), i * finalBins).traverseEach())
							.evaluate(
									frameOut.range(shape(finalBins), offset).traverseEach(),
									frameOut.range(shape(finalBins), offset + finalBins).traverseEach());
				}
			} else {
				PackedCollection<?> frameIn = inRoot.range(shape(FFT_BINS, 2));
				PackedCollection<?> frameOut = outRoot.range(shape(FFT_BINS, 2));

				for (int i = 0; i < count; i++) {
					frameIn.setMem(0, getCollection(), i * FFT_BINS,
							Math.min(FFT_BINS, getCollection().getMemLength() - i * FFT_BINS));
					fft.into(frameOut).evaluate(frameIn);

					// TODO This may not work correctly when finalBins != FFT_BINS / 2
					magnitude
							.into(out.range(shape(finalBins, 1), i * finalBins).traverseEach())
							.evaluate(
									frameOut.range(shape(finalBins), 0).traverseEach(),
									frameOut.range(shape(finalBins), finalBins).traverseEach());
				}
			}

			if (pooling) {
				if (sum) {
					// TODO  This should also be supported
					throw new UnsupportedOperationException();
				}

				int resultSize = count / FFT_POOL;
				if (count % FFT_POOL != 0) resultSize++;

				PackedCollection<?> pool = PackedCollection.factory().apply(resultSize * FFT_POOL_BINS)
						.reshape(resultSize, FFT_POOL_BINS, 1);

				int window = POOL_BATCH_IN * POOL_BATCH_IN;
				int poolWindow = POOL_BATCH_OUT * POOL_BATCH_OUT;
				int pcount = resultSize / POOL_BATCH_OUT;
				if (resultSize % POOL_BATCH_OUT != 0) pcount++;

				PackedCollection<?> poolIn = inRoot.range(shape(POOL_BATCH_IN, POOL_BATCH_IN, 1));
				PackedCollection<?> poolOut = outRoot.range(shape(POOL_BATCH_OUT, POOL_BATCH_OUT, 1));

				if (pcount < 1) {
					throw new IllegalArgumentException();
				}

				for (int i = 0; i < pcount; i++) {
					int remaining = out.getMemLength() - i * window;
					poolIn.setMem(0, out, i * window, Math.min(window, remaining));

					pool2d.into(poolOut.traverseEach()).evaluate(poolIn.traverseEach());

					remaining = pool.getMemLength() - i * poolWindow;
					pool.setMem(i * poolWindow, poolOut, 0, Math.min(poolWindow, remaining));
				}

				return pool.range(shape(resultSize, FFT_POOL_BINS, 1));
			} else if (sum) {
				PackedCollection<?> sumOut = new PackedCollection<>(finalBins, 1);

				for (int i = 0; i < count; i++) {
					scaledAdd.into(sumOut.each()).evaluate(
							sumOut.each(),
							out.range(shape(finalBins, 1), i * finalBins).each(),
							pack(1.0 / count));
				}

				return sumOut;
			} else {
				return out;
			}
		} finally {
			inRoot.destroy();
			outRoot.destroy();
			if (pooling || sum) out.destroy();
		}
	}

	public PackedCollection<?> features() {
		TraversalPolicy inputShape = shape(getCollection().getCount(), 2);
		PackedCollection input = PackedCollection.factory()
				.apply(inputShape.getTotalSize()).reshape(inputShape).traverse(1)
				.fill((int pos[]) -> pos[1] == 0 ? getCollection().toDouble(pos[0]) : 1.0);
		return mfcc.computeFeatures(
				Scalar.scalarBank(input.getCount(), input),
				getSampleRate(), PackedCollection::new);
	}

	public void save(File file) {
		PackedCollection w = getCollection();

		int frames = getFrameCount();

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

	public void saveMultiChannel(File file) {
		PackedCollection w = getData();

		int channelCount = w.getShape().length(0);
		int frames = w.getShape().length(1);

		try (WavFile wav = WavFile.newWavFile(file, channelCount, frames, 24, sampleRate)) {
			double data[][] = new double[channelCount][frames];
			for (int c = 0; c < channelCount; c++) {
				data[c] = w.toArray(c * frames, frames);
			}

			wav.writeFrames(data, frames);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public WaveCell toCell(TimeCell clock) {
		return toCell(clock.frameScalar());
	}

	public WaveCell toCell(Producer<Scalar> frame) {
		return new WaveCell(getCollection(), frame);
	}

	@Override
	public void destroy() {
		Destroyable.super.destroy();
		if (collection != null) collection.destroy();
		collection = null;
	}

	public Function<WaveCellData, WaveCell> toCell(double amplitude, Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat) {
		return data -> new WaveCell(data, getCollection(), getSampleRate(), amplitude, Ops.o().toScalar(offset),
				Ops.o().toScalar(repeat), Ops.o().scalar(0.0), Ops.o().scalar(getCollection().getMemLength()));
	}

	public static void init() { }

	public static WaveData load(File f) throws IOException {
		if (enableMonoLoad) {
			try (WavFile w = WavFile.openWavFile(f)) {
				double[][] wave = new double[w.getNumChannels()][(int) w.getFramesRemaining()];
				w.readFrames(wave, 0, (int) w.getFramesRemaining());
				return new WaveData(WavFile.channel(wave, 0), (int) w.getSampleRate());
			}
		}

		return loadMultiChannel(f);
	}

	public static WaveData loadMultiChannel(File f) throws IOException {
		try (WavFile w = WavFile.openWavFile(f)) {
			int channelCount = w.getNumChannels();
			PackedCollection<?> in = new PackedCollection<>(new TraversalPolicy(channelCount, w.getNumFrames()));

			double[][] wave = new double[w.getNumChannels()][(int) w.getFramesRemaining()];
			w.readFrames(wave, 0, (int) w.getFramesRemaining());

			for (int c = 0; c < wave.length; c++) {
				in.setMem(Math.toIntExact(c * w.getNumFrames()), wave[c]);
			}

			return new WaveData(in, (int) w.getSampleRate());
		}
	}

	public static FeatureSettings getDefaultFeatureSettings() {
		return new FeatureSettings(OutputLine.sampleRate);
	}
}
