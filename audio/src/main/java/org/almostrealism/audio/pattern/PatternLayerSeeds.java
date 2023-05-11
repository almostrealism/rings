/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.pattern;

import org.almostrealism.audio.data.ParameterSet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PatternLayerSeeds {
	/**
	 * When units is higher than 1, the seed process can easily produce
	 * more notes than the specified count. There are ways this could be
	 * handled directly via the positional function the determines note
	 * presence/absence, but trimming is a simpler solution. While the
	 * number of generated notes is above count, alternating notes are
	 * removed.
	 */
	public static boolean enableTrimming = true;

	private double position;
	private double scale;
	private double granularity;
	private double bias;

	private PatternElementFactory factory;
	private ParameterSet params;

	public PatternLayerSeeds() {
		this(0, 1.0, 1.0, 0.0, null, null);
	}

	public PatternLayerSeeds(double position, double scale, double granularity, double bias, PatternElementFactory factory, ParameterSet params) {
		this.position = position;
		this.scale = scale;
		this.granularity = granularity;
		this.bias = bias;
		this.factory = factory;
		this.params = params;
	}

	public double getPosition() {
		return position;
	}
	public void setPosition(double position) {
		this.position = position;
	}

	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getGranularity() {
		return granularity;
	}
	public void setGranularity(double granularity) {
		this.granularity = granularity;
	}

	public double getBias() { return bias; }
	public void setBias(double bias) { this.bias = bias; }

	public Stream<PatternLayer> generator(double offset, double duration, double bias, int chordDepth) {
		double count = Math.max(1.0, duration / granularity);

		List<PatternLayer> layers = IntStream.range(0, (int) count)
				.mapToObj(i ->
						factory.apply(null, position + offset + i * granularity, granularity, this.bias + bias, chordDepth, false, params).orElse(null))
				.filter(Objects::nonNull)
				.map(List::of)
				.map(PatternLayer::new)
				.collect(Collectors.toList());

		if (layers.size() <= 0 && (this.bias + bias) >= 1.0) {
			System.out.println("PatternLayerSeeds: No seeds generated, despite bias >= 1.0");
		}

		return layers.stream();
	}
}
