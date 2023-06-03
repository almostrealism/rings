/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.filter;

import io.almostrealism.relation.Producer;
import io.almostrealism.uml.Lifecycle;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.AudioFilterData;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;

public class AudioPassFilter implements TemporalFactor<PackedCollection<?>>, Lifecycle, CodeFeatures {
	public static final double MIN_FREQUENCY = 10.0;

	private AudioFilterData data;
	private Producer<PackedCollection<?>> frequency;
	private Producer<Scalar> resonance;
	private Producer<PackedCollection<?>> input;

	private boolean high;

	public AudioPassFilter(int sampleRate, Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance, boolean high) {
		this(sampleRate, new PolymorphicAudioData(), frequency, resonance, high);
	}

	public AudioPassFilter(int sampleRate, AudioFilterData data, Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance, boolean high) {
		this.data = data;
		this.frequency = _bound(frequency, MIN_FREQUENCY, 20000);
		this.resonance = resonance;
		this.high = high;
		setSampleRate(sampleRate);
	}

	public Producer<PackedCollection<?>> getFrequency() { return frequency; }
	public void setFrequency(Producer<PackedCollection<?>> frequency) {
		this.frequency = frequency;
	}

	public Producer<Scalar> getResonance() {
		return resonance;
	}
	public void setResonance(Producer<Scalar> resonance) {
		this.resonance = resonance;
	}

	public int getSampleRate() {
		return (int) data.sampleRate().getValue();
	}
	public void setSampleRate(int sampleRate) {
		data.setSampleRate(sampleRate);
	}

	public boolean isHigh() {
		return high;
	}

	@Override
	public Producer<PackedCollection<?>> getResultant(Producer<PackedCollection<?>> value) {
		if (input != null && input != value) {
			throw new UnsupportedOperationException("WARN: AudioPassFilter cannot be reused");
		}

		input = value;
		return data.getOutput();
	}

	@Override
	public Supplier<Runnable> tick() {
		return new AudioPassFilterComputation(data, frequency, resonance, input, high);
	}

	@Override
	public void reset() {
		this.data.reset();
	}
}