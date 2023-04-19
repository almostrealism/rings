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

package org.almostrealism.audio.sources;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.filter.Envelope;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.hardware.HardwareFeatures;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;

// TODO  Reimplement as a function of org.almostrealism.graph.TimeCell
public class PolynomialCell extends CollectionTemporalCellAdapter implements CodeFeatures, HardwareFeatures {
	private Envelope env;
	private final PolynomialCellData data;

	public PolynomialCell() {
		this(new PolymorphicAudioData());
	}

	public PolynomialCell(PolynomialCellData data) {
		this.data = data;
		addSetup(a(1, data.getWavePosition(), v(0.0)));
		addSetup(a(1, data.getAmplitude(), v(1.0)));
	}

	public void setEnvelope(Envelope e) { this.env = e; }

	public Supplier<Runnable> setWaveLength(Supplier<Evaluable<? extends Scalar>> seconds) {
		return a(1, data.getWaveLength(), scalarsMultiply(seconds, v(OutputLine.sampleRate)));
	}

	public Supplier<Runnable> setExponent(Supplier<Evaluable<? extends Scalar>> exp) {
		return a(1, data.getExponent(), exp);
	}

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		PackedCollection<?> value = new PackedCollection<>(1);
		OperationList push = new OperationList("PolynomialCell Push");
		push.add(new PolynomialCellPush(data, env == null ? v(1.0) :
				env.getScale(data.getWavePosition()), value));
		push.add(super.push(p(value)));
		return push;
	}


	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("PolynomialCell Tick");
		tick.add(new PolynomialCellTick(data, env == null ? v(1.0) :
				env.getScale(data.getWavePosition())));
		tick.add(super.tick());
		return tick;
	}
}
