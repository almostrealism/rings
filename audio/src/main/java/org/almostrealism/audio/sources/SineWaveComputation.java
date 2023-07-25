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

import io.almostrealism.scope.ArrayVariable;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationComputationAdapter;

import java.util.function.Supplier;

public abstract class SineWaveComputation extends OperationComputationAdapter {
	protected static final double TWO_PI = 2 * Math.PI;

	public SineWaveComputation(SineWaveCellData data, Producer<Scalar> envelope, PackedCollection<?> output) {
		super((Supplier) Ops.ops().p(output),
				(Supplier) data.getWavePosition(),
				(Supplier) data.getWaveLength(),
				(Supplier) data.getNotePosition(),
				(Supplier) data.getNoteLength(),
				(Supplier) data.getPhase(),
				(Supplier) data.getAmplitude(),
				(Supplier) data.getDepth(),
				(Supplier) envelope);
	}

	public ArrayVariable getOutput() { return getArgument(0, 1); }
	public ArrayVariable getWavePosition() { return getArgument(1, 2); }
	public ArrayVariable getWaveLength() { return getArgument(2, 2); }
	public ArrayVariable getNotePosition() { return getArgument(3, 2); }
	public ArrayVariable getNoteLength() { return getArgument(4, 2); }
	public ArrayVariable getPhase() { return getArgument(5, 2); }
	public ArrayVariable getAmplitude() { return getArgument(6, 2); }
	public ArrayVariable getDepth() { return getArgument(7, 2); }
	public ArrayVariable getEnvelope() { return getArgument(8, 2); }
}
