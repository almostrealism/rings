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
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.audio.optimize.LinearInterpolationChromosome;
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
import org.almostrealism.time.TemporalList;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class DefaultChannelSectionFactory implements Setup, CellFeatures {
	public static boolean enableFilter = false;

	private TimeCell clock;
	private LinearInterpolationChromosome volume;
	private RiseFallChromosome lowPassFilter;
	private int channel, channels;

	private DoubleSupplier measureDuration;
	private int length;
	private int sampleRate;

	public DefaultChannelSectionFactory(ConfigurableGenome genome, int channels, DoubleSupplier measureDuration, int length, int sampleRate) {
		this.clock = new TimeCell();
		this.channels = channels;
		this.measureDuration = measureDuration;
		this.length = length;
		this.sampleRate = sampleRate;

		SimpleChromosome v = genome.addSimpleChromosome(LinearInterpolationChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> v.addGene());
		this.volume = new LinearInterpolationChromosome(v, 0.0, 1.0, sampleRate);
		this.volume.setGlobalTime(clock.frame());

		SimpleChromosome lp = genome.addSimpleChromosome(RiseFallChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> lp.addGene());
		this.lowPassFilter = new RiseFallChromosome(lp, 0.0, 20000.0, 0.5, sampleRate);
		this.lowPassFilter.setGlobalTime(clock.frame());
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
		setup.add(() -> () -> lowPassFilter.setDuration(length * measureDuration.getAsDouble()));
		setup.add(volume.expand());
		setup.add(lowPassFilter.expand());
		return setup;
	}

	public class Section implements ChannelSection {
		private int position, length;
		private int geneIndex;
		private int samples;

		public Section() { }

		protected Section(int position, int length,
										int geneIndex,
										int samples) {
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

			KernelizedEvaluable product = multiply(v(1, 0), v(1, 1)).get();
			process.add(() -> () ->
					product.into(destination.get().evaluate().traverseEach()).evaluate(
							source.get().evaluate().traverseEach(),
							volume.getKernelList(0).valueAt(geneIndex)));

//			process.add(() -> () -> {
//				lowPassFilter.getKernelList(0).getData().getCount();
//			});
			return process;
		}
	}
}
