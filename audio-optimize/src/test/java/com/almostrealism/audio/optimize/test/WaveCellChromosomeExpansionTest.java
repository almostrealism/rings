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

package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.optimize.WavCellChromosomeExpansion;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

public class WaveCellChromosomeExpansionTest implements CellFeatures, TestFeatures {
	@Test
	public void expand() {
		Chromosome<Scalar> input = c(g(0.5, 0.7), g(1.0, 0.9), g(1.5, 1.1));

		WavCellChromosomeExpansion expansion = new WavCellChromosomeExpansion(input, 3, 2, OutputLine.sampleRate);

		expansion.addFactor((params, in) -> {
			ScalarProducer amp = scalar(params, 0);
			ScalarProducer wavelength = scalar(params, 1);
			return sin(v(TWO_PI).divide(wavelength).multiply(in)).multiply(amp);
		});

		expansion.setTransform(0, g -> g.valueAt(0).getResultant(v(1.0)));
		expansion.setTransform(1, g -> g.valueAt(1).getResultant(v(10.0)));

		expansion.expand().get().run();

		TemporalFactor<Scalar> factor = (TemporalFactor<Scalar>) expansion.valueAt(1).valueAt(0);
		Evaluable<Scalar> out = factor.getResultant(v(2.0)).get();
		factor.iter(OutputLine.sampleRate).get().run();
		assertEquals(1.2855509652478203, out.evaluate());
	}
}
