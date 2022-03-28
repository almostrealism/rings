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

import java.io.File;
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
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AdjustableDelayCell;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.graph.temporal.ScalarTemporalCellAdapter;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.CellularTemporalFactor;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.graph.temporal.GeneticTemporalFactory;
import org.almostrealism.Ops;
import org.almostrealism.time.Temporal;

public class GeneticTemporalFactoryFromDesirables implements CellFeatures {
	public static final int mixdownDuration = 140;

	public static final boolean enableRepeat = true;
	public static boolean enableMainFilterUp = true;
	public static boolean enableEfxFilters = true;
	public static boolean enableEfx = true;
	public static boolean enableWetInAdjustment = true;
	public static boolean enableMasterFilterDown = false;

	public static boolean enableMixdown = false;
	public static boolean enableSourcesOnly = false;
	public static boolean disableClean = false;

	public static Waves sourceOverride = null;

	public GeneticTemporalFactory<Scalar, Scalar, Cells> from(DesirablesProvider provider) {
		Function<Gene<Scalar>, WaveCell> generator = g -> {
				Producer<Scalar> duration = g.valueAt(2).getResultant(v(bpm(provider.getBeatPerMinute()).l(1)));

				if (sourceOverride == null) {
					return provider.getWaves().getChoiceCell(
							g.valueAt(0).getResultant(Ops.ops().v(1.0)),
							g.valueAt(1).getResultant(duration),
							enableRepeat ? duration : null);
				} else {
					return sourceOverride.getChoiceCell(g.valueAt(0).getResultant(Ops.ops().v(1.0)), v(0.0), null);
				}
		};

		return (genome, measures, output) -> {
			Supplier<Runnable> genomeSetup = genome instanceof Setup ? ((Setup) genome).setup() : () -> () -> { };

			// Generators
			CellList cells = cells(genome.valueAt(DefaultAudioGenome.GENERATORS).length(),
								i -> generator.apply(genome.valueAt(DefaultAudioGenome.GENERATORS, i)));

			cells.addSetup(() -> genomeSetup);

			if (enableMainFilterUp) {
				// Apply dynamic high pass filters
				cells = cells.map(fc(i -> {
					TemporalFactor<Scalar> f = (TemporalFactor<Scalar>) genome.valueAt(DefaultAudioGenome.MAIN_FILTER_UP, i, 0);
					return hp(scalarsMultiply(v(20000), f.getResultant(v(1.0))), v(DefaultAudioGenome.defaultResonance));
				}));
			}

			cells = cells
					.addRequirements(((DefaultAudioGenome) genome).getTemporals().toArray(TemporalFactor[]::new));

			if (enableSourcesOnly) {
				return cells.map(fc(i -> factor(genome.valueAt(DefaultAudioGenome.VOLUME, i, 0))))
						.sum().map(fc(i -> sf(0.2))).map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
			}

			if (enableMixdown)
				cells = cells.mixdown(mixdownDuration);

			// Volume adjustment
			CellList branch[] = cells.branch(
									fc(i -> factor(genome.valueAt(DefaultAudioGenome.VOLUME, i, 0))),
									enableEfxFilters ?
											fc(i -> factor(genome.valueAt(DefaultAudioGenome.VOLUME, i, 0))
											.andThen(genome.valueAt(DefaultAudioGenome.FX_FILTERS, i, 0))) :
											fc(i -> factor(genome.valueAt(DefaultAudioGenome.VOLUME, i, 0))));

			CellList main = branch[0];
			CellList efx = branch[1];

			// Sum the main layer
			main = main.sum();

			if (enableEfx) {
				// Create the delay layers
				int delayLayers = genome.valueAt(DefaultAudioGenome.PROCESSORS).length();
				CellList delays = IntStream.range(0, delayLayers)
						 	.mapToObj(i -> new AdjustableDelayCell(
								 genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 0).getResultant(v(1.0)),
								 genome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1).getResultant(v(1.0))))
						.collect(CellList.collector());

				// Route each line to each delay layer
				efx = efx.m(fi(), delays, i -> delayGene(delayLayers, genome.valueAt(DefaultAudioGenome.WET_IN, i)))
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

					if (enableMasterFilterDown) {
						// Apply dynamic low pass filter
						main = main.map(fc(i -> {
							TemporalFactor<Scalar> f = (TemporalFactor<Scalar>) genome.valueAt(DefaultAudioGenome.MASTER_FILTER_DOWN, i, 0);
							return lp(scalarsMultiply(v(20000), f.getResultant(v(1.0))), v(DefaultAudioGenome.defaultResonance));
//							return lp(scalarsMultiply(v(20000), v(1.0)), v(DefaultAudioGenome.defaultResonance));
						}));
					}

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
	 * This method wraps the specified {@link Factor} to prevent it from
	 * being detected as Temporal by {@link org.almostrealism.graph.FilteredCell}s
	 * that would proceed to invoke the {@link Temporal#tick()} operation.
	 * This is not a good solution, and this process needs to be reworked so
	 * it is clear who bears the responsibility for invoking {@link Temporal#tick()}
	 * and it doesn't get invoked multiple times.
	 */
	private Factor<Scalar> factor(Factor<Scalar> f) {
		return v -> f.getResultant(v);
	}

	/**
	 * Create a {@link Gene} for routing delays.
	 * The current implementation delivers audio to
	 * the first delay based on the wet level, and
	 * delivers nothing to the others.
	 */
	private Gene<Scalar> delayGene(int delays, Gene<Scalar> wet) {
		ArrayListGene<Scalar> gene = new ArrayListGene<>();

		if (enableWetInAdjustment) {
			gene.add(factor(wet.valueAt(0)));
		} else {
			gene.add(p -> v(0.2).multiply(p));
		}

		IntStream.range(0, delays - 1).forEach(i -> gene.add(p -> v(0.0)));
		return gene;
	}
}
