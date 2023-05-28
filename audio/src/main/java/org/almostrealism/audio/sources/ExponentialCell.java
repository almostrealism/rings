/*
 * Copyright 2021 Michael Murray
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

import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.algebra.Scalar;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Factor;

import java.util.function.Supplier;

public class ExponentialCell extends CollectionTemporalCellAdapter implements SamplingFeatures {
	private Factor<Scalar> env;
	private final ExponentialCellData data;

	public ExponentialCell() {
		this(new ExponentialCellData());
	}

	public ExponentialCell(ExponentialCellData data) {
		this.data = data;
	}

	public void setEnvelope(Factor<Scalar> e) { this.env = e; }

	public void strike() { data.setNotePosition(0); }

	public void setInputScale(double scale) { data.setInputScale(scale); }

	public void setOutputScale(double scale) { data.setOutputScale(scale); }

	@Override
	public Supplier<Runnable> setup() {
		return () -> () -> {
			data.setNoteLength(toFramesMilli(1000));
			data.setDepth(CollectionTemporalCellAdapter.depth);
		};
	}

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		PackedCollection<?> value = new PackedCollection<>(1);
		OperationList push = new OperationList("ExponentialCell Push");
		push.add(new ExponentialCellPush(data, env == null ? v(1.0) :
				env.getResultant(data::getNotePosition), value));
		push.add(super.push(p(value)));
		return push;
	}


	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("ExponentialCell Tick");
		tick.add(new ExponentialCellTick(data, env == null ? v(1.0) :
				env.getResultant(data::getNotePosition)));
		tick.add(super.tick());
		return tick;
	}

	@Override
	public void reset() {
		super.reset();
		throw new UnsupportedOperationException();
	}
}
