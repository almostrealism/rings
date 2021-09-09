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

package com.almostrealism.audio.optimize;

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.breeding.AssignableGenome;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.util.CodeFeatures;

public class SimpleOrganGenome implements Genome, CodeFeatures {
	private static double defaultResonance = 0.1; // TODO
	private static double maxFrequency = 20000;

	private static final int GENERATORS = 0;
	private static final int PROCESSORS = 1;
	private static final int TRANSMISSION = 2;
	private static final int FILTERS = 3;

	private AssignableGenome data;
	private int cells, length;
	private int sampleRate;

	public SimpleOrganGenome(int cells) {
		this(cells, OutputLine.sampleRate);
	}

	public SimpleOrganGenome(int cells, int sampleRate) {
		this(cells, 4, sampleRate);
	}

	protected SimpleOrganGenome(int cells, int length, int sampleRate) {
		this(new AssignableGenome(), cells, length, sampleRate);
	}

	private SimpleOrganGenome(AssignableGenome data, int cells, int length, int sampleRate) {
		this.data = data;
		this.cells = cells;
		this.length = length;
		this.sampleRate = sampleRate;
	}

	public void assignTo(Genome g) { data.assignTo(g); }

	@Override
	public Genome getHeadSubset() {
		return new SimpleOrganGenome(data, cells, length - 1, sampleRate);
	}

	@Override
	public Chromosome getLastChromosome() { return valueAt(length - 1); }

	@Override
	public int count() { return length; }

	@Override
	public Chromosome<?> valueAt(int pos) {
		if (pos == FILTERS) {
			return new FilterChromosome(pos);
		} else {
			return data.valueAt(pos);
		}
	}

	protected class FilterChromosome implements Chromosome<Scalar> {
		private final int index;

		public FilterChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<Scalar> valueAt(int pos) {
			return new FilterGene(index, pos);
		}
	}

	protected class FilterGene implements Gene<Scalar> {
		private final int chromosome;
		private final int index;

		public FilterGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			Producer<Scalar> lowFrequency = scalarsMultiply(v(maxFrequency), () -> args -> data.get(chromosome, index, 0));
			Producer<Scalar> highFrequency = scalarsMultiply(v(maxFrequency), () -> args -> data.get(chromosome, index, 1));
			return new AudioPassFilter(sampleRate, lowFrequency, v(defaultResonance), true)
					.andThen(new AudioPassFilter(sampleRate, highFrequency, v(defaultResonance), false));
		}
	}
}
