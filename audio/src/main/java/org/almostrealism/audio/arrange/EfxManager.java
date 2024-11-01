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

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.time.computations.MultiOrderFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class EfxManager implements CellFeatures {
	public static boolean enableEfx = true;
	public static boolean enableAutomation = true;
	public static double maxWet = 0.5;
	public static double maxFeedback = 0.5;
	public static int filterOrder = 40;

	private AutomationManager automation;

	private ConfigurableGenome genome;
	private SimpleChromosome delayTimes;
	private SimpleChromosome delayLevels;
	private SimpleChromosome delayAutomation;
	private int channels;
	private List<Integer> wetChannels;

	private DoubleSupplier beatDuration;
	private int sampleRate;

	public EfxManager(ConfigurableGenome genome, int channels,
					  AutomationManager automation,
					  DoubleSupplier beatDuration, int sampleRate) {
		this.genome = genome;
		this.channels = channels;
		this.wetChannels = new ArrayList<>();
		IntStream.range(0, channels).forEach(this.wetChannels::add);

		this.automation = automation;

		this.beatDuration = beatDuration;
		this.sampleRate = sampleRate;

		init();
	}

	protected void init() {
		double choices[] = IntStream.range(0, 5)
				.mapToDouble(i -> Math.pow(2, i - 2))
				.mapToObj(d -> List.of(d, 1.5 * d))
				.flatMap(List::stream)
				.mapToDouble(d -> d)
				.toArray();

		PackedCollection<?> c = new PackedCollection<>(choices.length);
		c.setMem(choices);

		delayTimes = genome.addSimpleChromosome(1);
		IntStream.range(0, channels).forEach(i -> delayTimes.addChoiceGene(c));

		delayLevels = genome.addSimpleChromosome(4);
		IntStream.range(0, channels).forEach(i -> {
			SimpleGene g = delayLevels.addGene();
			if (maxWet != 1.0) g.setTransform(0, p -> multiply(p, c(maxWet)));
			if (maxFeedback != 1.0) g.setTransform(1, p -> multiply(p, c(maxFeedback)));
		});

		delayAutomation = genome.addSimpleChromosome(AutomationManager.GENE_LENGTH);
		IntStream.range(0, channels).forEach(i -> delayAutomation.addGene());
	}

	public List<Integer> getWetChannels() { return wetChannels; }
	public void setWetChannels(List<Integer> wetChannels) { this.wetChannels = wetChannels; }

	public CellList apply(int channel, Producer<PackedCollection<?>> audio, double totalDuration, OperationList setup) {
		if (!enableEfx || !wetChannels.contains(channel)) {
			return createCells(audio, totalDuration);
		}

		CellList wet = createCells(applyFilter(channel, audio, setup), totalDuration)
						.map(fc(i -> delayLevels.valueAt(channel, 0)));
		CellList dry = createCells(audio, totalDuration);

		Producer<PackedCollection<?>> delay = delayTimes.valueAt(channel, 0).getResultant(c(1.0));

		CellList delays = IntStream.range(0, 1)
				.mapToObj(i -> new AdjustableDelayCell(sampleRate,
						scalar(shape(1), multiply(c(beatDuration.getAsDouble()), delay), 0),
						scalar(1.0)))
				.collect(CellList.collector());

		IntFunction<Cell<PackedCollection<?>>> auto =
				enableAutomation ?
						fc(i -> in -> {
							Producer<PackedCollection<?>> value = automation.getAggregatedValue(delayAutomation.valueAt(channel), null, 0.0);
							value = c(0.5).multiply(c(1.0).add(value));
							return multiply(in, value);
						}) :
						fi();

		wet = wet.m(auto, delays)
				.mself(fi(), i -> g(delayLevels.valueAt(channel, 1)))
				.sum();

		CellList cells = cells(wet, dry).sum();
		return cells;
	}

	protected CellList createCells(Producer<PackedCollection<?>> audio, double totalDuration) {
		return w(PolymorphicAudioData.supply(PackedCollection.factory()),
				sampleRate, shape(audio).getTotalSize(),
				null, c(totalDuration), traverse(0, audio));
	}

	protected Producer<PackedCollection<?>> applyFilter(int channel, Producer<PackedCollection<?>> audio, OperationList setup) {
		PackedCollection<?> destination = PackedCollection.factory().apply(shape(audio).getTotalSize());

		Producer<PackedCollection<?>> decision =
				delayLevels.valueAt(channel, 2).getResultant(c(1.0));
		Producer<PackedCollection<?>> cutoff = c(20000)
				.multiply(delayLevels.valueAt(channel, 3).getResultant(c(1.0)));

		CollectionProducer<PackedCollection<?>> lpCoefficients =
				lowPassCoefficients(cutoff, sampleRate, filterOrder)
						.reshape(1, filterOrder + 1);
		CollectionProducer<PackedCollection<?>> hpCoefficients =
				highPassCoefficients(cutoff, sampleRate, filterOrder)
						.reshape(1, filterOrder + 1);

		Producer<PackedCollection<?>> coefficients = choice(2,
				shape(filterOrder + 1), decision,
				concat(shape(2, filterOrder + 1), hpCoefficients, lpCoefficients));

		setup.add(a("efxFilter", cp(destination.each()),
					MultiOrderFilter.create(audio, coefficients)));
		return cp(destination);
	}
}
