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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.audio.optimize.LinearInterpolationChromosome;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.ReceptorCell;
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
import org.almostrealism.time.TemporalList;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class DefaultChannelSection implements ChannelSection, CellFeatures {
	public static boolean enableFilter = true;

	private int position, length;

	private TimeCell clock;
	private LinearInterpolationChromosome volume;
	private LinearInterpolationChromosome lowPassFilter;
	private int geneIndex;

	private int samples;
	private int sampleRate;

	public DefaultChannelSection() { }

	protected DefaultChannelSection(int position, int length,
									TimeCell clock,
									LinearInterpolationChromosome volume,
									LinearInterpolationChromosome lowPassFilter,
									int geneIndex,
									int samples, int sampleRate) {
		this.position = position;
		this.length = length;
		this.clock = clock;
		this.volume = volume;
		this.lowPassFilter = lowPassFilter;
		this.geneIndex = geneIndex;
		this.samples = samples;
		this.sampleRate = sampleRate;
	}

	@Override
	public int getPosition() { return position; }

	@Override
	public int getLength() { return length; }

	@Override
	public Supplier<Runnable> process(Producer<PackedCollection<?>> destination, Producer<PackedCollection<?>> source) {
		PackedCollection<?> input = new PackedCollection<>(samples);
		PackedCollection<PackedCollection<?>> output = new PackedCollection(shape(1, samples)).traverse(1);

		TemporalList temporals = lowPassFilter.getTemporals();

		Factor<PackedCollection<?>> factor = lowPassFilter.valueAt(geneIndex, 0);
		CellList cells = cells(1, i -> new WaveCell(input.traverseEach(), sampleRate));

		if (enableFilter) {
			cells = cells
					.addSetup((Setup) factor)
					.addRequirements(clock, temporals)
					.map(fc(i -> lp(factor.getResultant(c(1.0)),
							v(DefaultAudioGenome.defaultResonance))));
		}

		OperationList process = new OperationList();
		process.add(new MemoryDataCopy("DefaultChannelSection Input", () -> source.get().evaluate(), () -> input, samples));
		process.add(cells.export(output));
		process.add(new MemoryDataCopy("DefaultChannelSection Output", () -> output, () -> destination.get().evaluate(), samples));

		KernelizedEvaluable product = _multiply(v(0, 0), v(0, 1)).get();
		process.add(() -> () ->
			// TODO  Should be able to just use ::evaluate here...
			product.kernelEvaluate(destination.get().evaluate().traverseEach(),
									source.get().evaluate().traverseEach(),
									volume.getKernelList(0).valueAt(geneIndex)));
		return process;
	}

	// TODO  Honestly at this point I think the factory should be the parent class
	// TODO  and the channel section should be the child. The constructor to the
	// TODO  channel section is getting overloaded with arguments that all could
	// TODO  just be instance variables of the factory if it was the parent.
	public static class Factory implements Setup {
		private TimeCell clock;
		private LinearInterpolationChromosome volume;
		private LinearInterpolationChromosome lowPassFilter;
		private int channel, channels;

		private DoubleSupplier measureDuration;
		private int length;
		private int sampleRate;

		public Factory(ConfigurableGenome genome, int channels, DoubleSupplier measureDuration, int length, int sampleRate) {
			this.clock = new TimeCell();
			this.channels = channels;
			this.measureDuration = measureDuration;
			this.length = length;
			this.sampleRate = sampleRate;

			SimpleChromosome v = genome.addSimpleChromosome(LinearInterpolationChromosome.SIZE);

			for (int i = 0; i < channels; i++) {
				SimpleGene g = v.addGene();

				// TODO  Testing interpolation from 0.2 to 0.95 - this should be removed
				g.set(0, 0.2);
				g.set(1, 0.95);
			}

			this.volume = new LinearInterpolationChromosome(v, 0.0, 1.0, sampleRate);
			this.volume.setGlobalTime(clock.frame());

			SimpleChromosome lp = genome.addSimpleChromosome(LinearInterpolationChromosome.SIZE);

			for (int i = 0; i < channels; i++) {
				SimpleGene g = lp.addGene();

				// TODO  Testing interpolation from 0.0 to 1.0 - this should be removed
				g.set(0, 0.0);
				g.set(1, 1.0);
			}

			this.lowPassFilter = new LinearInterpolationChromosome(lp, 0.0, 20000.0, sampleRate);
			this.lowPassFilter.setGlobalTime(clock.frame());
		}

		public DefaultChannelSection createSection(int position) {
			if (channel >= channels) throw new IllegalArgumentException();
			return new DefaultChannelSection(position, length, clock, volume, lowPassFilter, channel++,
					(int) (sampleRate * length * measureDuration.getAsDouble()), sampleRate);
		}

		@Override
		public Supplier<Runnable> setup() {
			OperationList setup = new OperationList();
			setup.add(() -> () -> volume.setDuration(length * measureDuration.getAsDouble()));
			setup.add(() -> () -> lowPassFilter.setDuration(length * measureDuration.getAsDouble()));
			setup.add(volume.expand());
			setup.add(lowPassFilter.expand());
			return setup;
		}
	}
}
