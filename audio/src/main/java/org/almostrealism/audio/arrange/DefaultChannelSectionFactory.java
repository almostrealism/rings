/*
 * Copyright 2025 Michael Murray
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
import io.almostrealism.lifecycle.Destroyable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.optimize.FixedFilterChromosome;
import org.almostrealism.audio.optimize.OptimizeFactorFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.almostrealism.heredity.ProjectedChromosome;
import org.almostrealism.heredity.ProjectedGene;
import org.almostrealism.heredity.ProjectedGenome;
import org.almostrealism.io.Console;
import org.almostrealism.time.Frequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class DefaultChannelSectionFactory implements Setup, Destroyable,
						CellFeatures, EnvelopeFeatures, OptimizeFactorFeatures {
	public static boolean enableVolumeRiseFall = true;
	public static boolean enableFilter = true;

	public static double MAX_FILTER_RISE = 18000;

	public static final double repeatChoices[] = new double[] { 8, 16, 32 };

	private TimeCell clock;
	private PackedCollection<?> duration;

	private ProjectedChromosome volume;
	private ProjectedChromosome volumeExp;
	private ProjectedChromosome lowPassFilter;
	private ProjectedChromosome lowPassFilterExp;
	private ProjectedChromosome simpleDuration;
	private ProjectedChromosome simpleDurationSpeedUp;

	private int channel, channels;
	private IntPredicate wetChannels;
	private IntPredicate repeatChannels;

	private Supplier<Frequency> tempo;
	private DoubleSupplier measureDuration;
	private int length;
	private int sampleRate;

	public DefaultChannelSectionFactory(ProjectedGenome genome, int channels,
										IntPredicate wetChannels, IntPredicate repeatChannels,
										Supplier<Frequency> tempo, DoubleSupplier measureDuration,
										int length, int sampleRate) {
		this.clock = new TimeCell();
		this.duration = new PackedCollection<>(1);

		this.channels = channels;
		this.wetChannels = wetChannels;
		this.repeatChannels = repeatChannels;
		this.tempo = tempo;
		this.measureDuration = measureDuration;
		this.length = length;
		this.sampleRate = sampleRate;

		this.volume = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> {
			ProjectedGene gene = volume.addGene(3);
			gene.setRange(0, 0.1, 0.9);
			gene.setRange(1, 0.4, 0.8);
			gene.setRange(2, 0.0, 0.9);
		});

		this.volumeExp = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> {
			ProjectedGene g = volumeExp.addGene(1);
			g.setTransform(p -> oneToInfinity(p, 1.0).multiply(c(10.0)));
			g.setRange(0, factorForExponent(0.9), factorForExponent(1.2));
		});

		this.lowPassFilter = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> {
			ProjectedGene gene = lowPassFilter.addGene(3);
			gene.setRange(0, 0.1, 0.9);
			gene.setRange(1, 0.4, 0.8);
			gene.setRange(2, 0.0, 0.75);
		});

		this.lowPassFilterExp = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> {
			ProjectedGene g = lowPassFilterExp.addGene(1);
			g.setTransform(p -> oneToInfinity(p, 1.0).multiply(c(10.0)));
			g.setRange(0, factorForExponent(0.9), factorForExponent(2.5));
		});

		PackedCollection<?> repeat = new PackedCollection<>(repeatChoices.length);
		repeat.setMem(Arrays.stream(repeatChoices).map(this::factorForRepeat).toArray());

		this.simpleDuration = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> simpleDuration.addChoiceGene(repeat, 1));

		this.simpleDurationSpeedUp = genome.addChromosome();
		IntStream.range(0, channels).forEach(i -> {
			ProjectedGene g = simpleDurationSpeedUp.addGene(2);
			g.setTransform(p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
			g.setRange(0, factorForRepeatSpeedUpDuration(1), factorForRepeatSpeedUpDuration(4));
			g.setRange(1, factorForRepeatSpeedUpDuration(16), factorForRepeatSpeedUpDuration(80));
		});
	}

	public Section createSection(int position) {
		if (channel >= channels)
			throw new IllegalArgumentException();
		return new Section(position, length, channel++,
				(int) (sampleRate * length * measureDuration.getAsDouble()));
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList();
		setup.add(() -> () -> duration.setMem(length * measureDuration.getAsDouble()));
		return setup;
	}

	@Override
	public void destroy() {
		Destroyable.super.destroy();

		if (clock != null) clock.destroy();
		if (duration != null) duration.destroy();
		clock = null;
		duration = null;
	}

	@Override
	public Console console() { return CellFeatures.console; }

	public class Section implements ChannelSection {
		private int position, length;
		private int channel;
		private int samples;
		private List<Destroyable> dependencies;

		public Section() { }

		protected Section(int position, int length,
						  int channel, int samples) {
			this.position = position;
			this.length = length;
			this.channel = channel;
			this.samples = samples;
			this.dependencies = new ArrayList<>();
		}

		@Override
		public int getPosition() { return position; }

		@Override
		public int getLength() { return length; }

		@Override
		public Supplier<Runnable> process(Producer<PackedCollection<?>> destination, Producer<PackedCollection<?>> source) {
			PackedCollection<?> input = new PackedCollection<>(samples);
			PackedCollection<PackedCollection<?>> output = (PackedCollection) new PackedCollection(shape(1, samples)).traverse(1);
			dependencies.add(input);
			dependencies.add(output);

			int repeatGene = channel; // 0;
			Producer<PackedCollection<?>> r = simpleDuration.valueAt(repeatGene, 0)
													.getResultant(c(tempo.get().l(1)));
			Producer<PackedCollection<?>> su = simpleDurationSpeedUp.valueAt(repeatGene, 0)
													.getResultant(c(1.0));
			Producer<PackedCollection<?>> so = simpleDurationSpeedUp.valueAt(repeatGene, 1)
													.getResultant(c(1.0));
			Producer<PackedCollection<?>> repeat = durationAdjustment(concat(r, su), so, clock.time(sampleRate));

			CellList cells = cells(1, i ->
					new WaveCell(input.traverseEach(), sampleRate, 1.0, c(0.0), repeatChannels.test(channel) ? toScalar(repeat) : null))
					.addRequirements(clock);

			if (enableVolumeRiseFall) {
				Producer<PackedCollection<?>> d = volume.valueAt(channel, 0).getResultant(c(1.0));
				Producer<PackedCollection<?>> m = volume.valueAt(channel, 1).getResultant(c(1.0));
				Producer<PackedCollection<?>> p = volume.valueAt(channel, 2).getResultant(c(1.0));
				Producer<PackedCollection<?>> e = volumeExp.valueAt(channel, 0).getResultant(c(1.0));

				Producer<PackedCollection<?>> v = riseFall(0, 1.0, 0.0,
						d, m, p, e, clock.time(sampleRate), p(duration));

				cells = cells.map(fc(i -> volume(v)));
			}

			if (enableFilter && wetChannels.test(channel)) {
				Producer<PackedCollection<?>> d = lowPassFilter.valueAt(channel, 0).getResultant(c(1.0));
				Producer<PackedCollection<?>> m = lowPassFilter.valueAt(channel, 1).getResultant(c(1.0));
				Producer<PackedCollection<?>> p = lowPassFilter.valueAt(channel, 2).getResultant(c(1.0));
				Producer<PackedCollection<?>> e = lowPassFilterExp.valueAt(channel, 0).getResultant(c(1.0));

				Producer<PackedCollection<?>> lp = riseFall(0, MAX_FILTER_RISE, 0.0,
															d, m, p, e, clock.time(sampleRate), p(duration));
				cells = cells.map(fc(i -> lp(lp, scalar(FixedFilterChromosome.defaultResonance))));
			}

			dependencies.add(cells);

			OperationList process = new OperationList();
			process.add(new MemoryDataCopy("DefaultChannelSection Input", () -> source.get().evaluate(), () -> input, samples));
			process.add(cells.export(output));
			process.add(new MemoryDataCopy("DefaultChannelSection Output", () -> output, () -> destination.get().evaluate(), samples));
			return process;
		}

		@Override
		public void destroy() {
			ChannelSection.super.destroy();
			dependencies.forEach(Destroyable::destroy);
			dependencies.clear();
		}
	}
}
