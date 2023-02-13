/*
 * Copyright 2021 Michael Murray
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

import io.almostrealism.code.ProducerComputation;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PolymorphicAudioCell extends AudioCellChoiceAdapter {

	public PolymorphicAudioCell(PolymorphicAudioData data, ProducerComputation<PackedCollection<?>> decision,
								Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>... choices) {
		this(data, decision, Arrays.asList(choices));
	}

	public PolymorphicAudioCell(PolymorphicAudioData data, ProducerComputation<PackedCollection<?>> decision,
								List<Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>> choices) {
		super(decision, i -> data, choices, false);
	}

	public PolymorphicAudioCell(ProducerComputation<PackedCollection<?>> decision,
								List<CollectionTemporalCellAdapter> choices) {
		super(decision, choices, false);
	}
}
