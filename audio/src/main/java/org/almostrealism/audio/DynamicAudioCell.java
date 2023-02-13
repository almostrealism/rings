/*
 * Copyright 2022 Michael Murray
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
import org.almostrealism.audio.data.PolymorphicAudioDataBank;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;

import java.util.List;
import java.util.function.Function;

public class DynamicAudioCell extends AudioCellChoiceAdapter {
	public DynamicAudioCell(ProducerComputation<PackedCollection<?>> decision,
								List<Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>> choices) {
		this(new PolymorphicAudioDataBank(choices.size()), decision, choices);
	}

	public DynamicAudioCell(PolymorphicAudioDataBank data, ProducerComputation<PackedCollection<?>> decision,
							List<Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>> choices) {
		super(decision, data::get, choices, true);
	}
}

