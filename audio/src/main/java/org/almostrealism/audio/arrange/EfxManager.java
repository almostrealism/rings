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
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;

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

		delayLevels = genome.addSimpleChromosome(2);
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

	public CellList apply(int channel, CellList cells) {
		if (!enableEfx || !wetChannels.contains(channel)) {
			return cells;
		}

		if (cells.size() != 1) {
			warn("CellList size is " + cells.size());
		}

		Producer<PackedCollection<?>> delay = delayTimes.valueAt(channel, 0).getResultant(c(1.0));

		CellList delays = IntStream.range(0, 1)
				.mapToObj(i -> new AdjustableDelayCell(sampleRate,
						scalar(shape(1), multiply(c(beatDuration.getAsDouble()), delay), 0),
						scalar(1.0)))
				.collect(CellList.collector());

		CellList branch[] = cells.branch(fc(i -> delayLevels.valueAt(channel, 0)),
										fc(i -> sf(1.0)));
		CellList wet = branch[0];
		CellList dry = branch[1];

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

		cells = cells(wet, dry).sum();
		return cells;
	}
}
