/*
 * Copyright 2022 Michael Murray
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarBankHeap;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.PackedCollectionHeap;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.heredity.Factor;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WaveData implements SamplingFeatures {
	private static ContextSpecific<PackedCollectionHeap> collectionHeap;

	private PackedCollection collection;
	private int sampleRate;

	public WaveData(PackedCollection wave, int sampleRate) {
		if (wave == null) {
			System.out.println("WARN: Wave data is null");
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
		PackedCollection<?> result = new PackedCollection<>(getCollection().getShape());
		sampling(getSampleRate(), getDuration(), () -> processor.getResultant(p(getCollection())))
				.get().into(result).evaluate();
		return new WaveData(result, getSampleRate());
	}

	public void save(File file) {
		PackedCollection w = getCollection();

		// TODO  This actually *should* be getCount, but it is frequently
		// TODO  wrong because the traversal axis is not set correctly.
		// TODO  To support stereo or other multi-channel audio, we need
		// TODO  to fix this.
		int frames = w.getMemLength(); // w.getCount();

		WavFile wav;

		try {
			wav = WavFile.newWavFile(file, 2, frames, 24, sampleRate);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < frames; i++) {
			double value = w.toArray(i, 1)[0];

			try {
				wav.writeFrames(new double[][]{{value}, {value}}, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			wav.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static WaveData load(File f) throws IOException {
		WavFile w = WavFile.openWavFile(f);

		double[][] wave = new double[w.getNumChannels()][(int) w.getFramesRemaining()];
		w.readFrames(wave, 0, (int) w.getFramesRemaining());

		int channelCount = w.getNumChannels();

		assert channelCount > 0;
		int channel = 0;

		return new WaveData(WavFile.channel(wave, channel), (int) w.getSampleRate());
	}

	public static PackedCollectionHeap getCollectionHeap() { return collectionHeap == null ? null : collectionHeap.getValue(); }

	public static void setCollectionHeap(Supplier<PackedCollectionHeap> create, Consumer<PackedCollectionHeap> destroy) {
		collectionHeap = new DefaultContextSpecific<>(create, destroy);
		collectionHeap.init();
	}

	public static void dropHeap() {
		collectionHeap = null;
	}

	public static PackedCollection<?> allocateCollection(int count) {
		return Optional.ofNullable(getCollectionHeap()).map(h -> h.allocate(count)).orElse(new PackedCollection(count));
	}
}
