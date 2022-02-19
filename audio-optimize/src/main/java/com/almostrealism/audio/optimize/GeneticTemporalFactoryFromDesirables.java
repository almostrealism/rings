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

package com.almostrealism.audio.optimize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.almostrealism.audio.DesirablesProvider;
import io.almostrealism.code.ProducerComputation;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.PolymorphicAudioCell;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AdjustableDelayCell;
import org.almostrealism.graph.temporal.ScalarTemporalCellAdapter;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.almostrealism.Ops;

public class GeneticTemporalFactoryFromDesirables implements CellFeatures {
	public static final int mixdownDuration = 140;

	public static final boolean enableRepeat = true;
	public static boolean enableMainFilterUp = true;
	public static boolean enableEfxFilters = true;
	public static boolean enableEfx = true;
	public static boolean disableClean = false;

	public GeneticTemporalFactory<Scalar, Scalar, Cells> from(DesirablesProvider provider) {
		List<TemporalFactor<Scalar>> temporals = new ArrayList<>();

		Function<Gene<Scalar>, WaveCell> generator = g -> {
				TemporalFactor<Scalar> tf = (TemporalFactor<Scalar>) g.valueAt(2);
				temporals.add(tf);

				Producer<Scalar> duration = tf.getResultant(v(bpm(provider.getBeatPerMinute()).l(1)));

				return provider.getWaves().getChoiceCell(
						g.valueAt(0).getResultant(Ops.ops().v(1.0)),
						g.valueAt(1).getResultant(duration),
						enableRepeat ? duration : null);
		};

		return (genome, measures, output) -> {
			Supplier<Runnable> genomeSetup = genome instanceof Setup ? ((Setup) genome).setup() : () -> () -> { };

			List<TemporalFactor<Scalar>> mainFilterUp = new ArrayList<>();

			// Generators
			CellList cells = cells(genome.valueAt(DefaultAudioGenome.GENERATORS).length(),
								i -> generator.apply(genome.valueAt(DefaultAudioGenome.GENERATORS, i)));

			if (enableMainFilterUp) {
				// Apply dynamic high pass filters
				cells = cells.map(fc(i -> {
					TemporalFactor<Scalar> f = (TemporalFactor<Scalar>) genome.valueAt(DefaultAudioGenome.MAIN_FILTER_UP, i, 0);
					mainFilterUp.add(f);
					return hp(scalarsMultiply(v(20000), f.getResultant(v(1.0))), v(DefaultAudioGenome.defaultResonance));
				})).addRequirements(mainFilterUp.toArray(TemporalFactor[]::new));
			}

			cells = cells
					.addRequirements(temporals.toArray(TemporalFactor[]::new))
					.addSetup(() -> genomeSetup);

			cells = cells.mixdown(mixdownDuration);

			// Volume adjustment
			CellList branch[] = cells.branch(
									fc(i -> genome.valueAt(DefaultAudioGenome.VOLUME, i, 0)),
									enableEfxFilters ?
											fc(i -> genome.valueAt(DefaultAudioGenome.VOLUME, i, 1)
											.andThen(genome.valueAt(DefaultAudioGenome.FX_FILTERS, i, 0))) :
											fc(i -> genome.valueAt(DefaultAudioGenome.VOLUME, i, 1)));

			CellList main = branch[0];
			CellList efx = branch[1];

			// Sum the main layer
			main = main.sum();

			if (enableEfx) {
				// Create the delay layers
				int delayLayers = genome.valueAt(DefaultAudioGenome.PROCESSORS).length();
				TemporalFactor<Scalar> adjust[] = IntStream.range(0, delayLayers)
						.mapToObj(i -> List.of(genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1),
								genome.valueAt(DefaultAudioGenome.WET_IN, i, 0)))
						.flatMap(List::stream)
						.map(factor -> factor instanceof TemporalFactor ? ((TemporalFactor) factor) : null)
						.filter(Objects::nonNull)
						.toArray(TemporalFactor[]::new);
				CellList delays = IntStream.range(0, delayLayers)
						 	.mapToObj(i -> new AdjustableDelayCell(
								 genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 0).getResultant(v(1.0)),
								 genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1).getResultant(v(1.0))))
						.collect(CellList.collector());

				// Route each line to each delay layer
				efx = efx.m(fi(), delays, i -> delayGene(delayLayers, genome.valueAt(DefaultAudioGenome.WET_IN, i)))
						.addRequirements(adjust)
						// Feedback grid
						.mself(fi(), genome.valueAt(DefaultAudioGenome.TRANSMISSION),
								 fc(genome.valueAt(DefaultAudioGenome.WET_OUT, 0)))
						.sum();

				if (disableClean) {
					efx.get(0).setReceptor(Receptor.to(output, measures.get(0), measures.get(1)));
					return efx;
				} else {
					// Mix efx with main and measure #2
					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));

					// Deliver main to the output and measure #1
					main = main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0))));

					return cells(main, efx);
				}
			} else {
				// Deliver main to the output and measure #1 and #2
				return main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
			}
		};
	}

	/**
	 * Create a {@link Gene} for routing delays.
	 * The current implementation delivers audio to
	 * the first delay based on the wet level, and
	 * delivers nothing to the others.
	 */
	private Gene<Scalar> delayGene(int delays, Gene<Scalar> wet) {
		ArrayListGene<Scalar> gene = new ArrayListGene<>();
		gene.add(wet.valueAt(0));
		IntStream.range(0, delays - 1).forEach(i -> gene.add(p -> v(0.0)));
		return gene;
	}
}
