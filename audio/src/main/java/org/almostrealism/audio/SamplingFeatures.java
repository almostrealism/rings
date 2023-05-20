/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;

import java.util.function.Supplier;

public interface SamplingFeatures extends CodeFeatures {
	ThreadLocal<Integer> sampleRate = new ThreadLocal<>();
	ThreadLocal<Producer<PackedCollection<?>>> time = new ThreadLocal<>();

	default <T> T time(Producer<PackedCollection<?>> t, Supplier<T> r) {
		Producer<PackedCollection<?>> lastT = time.get();

		try {
			time.set(t);
			return r.get();
		} finally {
			time.set(lastT);
		}
	}

	default Producer<PackedCollection<?>> time() { return time.get(); }

	default <T> T sampleRate(int sr, Supplier<T> r) {
		Integer lastSr = sampleRate.get();

		try {
			sampleRate.set(sr);
			return r.get();
		} finally {
			sampleRate.set(lastSr);
		}
	}

	default int sampleRate() { return sampleRate.get() == null ? OutputLine.sampleRate : sampleRate.get(); }

	default <T> T sampling(int rate, double duration, Supplier<T> r) {
		int frames = (int) (rate * duration);
		return sampleRate(rate, () -> time(integers(0, frames).divide(c(sampleRate())), r));
	}

	default int toFrames(double sec) { return (int) (sampleRate() * sec); }

	default Producer<Scalar> toFrames(Supplier<Evaluable<? extends Scalar>> sec) {
		return scalarsMultiply(v(sampleRate()), sec);
	}

	default int toFramesMilli(int msec) { return (int) (sampleRate() * msec / 1000d); }

	default Producer<Scalar> toFramesMilli(Supplier<Evaluable<? extends Scalar>> msec) {
		return scalarsMultiply(v(sampleRate() / 1000d), msec);
	}
}
