/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.optimize.FixedFilterChromosome;
import org.almostrealism.audio.optimize.LinearInterpolationChromosome;
import org.almostrealism.audio.optimize.OptimizeFactorFeatures;
import org.almostrealism.audio.optimize.RiseFallChromosome;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.TemporalList;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class DefaultChannelSectionFactory implements Setup, CellFeatures, OptimizeFactorFeatures {
	public static boolean enableVolumeRiseFall = true;
	public static boolean enableRepeat = true;
	public static boolean enableFilter = true;

	public static final double repeatChoices[] = new double[] { 8, 16, 32 };

	private TimeCell clock;
	private LinearInterpolationChromosome volume;
	private RiseFallChromosome volumeRiseFall;
	private RiseFallChromosome lowPassFilter;
	private SimpleChromosome simpleDuration;
	private SimpleChromosome simpleDurationSpeedUp;

	private int channel, channels;
	private IntPredicate wetChannels;
	private IntPredicate repeatChannels;

	private Supplier<Frequency> tempo;
	private DoubleSupplier measureDuration;
	private int length;
	private int sampleRate;

	public DefaultChannelSectionFactory(ConfigurableGenome genome, int channels, IntPredicate wetChannels, IntPredicate repeatChannels,
										Supplier<Frequency> tempo, DoubleSupplier measureDuration, int length, int sampleRate) {
		this.clock = new TimeCell();
		this.channels = channels;
		this.wetChannels = wetChannels;
		this.repeatChannels = repeatChannels;
		this.tempo = tempo;
		this.measureDuration = measureDuration;
		this.length = length;
		this.sampleRate = sampleRate;

		SimpleChromosome v = genome.addSimpleChromosome(LinearInterpolationChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> v.addGene());
		this.volume = new LinearInterpolationChromosome(v, 0.0, 1.0, sampleRate);
		this.volume.setGlobalTime(clock.frame());

		SimpleChromosome vrf = genome.addSimpleChromosome(RiseFallChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> vrf.addGene());
		this.volumeRiseFall = new RiseFallChromosome(vrf, 0.0, 1.0, 0.5, sampleRate);
		this.volumeRiseFall.setGlobalTime(clock.frame());

		SimpleChromosome lp = genome.addSimpleChromosome(RiseFallChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> lp.addGene());
		this.lowPassFilter = new RiseFallChromosome(lp, 0.0, 20000.0, 0.5, sampleRate);
		this.lowPassFilter.setGlobalTime(clock.frame());

		PackedCollection<?> repeat = new PackedCollection<>(repeatChoices.length);
		repeat.setMem(Arrays.stream(repeatChoices).map(this::factorForRepeat).toArray());

		this.simpleDuration = genome.addSimpleChromosome(1);
		IntStream.range(0, channels).forEach(i -> simpleDuration.addChoiceGene(repeat));

		this.simpleDurationSpeedUp = genome.addSimpleChromosome(2);
		IntStream.range(0, channels).forEach(i -> {
			SimpleGene g = simpleDurationSpeedUp.addGene();
			g.setTransform(p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
		});

		initRanges();
	}

	protected void initRanges() {
		simpleDurationSpeedUp.setParameterRange(0, factorForRepeatSpeedUpDuration(1), factorForRepeatSpeedUpDuration(4));
		simpleDurationSpeedUp.setParameterRange(1, factorForRepeatSpeedUpDuration(16), factorForRepeatSpeedUpDuration(52));
	}

	public Section createSection(int position) {
		if (channel >= channels) throw new IllegalArgumentException();
		return new Section(position, length, channel++,
				(int) (sampleRate * length * measureDuration.getAsDouble()));
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList();
		setup.add(() -> () -> volume.setDuration(length * measureDuration.getAsDouble()));
		setup.add(() -> () -> volumeRiseFall.setDuration(length * measureDuration.getAsDouble()));
		setup.add(() -> () -> lowPassFilter.setDuration(length * measureDuration.getAsDouble()));
		setup.add(volume.expand());
		setup.add(volumeRiseFall.expand());
		setup.add(lowPassFilter.expand());
		return setup;
	}

	public class Section implements ChannelSection {
		private int position, length;
		private int geneIndex;
		private int samples;

		public Section() { }

		protected Section(int position, int length,
						  int geneIndex, int samples) {
			this.position = position;
			this.length = length;
			this.geneIndex = geneIndex;
			this.samples = samples;
		}

		@Override
		public int getPosition() { return position; }

		@Override
		public int getLength() { return length; }

		@Override
		public Supplier<Runnable> process(Producer<PackedCollection<?>> destination, Producer<PackedCollection<?>> source) {
			PackedCollection<?> input = new PackedCollection<>(samples);
			PackedCollection<PackedCollection<?>> output = (PackedCollection) new PackedCollection(shape(1, samples)).traverse(1);

			TemporalList temporals = new TemporalList();
			temporals.addAll(volume.getTemporals());
			temporals.addAll(volumeRiseFall.getTemporals());
			temporals.addAll(lowPassFilter.getTemporals());

			int repeatGene = geneIndex; // 0;
			Producer<PackedCollection<?>> r = simpleDuration.valueAt(repeatGene, 0)
													.getResultant(c(tempo.get().l(1)));
			Producer<PackedCollection<?>> su = simpleDurationSpeedUp.valueAt(repeatGene, 0)
													.getResultant(c(1.0));
			Producer<PackedCollection<?>> so = simpleDurationSpeedUp.valueAt(repeatGene, 1)
													.getResultant(c(1.0));
			Producer<PackedCollection<?>> repeat = durationAdjustment(concat(r, su), so, clock.time(sampleRate));

			CellList cells = cells(1, i ->
					new WaveCell(input.traverseEach(), sampleRate, 1.0, c(0.0), repeatChannels.test(channel) ? toScalar(repeat) : null))
					.addRequirements(clock)
					// TODO  Why can't the list just be added?
					.addRequirements(temporals.toArray(TemporalFactor[]::new));

			if (enableVolumeRiseFall) {
				cells = cells.map(fc(i -> factor(volumeRiseFall.valueAt(geneIndex, 0))));
			} else {
				cells = cells.map(fc(i -> factor(volume.valueAt(geneIndex, 0))));
			}

			if (enableFilter && wetChannels.test(channel)) {
				Factor<PackedCollection<?>> factor = lowPassFilter.valueAt(geneIndex, 0);
				Producer<PackedCollection<?>> lp = factor(factor).getResultant(c(1.0));
				cells = cells.map(fc(i -> lp(lp, v(FixedFilterChromosome.defaultResonance))));
			}

			OperationList process = new OperationList();
			process.add(new MemoryDataCopy("DefaultChannelSection Input", () -> source.get().evaluate(), () -> input, samples));
			process.add(cells.export(output));
			process.add(new MemoryDataCopy("DefaultChannelSection Output", () -> output, () -> destination.get().evaluate(), samples));
			return process;
		}

		/**
		 * This method wraps the specified {@link Factor} to prevent it from
		 * being detected as Temporal by {@link org.almostrealism.graph.FilteredCell}s
		 * that would proceed to invoke the {@link org.almostrealism.time.Temporal#tick()} operation.
		 * This is not a good solution, and this process needs to be reworked, so
		 * it is clear who bears the responsibility for invoking {@link org.almostrealism.time.Temporal#tick()}
		 * and it doesn't get invoked multiple times.
		 */
		private Factor<PackedCollection<?>> factor(Factor<PackedCollection<?>> f) {
			return v -> f.getResultant(v);
		}
	}
}
