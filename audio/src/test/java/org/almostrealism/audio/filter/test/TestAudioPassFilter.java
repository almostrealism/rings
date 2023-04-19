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

package org.almostrealism.audio.filter.test;

import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Provider;
import io.almostrealism.uml.Lifecycle;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.AudioFilterData;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.filter.AudioPassFilterComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.DefaultWaveCellData;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;

public class TestAudioPassFilter implements TemporalFactor<PackedCollection<?>>, Lifecycle, CodeFeatures {
	public static boolean deviceMemory = false;

	private AudioFilterData data;
	private Scalar output;
	private Producer<PackedCollection<?>> input;

	private boolean high;

	public TestAudioPassFilter(int sampleRate) {
		this(sampleRate, deviceMemory ?
						Hardware.getLocalHardware().getClDataContext().deviceMemory(() -> new PolymorphicAudioData()) : new PolymorphicAudioData());
	}

	public TestAudioPassFilter(int sampleRate, AudioFilterData data) {
		this.data = data;
		this.output = new Scalar();
		setSampleRate(sampleRate);
	}

	public int getSampleRate() {
		return (int) data.sampleRate().getValue();
	}
	public void setSampleRate(int sampleRate) {
		data.setSampleRate(sampleRate);
	}

	@Override
	public Producer<PackedCollection<?>> getResultant(Producer<PackedCollection<?>> value) {
		if (input != null && input != value) {
			throw new UnsupportedOperationException("WARN: AudioPassFilter cannot be reused");
		}

		input = value;
		// return () -> new Provider<>(output);
		return data.getOutput();
	}

	@Override
	public Supplier<Runnable> tick() {
//		return new TestAudioPassFilterComputation(data, input, () -> new Provider<>(output));
		return new TestAudioPassFilterComputation(data, input, data::getOutput);
	}

	@Override
	public void reset() {
		this.data.reset();
	}
}
