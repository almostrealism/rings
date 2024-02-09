/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.audio.optimize.test;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.optimize.WavCellChromosome;
import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.time.TemporalList;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

public class WaveCellChromosomeExpansionTest implements CellFeatures, TestFeatures {
	@Test
	public void expand() {
		TimeCell clock = new TimeCell();

		Chromosome<PackedCollection<?>> input = c(g(0.5, 0.7), g(1.0, 0.9), g(1.5, 1.1));
		WavCellChromosome expansion = new WavCellChromosome(input, 2, OutputLine.sampleRate);
		expansion.setGlobalTime(clock);

		expansion.setFactor((params, in) -> {
			CollectionProducerComputation amp = c(params, 0);
			CollectionProducerComputation wavelength = c(params, 1);
			return sin(c(TWO_PI).divide(wavelength).multiply(in)).multiply(amp);
		});

		expansion.setTransform(0, g -> g.valueAt(0).getResultant(c(1.0)));
		expansion.setTransform(1, g -> g.valueAt(1).getResultant(c(10.0)));

		expansion.expand().get().run();

		TemporalFactor<PackedCollection<?>> factor = (TemporalFactor<PackedCollection<?>>) expansion.valueAt(1).valueAt(0);
		Evaluable<PackedCollection<?>> out = factor.getResultant(c(2.0)).get();

		TemporalList t = new TemporalList();
		t.add(clock);
		t.add(factor);
		t.iter(OutputLine.sampleRate).get().run();
		assertEquals(1.2855509652478203, out.evaluate().toDouble(0));
	}
}
