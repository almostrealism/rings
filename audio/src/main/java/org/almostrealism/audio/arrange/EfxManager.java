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
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EfxManager implements CellFeatures {
	public static double maxFeedback = 1.0;

	private ConfigurableGenome genome;
	private SimpleChromosome delayTimes;
	private SimpleChromosome delayFeedbacks;
	private int channels;
	private List<Integer> wetChannels;

	private DoubleSupplier beatDuration;
	private int sampleRate;

	public EfxManager(ConfigurableGenome genome, int channels, DoubleSupplier beatDuration, int sampleRate) {
		this.genome = genome;
		this.channels = channels;
		this.wetChannels = new ArrayList<>();
		IntStream.range(0, channels).forEach(this.wetChannels::add);

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

		delayFeedbacks = genome.addSimpleChromosome(1);
		IntStream.range(0, channels).forEach(i -> {
			SimpleGene g = delayFeedbacks.addGene();
			if (maxFeedback != 1.0) g.setTransform(p -> _multiply(p, c(maxFeedback)));
		});
	}

	public List<Integer> getWetChannels() { return wetChannels; }
	public void setWetChannels(List<Integer> wetChannels) { this.wetChannels = wetChannels; }

	public CellList apply(int channel, CellList cells) {
		if (!wetChannels.contains(channel)) {
			return cells;
		}

		Producer<PackedCollection<?>> delay = delayTimes.valueAt(channel, 0).getResultant(c(1.0));

		CellList delays = IntStream.range(0, 1)
				.mapToObj(i -> new AdjustableDelayCell(sampleRate,
						scalar(shape(1), _multiply(c(beatDuration.getAsDouble()), delay), 0),
						v(1.0)))
				.collect(CellList.collector());

		cells = cells.m(fi(), delays)
				.mself(fi(), i -> g(delayFeedbacks.valueAt(channel, 0)))
				.sum();

		return cells;
	}
}
