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

import io.almostrealism.relation.Provider;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.BaseAudioData;

public interface AudioFilterData extends BaseAudioData {
	Scalar get(int index);

	default Scalar frequency() { return get(0); }
	default Scalar resonance() { return get(1); }
	default Scalar sampleRate() { return get(2); }
	default Scalar c() { return get(3); }
	default Scalar a1() { return get(4); }
	default Scalar a2() { return get(5); }
	default Scalar a3() { return get(6); }
	default Scalar b1() { return get(7); }
	default Scalar b2() { return get(8); }
	default PackedCollection<?> output() { return get(9); }
	default Scalar inputHistory0() { return get(10); }
	default Scalar inputHistory1() { return get(11); }
	default Scalar outputHistory0() { return get(12); }
	default Scalar outputHistory1() { return get(13); }
	default Scalar outputHistory2() { return get(14); }

	default Provider<Scalar> getFrequency() { return new Provider<>(frequency()); }
	default void setFrequency(double frequency) { frequency().setValue(frequency); }

	default Provider<Scalar> getResonance() { return new Provider<>(resonance()); }
	default void setResonance(double resonance) { resonance().setValue(resonance); }

	default Provider<Scalar> getSampleRate() { return new Provider<>(sampleRate()); }
	default void setSampleRate(double sampleRate) { sampleRate().setValue(sampleRate); }

	default Provider<Scalar> getC() { return new Provider<>(c()); }
	default Provider<Scalar> getA1() { return new Provider<>(a1()); }
	default Provider<Scalar> getA2() { return new Provider<>(a2()); }
	default Provider<Scalar> getA3() { return new Provider<>(a3()); }
	default Provider<Scalar> getB1() { return new Provider<>(b1()); }
	default Provider<Scalar> getB2() { return new Provider<>(b2()); }
	default Provider<PackedCollection<?>> getOutput() { return new Provider<>(output()); }
	default Provider<Scalar> getInputHistory0() { return new Provider<>(inputHistory0()); }
	default Provider<Scalar> getInputHistory1() { return new Provider<>(inputHistory1()); }
	default Provider<Scalar> getOutputHistory0() { return new Provider<>(outputHistory0()); }
	default Provider<Scalar> getOutputHistory1() { return new Provider<>(outputHistory1()); }
	default Provider<Scalar> getOutputHistory2() { return new Provider<>(outputHistory2()); }

	default void reset() {
		c().setValue(0.0);
		a1().setValue(0.0);
		a2().setValue(0.0);
		a3().setValue(0.0);
		b1().setValue(0.0);
		b2().setValue(0.0);
		output().setMem(0, 0.0);
		inputHistory0().setValue(0.0);
		inputHistory1().setValue(0.0);
		outputHistory0().setValue(0.0);
		outputHistory1().setValue(0.0);
		outputHistory2().setValue(0.0);
	}
}
