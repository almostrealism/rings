/*
 * Copyright 2022 Michael Murray
 *
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

import io.almostrealism.lifecycle.Lifecycle;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairFeatures;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarFeatures;
import org.almostrealism.audio.computations.ClipCounter;
import org.almostrealism.audio.computations.SilenceDurationComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Provider;
import org.almostrealism.hardware.OperationList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AudioMeter implements Receptor<PackedCollection<?>>, Lifecycle, ScalarFeatures, PairFeatures {
	private Receptor<PackedCollection<?>> forwarding;
	
	private Scalar clipCount = new Scalar();
	private Pair clipSettings = new Pair(-1.0, 1.0);
	
	private Scalar silenceValue = new Scalar();
	private Scalar silenceDuration = new Scalar();

	private List<Consumer<Scalar>> listeners;
	
	public AudioMeter() {
		listeners = new ArrayList<>();
	}

	public void setForwarding(Receptor<PackedCollection<?>> r) { this.forwarding = r; }
	public Receptor<PackedCollection<?>> getForwarding() { return this.forwarding; }

	public void setSilenceValue(double value) { this.silenceValue.setA(value); }
	
	public long getSilenceDuration() { return (long) this.silenceDuration.getValue(); }

	public void setClipMinValue(double value) { this.clipSettings.setA(value); }
	public void setClipMaxValue(double value) { this.clipSettings.setB(value); }
	
	public long getClipCount() { return (long) clipCount.getValue(); }

	public void addListener(Consumer<Scalar> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<Scalar> listener) {
		listeners.remove(listener);
	}

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		OperationList push = new OperationList("AudioMeter Push");
		push.add(new ClipCounter(() -> new Provider<>(clipCount), v(clipSettings), protein));
		push.add(new SilenceDurationComputation(() -> new Provider<>(silenceDuration), v(silenceValue), protein));
		if (forwarding != null)  push.add(forwarding.push(protein));

		if (!listeners.isEmpty()) {
			// push.add(() -> () -> listeners.forEach(listener -> listener.accept(p)));
			throw new UnsupportedOperationException();
		}

		return push;
	}

	@Override
	public void reset() {
		Lifecycle.super.reset();
		silenceDuration.setValue(0.0);
		silenceValue.setValue(0.0);
		clipCount.setValue(0.0);
	}
}
