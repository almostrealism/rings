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

package org.almostrealism.audio.optimize;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.collect.CollectionProducerComputation;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.AssignableGenome;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.ChromosomeFactory;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.Genome;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.time.TemporalList;

import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultAudioGenome implements Genome<PackedCollection<?>>, Setup, CellFeatures {
	public static final int GENERATORS = 0;
	public static final int PARAMETERS = 1;
	public static final int VOLUME = 2;
	public static final int MAIN_FILTER_UP = 3;
	public static final int WET_IN = 4;
	public static final int PROCESSORS = 5;
	public static final int TRANSMISSION = 6;
	public static final int WET_OUT = 7;
	public static final int FX_FILTERS = 8;
	public static final int MASTER_FILTER_DOWN = 9;

	public static double defaultResonance = 0.1; // TODO
	private static double maxFrequency = 20000;

	private AssignableGenome data;
	private int sources, delayLayers, length;
	private int sampleRate;

	private Producer<Scalar> globalTime;

	private GeneratorChromosome generatorChromosome;
	private AdjustmentChromosome volumeChromosome;
	private AdjustmentChromosome mainFilterUpChromosome;
	private AdjustmentChromosome wetInChromosome;
	private DelayChromosome delayChromosome;
	private AdjustmentChromosome masterFilterDownChromosome;

	public DefaultAudioGenome(int sources, int delayLayers, Producer<Scalar> globalTime) {
		this(sources, delayLayers, OutputLine.sampleRate, globalTime);
	}

	public DefaultAudioGenome(int sources, int delayLayers, int sampleRate, Producer<Scalar> globalTime) {
		this(sources, delayLayers, 10, sampleRate, globalTime);
	}

	protected DefaultAudioGenome(int sources, int delayLayers, int length, int sampleRate, Producer<Scalar> globalTime) {
		this(new AssignableGenome(), sources, delayLayers, length, sampleRate, globalTime);
	}

	private DefaultAudioGenome(AssignableGenome data, int sources, int delayLayers, int length, int sampleRate, Producer<Scalar> globalTime) {
		this.data = data;
		this.sources = sources;
		this.delayLayers = delayLayers;
		this.length = length;
		this.sampleRate = sampleRate;
		this.globalTime = globalTime;
	}

	protected void initChromosomes() {
		if (generatorChromosome == null) generatorChromosome = new GeneratorChromosome(GENERATORS, globalTime);
		if (volumeChromosome == null) volumeChromosome = new AdjustmentChromosome(VOLUME, 0.0, 1.0, true, globalTime);
		if (mainFilterUpChromosome == null) mainFilterUpChromosome = new AdjustmentChromosome(MAIN_FILTER_UP, 0.0, 1.0, false, globalTime);
		if (wetInChromosome == null) wetInChromosome = new AdjustmentChromosome(WET_IN, 0.0, 1.0, false, globalTime);
		if (delayChromosome == null) delayChromosome = new DelayChromosome(PROCESSORS, globalTime);
		if (masterFilterDownChromosome == null) masterFilterDownChromosome = new AdjustmentChromosome(MASTER_FILTER_DOWN, 0.0, 1.0, false, globalTime);
	}

	public void assignTo(Genome g) {
		data.assignTo(g);
		initChromosomes();
	}

	@Override
	public Genome getHeadSubset() {
		DefaultAudioGenome g = new DefaultAudioGenome(data, sources, delayLayers, length - 1, sampleRate, globalTime);
		g.initChromosomes();
		return g;
	}

	@Override
	public Chromosome getLastChromosome() { return valueAt(length - 1); }

	@Override
	public int count() { return length; }

	@Override
	public Chromosome<PackedCollection<?>> valueAt(int pos) {
		if (pos == GENERATORS) {
			return generatorChromosome;
		} else if (pos == VOLUME) {
			return volumeChromosome;
		} else if (pos == MAIN_FILTER_UP) {
			return mainFilterUpChromosome;
		} else if (pos == WET_IN) {
			return wetInChromosome;
		} else if (pos == PROCESSORS) {
			return delayChromosome;
		} else if (pos == FX_FILTERS) {
			return new FixedFilterChromosome(pos);
		} else if (pos == MASTER_FILTER_DOWN) {
			return masterFilterDownChromosome;
		} else {
			return data.valueAt(pos);
		}
	}

	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList("DefaultAudioGenome Chromosome Expansions");
		setup.add(generatorChromosome.expand());
		setup.add(volumeChromosome.expand());
		setup.add(mainFilterUpChromosome.expand());
		setup.add(wetInChromosome.expand());
		setup.add(delayChromosome.expand());
		setup.add(masterFilterDownChromosome.expand());
		return setup;
	}

	public TemporalList getTemporals() {
		TemporalList temporals = new TemporalList();
		temporals.addAll(generatorChromosome.getTemporals());
		temporals.addAll(volumeChromosome.getTemporals());
		temporals.addAll(mainFilterUpChromosome.getTemporals());
		temporals.addAll(wetInChromosome.getTemporals());
		temporals.addAll(delayChromosome.getTemporals());
		temporals.addAll(masterFilterDownChromosome.getTemporals());
		return temporals;
	}

	@Override
	public String toString() { return data.toString(); }

	public static double valueForFactor(Factor<PackedCollection<?>> value) {
		if (value instanceof ScaleFactor) {
			return ((ScaleFactor) value).getScaleValue();
		} else {
			return value.getResultant(Ops.ops().c(1.0)).get().evaluate().toDouble(0);
		}
	}

	public static double valueForFactor(Factor<PackedCollection<?>> value, double exp, double multiplier) {
		if (value instanceof ScaleFactor) {
			return HeredityFeatures.getInstance().oneToInfinity(((ScaleFactor) value).getScaleValue(), exp) * multiplier;
		} else {
			double v = value.getResultant(Ops.ops().c(1.0)).get().evaluate().toDouble(0);
			return HeredityFeatures.getInstance().oneToInfinity(v, exp) * multiplier;
		}
	}

	public static double factorForRepeat(double beats) {
		return ((Math.log(beats) / Math.log(2)) / 16) + 0.5;
	}

	public static double[] repeatForFactor(Factor<PackedCollection<?>> f) {
		double v = 16 * (valueForFactor(f) - 0.5);

		if (v == 0) {
			return new double[] { 1.0, 1.0 };
		} else if (v > 0) {
			return new double[] { Math.pow(2.0, v), 1.0 };
		} else if (v < 0) {
			return new double[] { 1.0, Math.pow(2.0, -v) };
		}

		return null;
	}

	public static double factorForRepeatSpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double repeatSpeedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForPeriodicAdjustmentDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double factorForPolyAdjustmentDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double polyFilterUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForPolyAdjustmentExponent(double exp) {
		return HeredityFeatures.getInstance().invertOneToInfinity(exp, 10, 1);
	}

	public static double factorForAdjustmentInitial(double value) {
		return HeredityFeatures.getInstance().invertOneToInfinity(value, 10, 1);
	}

	public static double factorForAdjustmentOffset(double value) {
		return HeredityFeatures.getInstance().invertOneToInfinity(value, 60, 3);
	}

	public static double factorForDelay(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double delayForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForSpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double speedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForSpeedUpPercentage(double decimal) {
		return HeredityFeatures.getInstance().invertOneToInfinity(decimal, 10, 0.5);
	}

	public static double speedUpPercentageForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 0.5, 10);
	}

	public static double factorForSlowDownDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double slowDownDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForSlowDownPercentage(double decimal) {
		return decimal;
	}

	public static double slowDownPercentageForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f);
	}

	public static double factorForPolySpeedUpDuration(double seconds) {
		return HeredityFeatures.getInstance().invertOneToInfinity(seconds, 60, 3);
	}

	public static double polySpeedUpDurationForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 3, 60);
	}

	public static double factorForPolySpeedUpExponent(double exp) {
		return HeredityFeatures.getInstance().invertOneToInfinity(exp, 10, 1);
	}

	public static double polySpeedUpExponentForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f, 1, 10);
	}

	public static double factorForFilterFrequency(double hertz) {
		return hertz / 20000;
	}

	public static double filterFrequencyForFactor(Factor<PackedCollection<?>> f) {
		return valueForFactor(f) * 20000;
	}

	@Deprecated
	protected class GeneratorChromosome extends WavCellChromosomeExpansion {
		public GeneratorChromosome(int index, Producer<Scalar> globalTime) {
			super(data.valueAt(index), data.length(index), 4, sampleRate);
			setGlobalTime(globalTime);
			setTransform(0, g -> g.valueAt(0).getResultant(c(1.0)));
			setTransform(1, g -> g.valueAt(1).getResultant(c(1.0)));
			setTransform(2, g -> g.valueAt(2).getResultant(c(1.0)));
			setTransform(3, g -> oneToInfinity(g.valueAt(3), 3.0).multiply(c(60.0)));
			addFactor(g -> g.valueAt(0).getResultant(c(1.0)));
			addFactor(g -> g.valueAt(1).getResultant(c(1.0)));
			addFactor((p, in) -> {
				CollectionProducerComputation repeat = c(p, 2);
				CollectionProducerComputation speedUpDuration = c(p, 3);

				CollectionProducerComputation initial = pow(c(2.0), c(16).multiply(c(-0.5).add(repeat)));

				return initial.divide(pow(c(2.0), floor(speedUpDuration.pow(c(-1.0)).multiply(in))));
				// return initial;
			});
		}
	}

	@Deprecated
	public class AdjustmentChromosome extends WavCellChromosomeExpansion {
		private boolean relative;

		public AdjustmentChromosome(int index, double min, double max, boolean relative, Producer<Scalar> globalTime) {
			super(data.valueAt(index), data.length(index), 6, sampleRate);
			setGlobalTime(globalTime);
			this.relative = relative;
			setTransform(0, g -> oneToInfinity(g.valueAt(0), 3.0).multiply(c(60.0)));
			setTransform(1, g -> oneToInfinity(g.valueAt(1), 3.0).multiply(c(60.0)));
			setTransform(2, g -> oneToInfinity(g.valueAt(2), 1.0).multiply(c(10.0)));
			setTransform(3, g -> oneToInfinity(g.valueAt(3), 1.0).multiply(c(10.0)));
			setTransform(4, g -> g.valueAt(4).getResultant(c(1.0)));
			setTransform(5, g -> oneToInfinity(g.valueAt(5), 3.0).multiply(c(60.0)));
			addFactor((p, in) -> {
				CollectionProducerComputation periodicWavelength = c(p, 0);
				CollectionProducerComputation periodicAmp = c(1.0);
				CollectionProducerComputation polyWaveLength = c(p, 1);
				CollectionProducerComputation polyExp = c(p, 2);
				CollectionProducerComputation initial = c(p, 3);
				CollectionProducerComputation scale = c(p, 4);
				CollectionProducerComputation offset = c(p, 5);

//				return sinw(in, periodicWavelength, periodicAmp).pow(2.0)
//						.multiply(polyWaveLength.pow(-1.0).multiply(in).pow(polyExp));

				if (relative) scale = scale.multiply(initial);
				CollectionProducerComputation pos = subtract(in, offset);
				return _bound(pos._greaterThan(c(0.0), polyWaveLength.pow(c(-1.0)).multiply(pos).pow(polyExp).multiply(scale).add(initial), initial), min, max);
//				return bound(polyWaveLength.pow(-1.0).multiply(pos).pow(polyExp).multiply(scale).add(initial), min, max);
			});
		}
	}

	public class DelayChromosome extends WavCellChromosomeExpansion {
		public DelayChromosome(int index, Producer<Scalar> globalTime) {
			super(data.valueAt(index), data.length(index), 7, sampleRate);
			setGlobalTime(globalTime);
			setTransform(0, g -> oneToInfinity(g.valueAt(0).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
			setTransform(1, g -> oneToInfinity(g.valueAt(1).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
			setTransform(2, g -> oneToInfinity(g.valueAt(2).getResultant(c(1.0)), 0.5).multiply(c(10.0)));
			setTransform(3, g -> oneToInfinity(g.valueAt(3).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
			setTransform(4, g -> g.valueAt(4).getResultant(c(1.0)));
			setTransform(5, g -> oneToInfinity(g.valueAt(5).getResultant(c(1.0)), 3.0).multiply(c(60.0)));
			setTransform(6, g -> oneToInfinity(g.valueAt(6).getResultant(c(1.0)), 1.0).multiply(c(10.0)));
			addFactor(g -> g.valueAt(0).getResultant(c(1.0)));
			addFactor((p, in) -> {
				CollectionProducerComputation speedUpWavelength = c(p, 1).multiply(c(2.0));
				CollectionProducerComputation speedUpAmp = c(p, 2);
				CollectionProducerComputation slowDownWavelength = c(p, 3).multiply(c(2.0));
				CollectionProducerComputation slowDownAmp = c(p, 4);
				CollectionProducerComputation polySpeedUpWaveLength = c(p, 5);
				CollectionProducerComputation polySpeedUpExp = c(p, 6);
				return c(1.0).add(_sinw(in, speedUpWavelength, speedUpAmp).pow(c(2.0)))
						.multiply(c(1.0).subtract(_sinw(in, slowDownWavelength, slowDownAmp).pow(c(2.0))))
						.multiply(c(1.0).add(polySpeedUpWaveLength.pow(c(-1.0)).multiply(in).pow(polySpeedUpExp)));
			});
		}
	}

	protected class FixedFilterChromosome implements Chromosome<PackedCollection<?>> {
		private final int index;

		public FixedFilterChromosome(int index) {
			this.index = index;
		}

		@Override
		public int length() {
			return data.length(index);
		}

		@Override
		public Gene<PackedCollection<?>> valueAt(int pos) {
			return new FixedFilterGene(index, pos);
		}
	}

	protected class FixedFilterGene implements Gene<PackedCollection<?>> {
		private final int chromosome;
		private final int index;

		public FixedFilterGene(int chromosome, int index) {
			this.chromosome = chromosome;
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<PackedCollection<?>> valueAt(int pos) {
			Producer<PackedCollection<?>> lowFrequency = multiply(c(maxFrequency), data.valueAt(chromosome, index, 0).getResultant(c(1.0)));
			Producer<PackedCollection<?>> highFrequency = multiply(c(maxFrequency), data.valueAt(chromosome, index, 1).getResultant(c(1.0)));
			return new AudioPassFilter(sampleRate, lowFrequency, v(defaultResonance), true)
					.andThen(new AudioPassFilter(sampleRate, highFrequency, v(defaultResonance), false));
		}
	}

	public static ChromosomeFactory<PackedCollection<?>> generatorFactory(IntToDoubleFunction choiceMin, IntToDoubleFunction choiceMax,
																	   double offsetChoices[], double repeatChoices[],
																	   double repeatSpeedUpDurationMin, double repeatSpeedUpDurationMax) {
		return new ChromosomeFactory<>() {
			private int genes;

			@Override
			public ChromosomeFactory<PackedCollection<?>> setChromosomeSize(int genes, int factors) {
				this.genes = genes;
				return this;
			}

			@Override
			public Chromosome<PackedCollection<?>> generateChromosome(double arg) {
				return IntStream.range(0, genes)
						.mapToObj(i -> IntStream.range(0, 4)
								.mapToObj(j -> new ScaleFactor(value(i, j)))
								.collect(Collectors.toCollection(ArrayListGene::new)))
						.collect(Collectors.toCollection(() -> new ArrayListChromosome<>()));
			}

			private double value(int gene, int factor) {
				if (factor == 0) {
					double min = choiceMin.applyAsDouble(gene);
					double max = choiceMax.applyAsDouble(gene);
					return min + Math.random() * (max - min);
				} else if (factor == 1) {
					return offsetChoices[(int) (Math.random() * offsetChoices.length)];
				} else if (factor == 2) {
					return factorForRepeat(repeatChoices[(int) (Math.random() * repeatChoices.length)]);
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
