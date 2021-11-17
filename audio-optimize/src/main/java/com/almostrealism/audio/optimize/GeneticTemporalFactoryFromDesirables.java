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
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.SummationCell;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.organs.GeneticTemporalFactory;
import org.almostrealism.util.Ops;

public class GeneticTemporalFactoryFromDesirables implements CellFeatures {
	public GeneticTemporalFactory<Scalar, Scalar, Cells> from(DesirablesProvider provider) {
		Supplier<PolymorphicAudioData> dataSupplier = new Supplier<>() {
			private final int SIZE = 100;
			private final PolymorphicAudioDataBank bank = new PolymorphicAudioDataBank(SIZE);
			private int count = 0;

			@Override
			public PolymorphicAudioData get() {
				if (count >= SIZE) throw new IllegalArgumentException("No more audio data space available");
				return bank.get(count++);
			}
		};

		List<Function<Gene<Scalar>, AudioCellAdapter>> choices = new ArrayList<>();

		if (!provider.getFrequencies().isEmpty()) {
			provider.getFrequencies().forEach(f -> choices.add(g -> (AudioCellAdapter) w(dataSupplier, f).get(0)));
		}

		if (!provider.getSamples().isEmpty()) {
			provider.getSamples().forEach(f -> choices.add(g -> {
				Producer<Scalar> duration = g.valueAt(2).getResultant(v(bpm(provider.getBeatPerMinute()).l(1)));
				return (AudioCellAdapter) w(dataSupplier, g.valueAt(1).getResultant(duration), duration, f).get(0);
			}));
		}

		Function<Gene<Scalar>, PolymorphicAudioCell> generator = g ->
				new PolymorphicAudioCell(
						(ProducerComputation<Scalar>) g.valueAt(0).getResultant(Ops.ops().v(1.0)),
						choices.stream().map(c -> c.apply(g)).collect(Collectors.toList()));

		return (genome, measures, output) -> {
			// Generators
			CellList cells = cells(genome.valueAt(SimpleOrganGenome.GENERATORS).length(),
								i -> generator.apply(genome.valueAt(SimpleOrganGenome.GENERATORS, i)));

			// Volume adjustment
			CellList branch[] = cells.branch(
									fc(i -> genome.valueAt(SimpleOrganGenome.VOLUME, i, 0)),
									fc(i -> genome.valueAt(SimpleOrganGenome.VOLUME, i, 1)
											.andThen(genome.valueAt(SimpleOrganGenome.FILTERS, i, 0))));

			CellList main = branch[0];
			CellList efx = branch[1];

			main = main.sum();

			TemporalFactor<Scalar> adjust[] = IntStream.range(0, efx.size())
					.mapToObj(i -> genome.valueAt(SimpleOrganGenome.PROCESSORS, i, 1))
					.toArray(TemporalFactor[]::new);

			efx = efx
					// Processing
					.map(i ->
							new AdjustableDelayCell(genome.valueAt(SimpleOrganGenome.PROCESSORS, i, 0).getResultant(v(60)),
													adjust[i].getResultant(v(1.0))))
					.addRequirements(adjust)
					// Feedback grid
					.mself(fi(),
							genome.valueAt(SimpleOrganGenome.TRANSMISSION),
							fc(genome.valueAt(SimpleOrganGenome.WET, 0)))
					.sum();

			// Mix efx with main and measure #2
			efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));

			// Deliver main to the output and measure #1
			main = main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0))));

			return cells(main, efx);
		};
	}
}
