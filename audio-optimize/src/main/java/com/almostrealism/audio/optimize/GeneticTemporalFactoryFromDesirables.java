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

package com.almostrealism.audio.optimize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.almostrealism.audio.DesirablesProvider;
import io.almostrealism.code.ProducerComputation;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.PolymorphicAudioCell;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.data.PolymorphicAudioDataBank;
import org.almostrealism.audio.filter.AdjustableDelayCell;
import org.almostrealism.audio.filter.AudioCellAdapter;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.organs.GeneticTemporalFactory;
import org.almostrealism.util.Ops;

public class GeneticTemporalFactoryFromDesirables implements CellFeatures {
	public GeneticTemporalFactory<Scalar, Cells> from(DesirablesProvider provider) {
		Supplier<PolymorphicAudioData> dataSupplier = new Supplier<>() {
			private final int SIZE = 50;
			private final PolymorphicAudioDataBank bank = new PolymorphicAudioDataBank(SIZE);
			private int count = 0;

			@Override
			public PolymorphicAudioData get() {
				if (count >= SIZE) throw new IllegalArgumentException("No more audio data space available");
				return bank.get(count++);
			}
		};

		List<Function<PolymorphicAudioData, ? extends AudioCellAdapter>> choices = new ArrayList<>();

		if (!provider.getFrequencies().isEmpty()) {
			provider.getFrequencies().forEach(f -> choices.add(data -> (AudioCellAdapter) w(() -> data, f).get(0)));
		}

		if (!provider.getSamples().isEmpty()) {
			provider.getSamples().forEach(f -> choices.add(data -> (AudioCellAdapter) w(() -> data, f).get(0)));
		}

		Function<Gene<Scalar>, PolymorphicAudioCell> generator = g ->
				new PolymorphicAudioCell(dataSupplier.get(), (ProducerComputation<Scalar>) g.valueAt(0).getResultant(Ops.ops().v(1.0)), choices);

		return (genome, meter) -> {
			CellList cells =
					// Generators
					cells(genome.valueAt(0).length(), i -> generator.apply((Gene) genome.valueAt(0).valueAt(i)))
							// Volume adjustment
							.f(i -> (Factor) genome.valueAt(1).valueAt(i).valueAt(0))
							// Processing
							.map(i -> new AdjustableDelayCell((Scalar) genome.valueAt(2).valueAt(i).valueAt(1).getResultant((Producer) v(60)).get().evaluate()))
							// Feedback grid
							.mself(fc(i -> (Factor) genome.valueAt(4).valueAt(i).valueAt(0)), i -> (Gene<Scalar>) genome.valueAt(3).valueAt(i));

			((CellAdapter) cells.get(cells.size() - 1)).setMeter(meter);
			return cells;
		};
	}
}
