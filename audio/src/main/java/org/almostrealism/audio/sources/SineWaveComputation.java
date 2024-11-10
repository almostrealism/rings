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

package org.almostrealism.audio.sources;

import io.almostrealism.expression.InstanceReference;
import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationComputationAdapter;

import java.util.function.Supplier;

public abstract class SineWaveComputation extends OperationComputationAdapter<PackedCollection<?>> {
	protected static final double TWO_PI = 2 * Math.PI;

	public SineWaveComputation(SineWaveCellData data, Producer<Scalar> envelope, PackedCollection<?> output) {
		super((Supplier) Ops.o().p(output),
				(Supplier) data.getWavePosition(),
				(Supplier) data.getWaveLength(),
				(Supplier) data.getNotePosition(),
				(Supplier) data.getNoteLength(),
				(Supplier) data.getPhase(),
				(Supplier) data.getAmplitude(),
				(Supplier) data.getDepth(),
				(Supplier) envelope);
	}

	public ArrayVariable<Double> getOutput() { return getArgument(0, 1); }
	public ArrayVariable<Double> getWavePosition() { return getArgument(1, 2); }
	public ArrayVariable<Double> getWaveLength() { return getArgument(2, 2); }
	public ArrayVariable<Double> getNotePosition() { return getArgument(3, 2); }
	public ArrayVariable<Double> getNoteLength() { return getArgument(4, 2); }
	public ArrayVariable<Double> getPhase() { return getArgument(5, 2); }
	public ArrayVariable<Double> getAmplitude() { return getArgument(6, 2); }
	public ArrayVariable<Double> getDepth() { return getArgument(7, 2); }
	public ArrayVariable<Double> getEnvelope() { return getArgument(8, 2); }

	public InstanceReference<ArrayVariable<Double>, Double> output() { return (InstanceReference<ArrayVariable<Double>, Double>) getOutput().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> wavePosition() { return (InstanceReference<ArrayVariable<Double>, Double>) getWavePosition().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> waveLength() { return (InstanceReference<ArrayVariable<Double>, Double>) getWaveLength().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> notePosition() { return (InstanceReference<ArrayVariable<Double>, Double>) getNotePosition().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> noteLength() { return (InstanceReference<ArrayVariable<Double>, Double>) getNoteLength().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> phase() { return (InstanceReference<ArrayVariable<Double>, Double>) getPhase().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> amplitude() { return (InstanceReference<ArrayVariable<Double>, Double>) getAmplitude().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> depth() { return (InstanceReference<ArrayVariable<Double>, Double>) getDepth().valueAt(0); }
	public InstanceReference<ArrayVariable<Double>, Double> envelope() { return (InstanceReference<ArrayVariable<Double>, Double>) getEnvelope().valueAt(0); }
}
