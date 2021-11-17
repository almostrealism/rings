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
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.breeding.AssignableGenome;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.CellularTemporalFactor;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.util.CodeFeatures;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleOrganGenome implements Genome<Scalar>, CellFeatures {
	private static double defaultResonance = 0.1; // TODO
	private static double maxFrequency = 20000;

	public static final int GENERATORS = 0;
	public static final int VOLUME = 1;
	public static final int PROCESSORS = 2;
	public static final int TRANSMISSION = 3;
	public static final int WET = 4;
	public static final int FILTERS = 5;

	private AssignableGenome data;
	private int cells, length;
	private int sampleRate;

	public SimpleOrganGenome(int cells) {
		this(cells, OutputLine.sampleRate);
	}

	public SimpleOrganGenome(int cells, int sampleRate) {
		this(cells, 6, sampleRate);
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
	public Chromosome<Scalar> valueAt(int pos) {
		if (pos == GENERATORS) {
			return new GeneratorChromosome(pos);
		} else if (pos == PROCESSORS) {
			return new DelayChromosome(pos);
		} else if (pos == FILTERS) {
			return new FilterChromosome(pos);
		} else {
			return data.valueAt(pos);
		}
	}

	@Override
	public String toString() {return data.toString(); }

	public static double factorForRepeat(double beats) {
		return ((Math.log(beats) / Math.log(2)) / 16) + 0.5;
	}

	public static double factorForDelay(double seconds) {
		return Math.pow(1 - (1 / ((seconds / 60) + 1)), 1.0 / 3);
	}

	public static double factorForFilterFrequency(double hertz) {
		return hertz / 20000;
	}

	protected class GeneratorChromosome implements Chromosome<Scalar> {
		private final int index;

		public GeneratorChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<Scalar> valueAt(int pos) {
			return new GeneratorGene(index, pos);
		}
	}

	protected class GeneratorGene implements Gene<Scalar> {
		private final int chromosome;
		private final int index;

		public GeneratorGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			if (pos < 2) {
				return protein -> scalarsMultiply(protein, () -> args -> data.get(chromosome, index, pos));
			} else {
				return protein ->
					pow(v(2.0), v(16).multiply(v(-0.5)
								.add(() -> args -> data.get(chromosome, index, pos))))
							.multiply(protein);
			}
		}
	}

	protected class DelayChromosome implements Chromosome<Scalar> {
		private final int index;

		public DelayChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<Scalar> valueAt(int pos) {
			return new DelayGene(index, pos);
		}
	}

	protected class DelayGene implements Gene<Scalar> {
		private final int chromosome;
		private final int index;

		public DelayGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			if (pos == 0) {
				return protein -> {
					ScalarProducer cube = pow(() -> args -> data.get(chromosome, index, pos), v(3));
					return cube.minus().add(1.0).pow(-1.0).subtract(1.0).multiply(protein);
				};
			} else {
				ScalarProducer pow = pow(() -> args -> data.get(chromosome, index, 1), v(1.0 / 19.0));
				Producer<Scalar> amp = () -> args -> data.get(chromosome, index, 2);

				SineWaveCell generator = new SineWaveCell();
				generator.setPhase(0.5);
				generator.setNoteLength(0);
				generator.addSetup(generator.setFreq(v(1.0).subtract(pow).multiply(v(30.0))));
				generator.addSetup(generator.setAmplitude(amp));

				Scalar a = new Scalar(0.0);
				return generator.toFactor(() -> a, SimpleOrganGenome.this::a).andThen(v -> scalarAdd(amp, v));
			}
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

	public static ChromosomeFactory<Scalar> generatorFactory() {
		double offsetChoices[] = IntStream.range(0, 7)
				.mapToDouble(i -> Math.pow(2, -i))
				.toArray();
		offsetChoices[0] = 0.0;

		double repeatChoices[] = IntStream.range(0, 13)
				.map(i -> i - 6)
				.mapToDouble(i -> Math.pow(2, i))
				.map(SimpleOrganGenome::factorForRepeat)
				.toArray();

		return new ChromosomeFactory<>() {
			private int genes, factors;

			@Override
			public ChromosomeFactory<Scalar> setChromosomeSize(int genes, int factors) {
				this.genes = genes;
				this.factors = factors;
				return this;
			}

			@Override
			public Chromosome<Scalar> generateChromosome(double arg) {
				return IntStream.range(0, genes)
						.mapToObj(i -> IntStream.range(0, 3)
								.mapToObj(j -> new ScaleFactor(value(i, j)))
								.collect(Collectors.toCollection(ArrayListGene::new)))
						.collect(Collectors.toCollection(ArrayListChromosome::new));
			}

			private double value(int gene, int factor) {
				if (factor == 0) {
					return Math.random();
				} else if (factor == 1) {
					return offsetChoices[(int) (Math.random() * offsetChoices.length)];
				} else if (factor == 2) {
					return repeatChoices[(int) (Math.random() * repeatChoices.length)];
				} else {
					throw new IllegalArgumentException();
				}
			}
		};
	}
}
