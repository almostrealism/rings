/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.sources.test;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.RescalingSourceAggregator;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.util.TestFeatures;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RescalingAggregationTests implements TestFeatures {
	private int sampleRate = OutputLine.sampleRate;

	@Test
	public void loadAggregated() throws IOException {
		WaveData data = WaveData.load(new File("Library/organ.wav"));
		PackedCollection<?> eq = data.aggregatedFft(true);
		log(eq.getShape());

		int total = 0;
		for (int i = 0; i < eq.getShape().length(0); i++) {
			if (eq.toDouble(i) > 0.1) {
				total++;
			}
		}

		log(total);
		Assert.assertTrue(total > 200);
	}

	@Test
	public void rescale() throws IOException {
		RescalingSourceAggregator aggregator = new RescalingSourceAggregator();

		WaveData input = WaveData.load(new File("Library/SN_Forever_Future.wav"));
		WaveData filter = WaveData.load(new File("Library/organ.wav"));

		Producer<PackedCollection<?>> aggregated = aggregator.aggregate(
				new BufferDetails(sampleRate, 28.0),
				null, null,
				cp(input.getCollection()),
				cp(filter.getCollection()));

		WaveData out = new WaveData(aggregated.get().evaluate(), sampleRate);
		out.save(new File("results/rescaling-aggregator.wav"));
	}
}