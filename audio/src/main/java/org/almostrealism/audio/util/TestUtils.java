/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.util;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Genome;

public class TestUtils {
	public static Genome genome(double x1a, double x2a, boolean adjust) {
		return genome(x1a, x2a, 0.5, 0.5, adjust);
	}

	public static Genome genome(double x1a, double x2a, double v1a, double v2a, boolean adjust) {
		ArrayListChromosome<Scalar> generators = new ArrayListChromosome<>();
		generators.add(new ArrayListGene<>(x1a));
		generators.add(new ArrayListGene<>(x2a));

		ArrayListChromosome<Scalar> volume = new ArrayListChromosome();
		volume.add(new ArrayListGene<>(v1a));
		volume.add(new ArrayListGene<>(v2a));

		ArrayListChromosome<Scalar> processing = new ArrayListChromosome();
		processing.add(new ArrayListGene<>(1.0, 0.2));
		processing.add(new ArrayListGene<>(1.0, 0.2));

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();
		transmission.add(new ArrayListGene<>(0.0, 1.0));
		transmission.add(new ArrayListGene<>(1.0, 0.0));

		ArrayListChromosome<Scalar> filters = new ArrayListChromosome();
		filters.add(new ArrayListGene<>(0.0, 1.0));
		filters.add(new ArrayListGene<>(0.0, 1.0));

		ArrayListChromosome<Scalar> a = new ArrayListChromosome();

		if (adjust) {
			a.add(new ArrayListGene<>(0.1, 0.0, 1.0));
			a.add(new ArrayListGene<>(0.1, 0.0, 1.0));
		} else {
			a.add(new ArrayListGene<>(0.0, 1.0, 1.0));
			a.add(new ArrayListGene<>(0.0, 1.0, 1.0));
		}

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(volume);
		genome.add(processing);
		genome.add(transmission);
		genome.add(filters);
		genome.add(a);
		return genome;
	}
}
