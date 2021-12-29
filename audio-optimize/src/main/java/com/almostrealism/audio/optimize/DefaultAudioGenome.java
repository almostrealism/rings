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

import io.almostrealism.code.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.audio.sources.PolynomialCell;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.breeding.AssignableGenome;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.CellularTemporalFactor;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.ScaleFactor;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultAudioGenome implements Genome<Scalar>, CellFeatures, Setup {
	public static final int GENERATORS = 0;
	public static final int VOLUME = 1;
	public static final int MAIN_FILTER_UP = 2;
	public static final int WET_IN = 3;
	public static final int PROCESSORS = 4;
	public static final int TRANSMISSION = 5;
	public static final int WET_OUT = 6;
	public static final int FX_FILTERS = 7;

	public static double defaultResonance = 0.1; // TODO
	private static double maxFrequency = 20000;

	private AssignableGenome data;
	private int sources, delayLayers, length;
	private int sampleRate;

	private AdjustmentChromosome mainFilterUpChromosome;
	private DelayChromosome delayChromosome;

	public DefaultAudioGenome(int sources, int delayLayers) {
		this(sources, delayLayers, OutputLine.sampleRate);
	}

	public DefaultAudioGenome(int sources, int delayLayers, int sampleRate) {
		this(sources, delayLayers, 8, sampleRate);
	}

	protected DefaultAudioGenome(int sources, int delayLayers, int length, int sampleRate) {
		this(new AssignableGenome(), sources, delayLayers, length, sampleRate);
	}

	private DefaultAudioGenome(AssignableGenome data, int sources, int delayLayers, int length, int sampleRate) {
		this.data = data;
		this.sources = sources;
		this.delayLayers = delayLayers;
		this.length = length;
		this.sampleRate = sampleRate;
		this.mainFilterUpChromosome = new AdjustmentChromosome(MAIN_FILTER_UP);
		this.delayChromosome = new DelayChromosome(PROCESSORS);
	}

	public void assignTo(Genome g) { data.assignTo(g); }

	@Override
	public Genome getHeadSubset() {
		return new DefaultAudioGenome(data, sources, delayLayers, length - 1, sampleRate);
	}

	@Override
	public Chromosome getLastChromosome() { return valueAt(length - 1); }

	@Override
	public int count() { return length; }

	@Override
	public Chromosome<Scalar> valueAt(int pos) {
		if (pos == GENERATORS) {
			return new GeneratorChromosome(pos);
		} else if (pos == MAIN_FILTER_UP) {
			return mainFilterUpChromosome;
		} else if (pos == PROCESSORS) {
			return delayChromosome;
		} else if (pos == FX_FILTERS) {
			return new FixedFilterChromosome(pos);
		} else {
			return data.valueAt(pos);
		}
	}

	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList();
		setup.add(mainFilterUpChromosome.expand());
		setup.add(delayChromosome.expand());
		return setup;
	}

	@Override
	public String toString() { return data.toString(); }

	public static double factorForRepeat(double beats) {
		return ((Math.log(beats) / Math.log(2)) / 16) + 0.5;
	}

	public static double factorForRepeatSpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForPeriodicFilterUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForPolyFilterUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForPolyFilterUpExponent(double exp) {
		return HeredityFeatures.getInstance().invertOneToInfinity(exp, 10, 1);
	}

	public static double factorForDelay(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForSpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForSpeedUpPercentage(double decimal) {
		return HeredityFeatures.getInstance().invertOneToInfinity(decimal, 10, 0.5);
	}

	public static double factorForSlowDownDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForSlowDownPercentage(double decimal) {
		return decimal;
	}

	public static double factorForPolySpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForPolySpeedUpExponent(double exp) {
		return HeredityFeatures.getInstance().invertOneToInfinity(exp, 10, 1);
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
		public int length() { return 4; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			if (pos < 2) {
				// Choice & Offset (0.0 to 1.0)
				return protein -> data.valueAt(chromosome, index, pos).getResultant(protein);
			} else {
				// Repeat
				/*
				ScalarBank time = timeline.getValue();

				KernelList values = new KernelList<Scalar>(ScalarBank::new, ScalarTable::new, p -> {
					ScalarProducer repeat = scalar(p, 0);
					ScalarProducer speedUpDuration = scalar(p, 1);

					ScalarProducer initial = pow(v(2.0), v(16).multiply(v(-0.5).add(repeat)));
					ScalarProducer speedUp = oneToInfinity(speedUpDuration, 3).multiply(60);

					Producer<Scalar> in = value(Scalar.class, 0);
					// return initial.multiply(pow(v(2.0), floor(speedUp.pow(-1.0).multiply(in))));
					return initial;
				}, 1, 2);
				values.setInput(time);

				Producer<Scalar> repeat = data.valueAt(chromosome, index, 2).getResultant(v(1.0));
				Producer<Scalar> speedUpDuration = data.valueAt(chromosome, index, 3).getResultant(v(1.0));
				values.setParameters(0, scalars(repeat, speedUpDuration));

				WavCell generator = new WavCell((ScalarBank) values.valueAt(0), sampleRate);
				generator.addSetup(values);

				Scalar v = new Scalar();
				return generator.toFactor(() -> v, DefaultAudioGenome.this::a, DefaultAudioGenome.this::scalarsMultiply);
				*/

				return protein ->
					pow(v(2.0), v(16).multiply(v(-0.5)
								.add(data.valueAt(chromosome, index, pos).getResultant(v(1.0)))))
							.multiply(protein);
			}
		}
	}

	protected class AdjustmentChromosome extends WavCellChromosomeExpansion {
		public AdjustmentChromosome(int index) {
			super(data.valueAt(index), data.length(index), 7, sampleRate);
			setTransform(0, g -> oneToInfinity(g.valueAt(0), 3.0).multiply(60.0));
			setTransform(1, g -> oneToInfinity(g.valueAt(1), 3.0).multiply(60.0));
			setTransform(2, g -> oneToInfinity(g.valueAt(2), 1.0).multiply(10.0));
			addFactor((p, in) -> {
				ScalarProducer periodicWavelength = scalar(p, 0);
				ScalarProducer periodicAmp = v(1.0);
				ScalarProducer polyWaveLength = scalar(p, 1);
				ScalarProducer polyExp = scalar(p, 2);

//				return sinw(in, periodicWavelength, periodicAmp).pow(2.0)
//						.multiply(polyWaveLength.pow(-1.0).multiply(in).pow(polyExp));
				return polyWaveLength.pow(-1.0).multiply(in).pow(polyExp);
			});
		}
	}

	protected class LegacyAdjustmentChromosome implements Chromosome<Scalar> {
		private final int index;

		public LegacyAdjustmentChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<Scalar> valueAt(int pos) {
			return new AdjustmentGene(index, pos);
		}
	}

	protected class AdjustmentGene implements Gene<Scalar> {
		private final int chromosome;
		private final int index;

		public AdjustmentGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			Producer<Scalar> periodicDuration = data.valueAt(chromosome, index, 0).getResultant(v(1.0));
			Producer<Scalar> polyDuration = data.valueAt(chromosome, index, 1).getResultant(v(1.0));
			Producer<Scalar> polyExponent = data.valueAt(chromosome, index, 2).getResultant(v(1.0));

			SineWaveCell periodicGenerator = new SineWaveCell();
			ScalarProducer periodicWavelength = oneToInfinity(periodicDuration, 3).multiply(60);
			ScalarProducer periodicAmp = v(1.0);
			periodicGenerator.setNoteLength(0);
			periodicGenerator.addSetup(periodicGenerator.setFreq(periodicWavelength.pow(-1.0)));
			periodicGenerator.addSetup(periodicGenerator.setAmplitude(periodicAmp));

			PolynomialCell polyGenerator = new PolynomialCell();
			ScalarProducer polyWaveLength = oneToInfinity(polyDuration, 3).multiply(60);
			ScalarProducer polyExp = oneToInfinity(polyExponent, 1).multiply(10);
			polyGenerator.addSetup(polyGenerator.setWaveLength(polyWaveLength));
			polyGenerator.addSetup(polyGenerator.setExponent(polyExp));

			Scalar periodic = new Scalar(0.0);
			CellularTemporalFactor<Scalar> periodicFactor =
					periodicGenerator.toFactor(() -> periodic, DefaultAudioGenome.this::a)
							.andThen(v -> v(1.0).add(pow(v, 2.0)));

			Scalar poly = new Scalar(0.0);
			CellularTemporalFactor<Scalar> polyFactor =
					polyGenerator.toFactor(() -> poly, DefaultAudioGenome.this::a);

			/*
			return new CombinedFactor<>(periodicFactor, polyFactor) {
				@Override
				public Producer<Scalar> getResultant(Producer<Scalar> value) {
					return scalarsMultiply(getA().getResultant(value), getB().getResultant(value));
				}
			};
			 */

			return polyFactor;
		}
	}

	protected class DelayChromosome extends WavCellChromosomeExpansion {
		public DelayChromosome(int index) {
			super(data.valueAt(index), data.length(index), 7, sampleRate);
			setTransform(0, g -> oneToInfinity(g.valueAt(0).getResultant(v(1.0)), 3.0).multiply(v(60.0)));
			setTransform(1, g -> oneToInfinity(g.valueAt(1).getResultant(v(1.0)), 3.0).multiply(v(60.0)));
			setTransform(2, g -> oneToInfinity(g.valueAt(2).getResultant(v(1.0)), 0.5).multiply(v(10.0)));
			setTransform(3, g -> oneToInfinity(g.valueAt(3).getResultant(v(1.0)), 3.0).multiply(v(60.0)));
			setTransform(4, g -> g.valueAt(4).getResultant(v(1.0)));
			setTransform(5, g -> oneToInfinity(g.valueAt(5).getResultant(v(1.0)), 3.0).multiply(v(60.0)));
			setTransform(6, g -> oneToInfinity(g.valueAt(6).getResultant(v(1.0)), 1.0).multiply(v(10.0)));
			addFactor(g -> g.valueAt(0).getResultant(v(1.0)));
			addFactor((p, in) -> {
				ScalarProducer speedUpWavelength = scalar(p, 1);
				ScalarProducer speedUpAmp = scalar(p, 2);
				ScalarProducer slowDownWavelength = scalar(p, 3);
				ScalarProducer slowDownAmp = scalar(p, 4);
				ScalarProducer polySpeedUpWaveLength = scalar(p, 5);
				ScalarProducer polySpeedUpExp = scalar(p, 6);
				return v(1.0).add(sinw(in, speedUpWavelength, speedUpAmp).pow(2.0))
						.multiply(v(1.0).subtract(sinw(in, slowDownWavelength, slowDownAmp).pow(2.0)))
						.multiply(v(1.0).add(polySpeedUpWaveLength.pow(-1.0).multiply(in).pow(polySpeedUpExp)));
			});
		}
	}

	protected class FixedFilterChromosome implements Chromosome<Scalar> {
		private final int index;

		public FixedFilterChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<Scalar> valueAt(int pos) {
			return new FixedFilterGene(index, pos);
		}
	}

	protected class FixedFilterGene implements Gene<Scalar> {
		private final int chromosome;
		private final int index;

		public FixedFilterGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<Scalar> valueAt(int pos) {
			Producer<Scalar> lowFrequency = scalarsMultiply(v(maxFrequency), data.valueAt(chromosome, index, 0).getResultant(v(1.0)));
			Producer<Scalar> highFrequency = scalarsMultiply(v(maxFrequency), data.valueAt(chromosome, index, 1).getResultant(v(1.0)));
			return new AudioPassFilter(sampleRate, lowFrequency, v(defaultResonance), true)
					.andThen(new AudioPassFilter(sampleRate, highFrequency, v(defaultResonance), false));
		}
	}

	public static ChromosomeFactory<Scalar> generatorFactory(double offsetChoices[], double repeatChoices[],
															 double repeatSpeedUpDurationMin, double repeatSpeedUpDurationMax) {
		return new ChromosomeFactory<>() {
			private int genes;

			@Override
			public ChromosomeFactory<Scalar> setChromosomeSize(int genes, int factors) {
				this.genes = genes;
				return this;
			}

			@Override
			public Chromosome<Scalar> generateChromosome(double arg) {
				return IntStream.range(0, genes)
						.mapToObj(i -> IntStream.range(0, 4)
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
				} else if (factor == 3) {
					double min = factorForRepeatSpeedUpDuration(repeatSpeedUpDurationMin);
					double max = factorForRepeatSpeedUpDuration(repeatSpeedUpDurationMax);
					return min + Math.random() * (max - min);
				} else {
					throw new IllegalArgumentException();
				}
			}
		};
	}
}
