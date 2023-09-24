/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.optimize.AdjustmentChromosome;
import org.almostrealism.audio.optimize.DelayChromosome;
import org.almostrealism.audio.optimize.FixedFilterChromosome;
import org.almostrealism.audio.optimize.OptimizeFactorFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.PassThroughCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.time.TemporalList;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class MixdownManager implements Setup, CellFeatures, OptimizeFactorFeatures {
	public static boolean enableAdjustmentChromosome = true;

	private TimeCell clock;
	private int sampleRate;

	private AdjustmentChromosome volume;
	private AdjustmentChromosome mainFilterUp;
	private AdjustmentChromosome wetIn;
	private SimpleChromosome wetInSimple;
	private SimpleChromosome transmission;
	private SimpleChromosome wetOut;
	private SimpleChromosome delay;
	private DelayChromosome delayDynamics;
	private FixedFilterChromosome wetFilter;
	private AdjustmentChromosome mainFilterDown;

	public MixdownManager(ConfigurableGenome genome, int channels, int delayLayers,
						  TimeCell clock, int sampleRate) {
		this.clock = clock;
		this.sampleRate = sampleRate;

		SimpleChromosome v = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> v.addGene());
		this.volume = new AdjustmentChromosome(v, 0.0, 1.0, true, sampleRate);
		this.volume.setGlobalTime(clock.frame());

		SimpleChromosome fup = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> fup.addGene());
		this.mainFilterUp = new AdjustmentChromosome(fup, 0.0, 1.0, false, sampleRate);
		this.mainFilterUp.setGlobalTime(clock.frame());

		SimpleChromosome w = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> w.addGene());
		this.wetIn = new AdjustmentChromosome(w, 0.0, 1.0, false, sampleRate);
		this.wetIn.setGlobalTime(clock.frame());

		this.wetInSimple = genome.addSimpleChromosome(6);
		IntStream.range(0, channels).forEach(i -> {
			SimpleGene g = wetInSimple.addGene();
			g.setTransform(0, p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
			g.setTransform(1, p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
			g.setTransform(2, p -> oneToInfinity(p, 1.0).multiply(c(10.0)));
			g.setTransform(3, p -> oneToInfinity(p, 1.0).multiply(c(10.0)));
			g.setTransform(4, p -> p);
			g.setTransform(5, p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
		});

		this.transmission = genome.addSimpleChromosome(delayLayers);
		IntStream.range(0, delayLayers).forEach(i -> transmission.addGene());

		this.wetOut = genome.addSimpleChromosome(delayLayers);
		this.wetOut.addGene();

		this.delay = genome.addSimpleChromosome(1);
		IntStream.range(0, delayLayers).forEach(i -> {
			SimpleGene g = delay.addGene();
			g.setTransform(p -> oneToInfinity(p, 3.0).multiply(c(60.0)));
		});

		SimpleChromosome d = genome.addSimpleChromosome(DelayChromosome.SIZE);
		IntStream.range(0, delayLayers).forEach(i -> d.addGene());
		this.delayDynamics = new DelayChromosome(d, sampleRate);
		this.delayDynamics.setGlobalTime(clock.frame());

		SimpleChromosome wf = genome.addSimpleChromosome(FixedFilterChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> wf.addGene());
		this.wetFilter = new FixedFilterChromosome(wf, sampleRate);

		SimpleChromosome fdown = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> fdown.addGene());
		this.mainFilterDown = new AdjustmentChromosome(fdown, 0.0, 1.0, false, sampleRate);
		this.mainFilterDown.setGlobalTime(clock.frame());

		initRanges(new Configuration(channels), delayLayers);
	}

	public void initRanges(Configuration config, int delayLayers) {
		volume.setPeriodicDurationRange(config.periodicVolumeDurationMin, config.periodicVolumeDurationMax);
		volume.setOverallDurationRange(config.overallVolumeDurationMin, config.overallVolumeDurationMax);
		volume.setOverallInitialRange(config.minVolumeValue, config.maxVolumeValue);
		volume.setOverallScaleRange(-1.0, -1.0);
		volume.setOverallExponentRange(config.overallVolumeExponentMin, config.overallVolumeExponentMax);
		volume.setOverallOffsetRange(config.overallVolumeOffsetMin, config.overallVolumeOffsetMax);

		mainFilterUp.setPeriodicDurationRange(config.periodicFilterUpDurationMin, config.periodicFilterUpDurationMax);
		mainFilterUp.setOverallDurationRange(config.overallFilterUpDurationMin, config.overallFilterUpDurationMax);
		mainFilterUp.setOverallInitialRange(0, 0);
		mainFilterUp.setOverallScaleRange(1.0, 1.0);
		mainFilterUp.setOverallExponentRange(config.overallFilterUpExponentMin, config.overallFilterUpExponentMax);
		mainFilterUp.setOverallOffsetRange(config.overallFilterUpOffsetMin, config.overallFilterUpOffsetMax);

		wetIn.setPeriodicDurationRange(config.periodicWetInDurationMin, config.periodicWetInDurationMax);
		wetIn.setOverallDurationRange(config.overallWetInDurationMin, config.overallWetInDurationMax);
		wetIn.setOverallExponentRange(config.overallWetInExponentMin, config.overallWetInExponentMax);
		wetIn.setOverallInitialRange(0, 0);
		wetIn.setOverallScaleRange(1.0, 1.0);
		wetIn.setOverallOffsetRange(config.overallWetInOffsetMin, config.overallWetInOffsetMax);

		wetInSimple.setParameterRange(0,
				factorForPeriodicAdjustmentDuration(config.periodicWetInDurationMin),
				factorForPeriodicAdjustmentDuration(config.periodicWetInDurationMax));
		wetInSimple.setParameterRange(1,
				factorForPolyAdjustmentDuration(config.overallWetInDurationMin),
				factorForPolyAdjustmentDuration(config.overallWetInDurationMax));
		wetInSimple.setParameterRange(2,
				factorForPolyAdjustmentExponent(config.overallWetInExponentMin),
				factorForPolyAdjustmentExponent(config.overallWetInExponentMax));
		wetInSimple.setParameterRange(3,
				factorForAdjustmentInitial(0),
				factorForAdjustmentInitial(0));
		wetInSimple.setParameterRange(4, 1.0, 1.0);
		wetInSimple.setParameterRange(5,
				factorForAdjustmentOffset(config.overallWetInOffsetMin),
				factorForAdjustmentOffset(config.overallWetInOffsetMax));

		IntStream.range(0, delayLayers).forEach(i -> transmission.setParameterRange(i, config.minTransmission, config.maxTransmission));

		IntStream.range(0, delayLayers).forEach(i -> wetOut.setParameterRange(i, config.minWetOut, config.maxWetOut));

		delay.setParameterRange(0, factorForDelay(config.minDelay), factorForDelay(config.maxDelay));

		delayDynamics.setDelayRange(config.minDelay, config.maxDelay);
		delayDynamics.setPeriodicSpeedUpDurationRange(config.periodicSpeedUpDurationMin, config.periodicSpeedUpDurationMax);
		delayDynamics.setPeriodicSpeedUpPercentageRange(config.periodicSpeedUpPercentageMin, config.periodicSpeedUpPercentageMax);
		delayDynamics.setPeriodicSlowDownDurationRange(config.periodicSlowDownDurationMin, config.periodicSlowDownDurationMax);
		delayDynamics.setPeriodicSlowDownPercentageRange(config.periodicSlowDownPercentageMin, config.periodicSlowDownPercentageMax);
		delayDynamics.setOverallSpeedUpDurationRange(config.overallSpeedUpDurationMin, config.overallSpeedUpDurationMax);
		delayDynamics.setOverallSpeedUpExponentRange(config.overallSpeedUpExponentMin, config.overallSpeedUpExponentMax);

		wetFilter.setHighPassRange(config.minHighPass, config.maxHighPass);
		wetFilter.setLowPassRange(config.minLowPass, config.maxLowPass);

		mainFilterDown.setPeriodicDurationRange(config.periodicMasterFilterDownDurationMin, config.periodicMasterFilterDownDurationMax);
		mainFilterDown.setOverallDurationRange(config.overallMasterFilterDownDurationMin, config.overallMasterFilterDownDurationMax);
		mainFilterDown.setOverallInitialRange(1.0, 1.0);
		mainFilterDown.setOverallScaleRange(-1.0, -1.0);
		mainFilterDown.setOverallExponentRange(config.overallMasterFilterDownExponentMin, config.overallMasterFilterDownExponentMax);
		mainFilterDown.setOverallOffsetRange(config.overallMasterFilterDownOffsetMin, config.overallMasterFilterDownOffsetMax);
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList();
		setup.add(volume.expand());
		if (AudioScene.enableMainFilterUp) setup.add(mainFilterUp.expand());
		if (enableAdjustmentChromosome) setup.add(wetIn.expand());
		if (!AudioScene.enableSourcesOnly) setup.add(delayDynamics.expand());
		if (!AudioScene.enableSourcesOnly) setup.add(mainFilterDown.expand());
		return setup;
	}

	public CellList cells(CellList sources,
						  List<? extends Receptor<PackedCollection<?>>> measures,
						  List<? extends Receptor<PackedCollection<?>>> stems,
						  Receptor<PackedCollection<?>> output) {
		CellList cells = sources;

		if (AudioScene.enableMainFilterUp) {
			// Apply dynamic high pass filters
			cells = cells.map(fc(i -> {
				TemporalFactor<PackedCollection<?>> f = (TemporalFactor<PackedCollection<?>>) mainFilterUp.valueAt(i, 0);
				return hp(scalar(20000).multiply(f.getResultant(c(1.0))), v(FixedFilterChromosome.defaultResonance));
			}));
		}

		TemporalList temporals = new TemporalList();
		temporals.addAll(volume.getTemporals());
		if (AudioScene.enableMainFilterUp) temporals.addAll(mainFilterUp.getTemporals());
		if (enableAdjustmentChromosome) temporals.addAll(wetIn.getTemporals());
		if (!AudioScene.enableSourcesOnly) temporals.addAll(delayDynamics.getTemporals());
		if (!AudioScene.enableSourcesOnly) temporals.addAll(mainFilterDown.getTemporals());

		cells = cells.addRequirements(temporals.toArray(TemporalFactor[]::new));

		if (AudioScene.enableSourcesOnly) {
			return cells
					.map(fc(i -> factor(volume.valueAt(i, 0))))
					.sum().map(fc(i -> sf(0.2))).map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
		}

		if (AudioScene.enableMixdown)
			cells = cells.mixdown(AudioScene.mixdownDuration);

		// Volume adjustment
		CellList branch[] = cells.branch(
				fc(i -> factor(volume.valueAt(i, 0))),
				AudioScene.enableEfxFilters ?
						fc(i -> factor(volume.valueAt(i, 0)).andThen(wetFilter.valueAt(i, 0))) :
						fc(i -> factor(volume.valueAt(i, 0))));

		CellList main = branch[0];
		CellList efx = branch[1];

		if (stems != null && !stems.isEmpty()) {
			main = main.branch(i -> new ReceptorCell<>(stems.get(i)),
								i -> new PassThroughCell<>())[1];
		}

		// Sum the main layer
		main = main.sum();

		if (AudioScene.enableEfx) {
			int delayLayers = delayDynamics.length();
			CellList delays = IntStream.range(0, delayLayers)
					.mapToObj(i -> new AdjustableDelayCell(OutputLine.sampleRate,
							toScalar(delay.valueAt(i, 0).getResultant(c(1.0))),
							toScalar(delayDynamics.valueAt(i, 0).getResultant(c(1.0)))))
					.collect(CellList.collector());

			IntFunction<Gene<PackedCollection<?>>> tg;

			if (enableAdjustmentChromosome) {
				// Route each line to each delay layer
				tg = i -> delayGene(delayLayers, wetIn.valueAt(i));
			} else {
				tg = i -> delayGene(delayLayers, new Gene<>() {
					@Override
					public int length() { return 1; }

					@Override
					public Factor<PackedCollection<?>> valueAt(int pos) {
						return in ->
								multiply(adjustment(
								wetInSimple.valueAt(i, 0).getResultant(c(1.0)),
								wetInSimple.valueAt(i, 1).getResultant(c(1.0)),
								wetInSimple.valueAt(i, 2).getResultant(c(1.0)),
								wetInSimple.valueAt(i, 3).getResultant(c(1.0)),
								wetInSimple.valueAt(i, 4).getResultant(c(1.0)),
								wetInSimple.valueAt(i, 5).getResultant(c(1.0)),
								clock.time(sampleRate), 0.0, 1.0, false), in);
					}
				});
			}

			// Route each line to each delay layer
			efx = efx.m(fi(), delays, tg)
					// Feedback grid
					.mself(fi(), transmission, fc(wetOut.valueAt(0)))
					.sum();

			if (AudioScene.disableClean) {
				efx.get(0).setReceptor(Receptor.to(output, measures.get(0), measures.get(1)));
				return efx;
			} else {
				if (stems != null && !stems.isEmpty()) {
					// Mix efx with main, measure #2, and the stem channel
//					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1), stems.get(sources.size())));
					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));
				} else {
					// Mix efx with main and measure #2
					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));
				}

				if (AudioScene.enableMasterFilterDown) {
					// Apply dynamic low pass filter
					main = main.map(fc(i -> {
						TemporalFactor<PackedCollection<?>> f = (TemporalFactor<PackedCollection<?>>) mainFilterDown.valueAt(i, 0);
						return lp(scalar(20000).multiply(f.getResultant(c(1.0))), v(FixedFilterChromosome.defaultResonance));
					}));
				}

				// Deliver main to the output and measure #1
				main = main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0))));

				return cells(main, efx);
			}
		} else {
			// Deliver main to the output and measure #1 and #2
			return main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
		}
	}


	/**
	 * This method wraps the specified {@link Factor} to prevent it from
	 * being detected as Temporal by {@link org.almostrealism.graph.FilteredCell}s
	 * that would proceed to invoke the {@link org.almostrealism.time.Temporal#tick()} operation.
	 * This is not a good solution, and this process needs to be reworked, so
	 * it is clear who bears the responsibility for invoking {@link org.almostrealism.time.Temporal#tick()}
	 * and it doesn't get invoked multiple times.
	 */
	private Factor<PackedCollection<?>> factor(Factor<PackedCollection<?>> f) {
		return v -> f.getResultant(v);
	}

	/**
	 * Create a {@link Gene} for routing delays.
	 * The current implementation delivers audio to
	 * the first delay based on the wet level, and
	 * delivers nothing to the others.
	 */
	private Gene<PackedCollection<?>> delayGene(int delays, Gene<PackedCollection<?>> wet) {
		ArrayListGene<PackedCollection<?>> gene = new ArrayListGene<>();

		if (AudioScene.enableWetInAdjustment) {
			gene.add(factor(wet.valueAt(0)));
		} else {
			gene.add(p -> c(0.2).multiply(p));
		}

		IntStream.range(0, delays - 1).forEach(i -> gene.add(p -> c(0.0)));
		return gene;
	}

	public static class Configuration implements OptimizeFactorFeatures {
		public IntToDoubleFunction minChoice, maxChoice;
		public double minChoiceValue, maxChoiceValue;

		public double repeatSpeedUpDurationMin, repeatSpeedUpDurationMax;

		public IntToDoubleFunction minX, maxX;
		public IntToDoubleFunction minY, maxY;
		public IntToDoubleFunction minZ, maxZ;
		public double minXValue, maxXValue;
		public double minYValue, maxYValue;
		public double minZValue, maxZValue;

		public IntToDoubleFunction minVolume, maxVolume;
		public double minVolumeValue, maxVolumeValue;
		public double periodicVolumeDurationMin, periodicVolumeDurationMax;
		public double overallVolumeDurationMin, overallVolumeDurationMax;
		public double overallVolumeExponentMin, overallVolumeExponentMax;
		public double overallVolumeOffsetMin, overallVolumeOffsetMax;

		public double periodicFilterUpDurationMin, periodicFilterUpDurationMax;
		public double overallFilterUpDurationMin, overallFilterUpDurationMax;
		public double overallFilterUpExponentMin, overallFilterUpExponentMax;
		public double overallFilterUpOffsetMin, overallFilterUpOffsetMax;

		public double minTransmission, maxTransmission;
		public double minDelay, maxDelay;

		public double periodicSpeedUpDurationMin, periodicSpeedUpDurationMax;
		public double periodicSpeedUpPercentageMin, periodicSpeedUpPercentageMax;
		public double periodicSlowDownDurationMin, periodicSlowDownDurationMax;
		public double periodicSlowDownPercentageMin, periodicSlowDownPercentageMax;
		public double overallSpeedUpDurationMin, overallSpeedUpDurationMax;
		public double overallSpeedUpExponentMin, overallSpeedUpExponentMax;

		public double periodicWetInDurationMin, periodicWetInDurationMax;
		public double overallWetInDurationMin, overallWetInDurationMax;
		public double overallWetInExponentMin, overallWetInExponentMax;
		public double overallWetInOffsetMin, overallWetInOffsetMax;

		public double minWetOut, maxWetOut;
		public double minHighPass, maxHighPass;
		public double minLowPass, maxLowPass;

		public double periodicMasterFilterDownDurationMin, periodicMasterFilterDownDurationMax;
		public double overallMasterFilterDownDurationMin, overallMasterFilterDownDurationMax;
		public double overallMasterFilterDownExponentMin, overallMasterFilterDownExponentMax;
		public double overallMasterFilterDownOffsetMin, overallMasterFilterDownOffsetMax;

		public double offsetChoices[];
		public double repeatChoices[];

		public Configuration() { this(1); }

		public Configuration(int scale) {
			double offset = 30;
			double duration = 0;

			minChoiceValue = 0.0;
			maxChoiceValue = 1.0;
			repeatSpeedUpDurationMin = 5.0;
			repeatSpeedUpDurationMax = 60.0;

			minVolumeValue = 2.0 / scale;
			maxVolumeValue = 2.0 / scale;
			periodicVolumeDurationMin = 0.5;
			periodicVolumeDurationMax = 180;
//			overallVolumeDurationMin = 60;
//			overallVolumeDurationMax = 240;
			overallVolumeDurationMin = duration + 5.0;
			overallVolumeDurationMax = duration + 30.0;
			overallVolumeExponentMin = 1;
			overallVolumeExponentMax = 1;
			overallVolumeOffsetMin = offset + 25.0;
			overallVolumeOffsetMax = offset + 45.0;

			periodicFilterUpDurationMin = 0.5;
			periodicFilterUpDurationMax = 180;
			overallFilterUpDurationMin = duration + 90.0;
			overallFilterUpDurationMax = duration + 360.0;
			overallFilterUpExponentMin = 0.5;
			overallFilterUpExponentMax = 3.5;
			overallFilterUpOffsetMin = offset + 15.0;
			overallFilterUpOffsetMax = offset + 45.0;

			minTransmission = 0.3;
			maxTransmission = 0.6;
			minDelay = 4.0;
			maxDelay = 20.0;

			periodicSpeedUpDurationMin = 20.0;
			periodicSpeedUpDurationMax = 180.0;
			periodicSpeedUpPercentageMin = 0.0;
			periodicSpeedUpPercentageMax = 2.0;

			periodicSlowDownDurationMin = 20.0;
			periodicSlowDownDurationMax = 180.0;
			periodicSlowDownPercentageMin = 0.0;
			periodicSlowDownPercentageMax = 0.9;

			overallSpeedUpDurationMin = 10.0;
			overallSpeedUpDurationMax = 60.0;
			overallSpeedUpExponentMin = 1;
			overallSpeedUpExponentMax = 1;

			periodicWetInDurationMin = 0.5;
			periodicWetInDurationMax = 180;
			overallWetInDurationMin = duration + 5.0;
			overallWetInDurationMax = duration + 50.0;
			overallWetInExponentMin = 0.5;
			overallWetInExponentMax = 2.5;
			overallWetInOffsetMin = offset;
			overallWetInOffsetMax = offset + 40;

			minWetOut = 1.0;
			maxWetOut = 1.7;
			minHighPass = 0.0;
			maxHighPass = 5000.0;
			minLowPass = 15000.0;
			maxLowPass = 20000.0;

			periodicMasterFilterDownDurationMin = 0.5;
			periodicMasterFilterDownDurationMax = 90;
			overallMasterFilterDownDurationMin = duration + 30;
			overallMasterFilterDownDurationMax = duration + 120;
			overallMasterFilterDownExponentMin = 0.5;
			overallMasterFilterDownExponentMax = 3.5;
			overallMasterFilterDownOffsetMin = offset;
			overallMasterFilterDownOffsetMax = offset + 30;

			offsetChoices = IntStream.range(0, 7)
					.mapToDouble(i -> Math.pow(2, -i))
					.toArray();
			offsetChoices[0] = 0.0;

			repeatChoices = IntStream.range(0, 9)
					.map(i -> i - 2)
					.mapToDouble(i -> Math.pow(2, i))
					.toArray();

			repeatChoices = new double[] { 16 };


			minChoice = i -> minChoiceValue;
			maxChoice = i -> maxChoiceValue;
			minX = i -> minXValue;
			maxX = i -> maxXValue;
			minY = i -> minYValue;
			maxY = i -> maxYValue;
			minZ = i -> minZValue;
			maxZ = i -> maxZValue;
			minVolume = i -> minVolumeValue;
			maxVolume = i -> maxVolumeValue;
		}
	}
}
