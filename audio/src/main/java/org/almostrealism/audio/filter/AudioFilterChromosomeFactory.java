/*
 * Copyright 2016 Michael Murray
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

package org.almostrealism.audio.filter;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.IdentityFactor;

public class AudioFilterChromosomeFactory implements ChromosomeFactory<Scalar> {
	private int genes, factors;

	@Override
	public ChromosomeFactory<Scalar> setChromosomeSize(int genes, int factors) {
		this.genes = genes;
		this.factors = factors;
		return this;
	}

	@Override
	public Chromosome<Scalar> generateChromosome(double arg) {
		return IdentityFactor.chromosome(genes, factors);
	}
}
