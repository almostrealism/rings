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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.DelayNetwork;
import org.almostrealism.audio.optimize.DelayChromosome;
import org.almostrealism.audio.optimize.FixedFilterChromosome;
import org.almostrealism.audio.optimize.OptimizeFactorFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.PassThroughCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ConfigurableGenome;
import io.almostrealism.relation.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.heredity.TemporalFactor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class MixdownManager implements Setup, CellFeatures, OptimizeFactorFeatures {
	public static final int mixdownDuration = 140;

	public static boolean enableMixdown = false;
	public static boolean enableSourcesOnly = false;
	public static boolean disableClean = false;

	public static boolean enableMainFilterUp = true;
	public static boolean enableAutomationManager = true;

	public static boolean enableEfxFilters = true;
	public static boolean enableEfx = true;
	public static boolean enableReverb = true;
	public static boolean enableTransmission = true;
	public static boolean enableWetInAdjustment = true;
	public static boolean enableMasterFilterDown = true;

	public static boolean enableWetSources = true;

	protected static double reverbLevel = 2.0;

	private AutomationManager automation;
	private TimeCell clock;
	private int sampleRate;

	private PackedCollection<?> volumeAdjustmentScale;
	private PackedCollection<?> mainFilterUpAdjustmentScale;
	private PackedCollection<?> mainFilterDownAdjustmentScale;
	private PackedCollection<?> reverbAdjustmentScale;

	private SimpleChromosome volumeSimple;
	private SimpleChromosome mainFilterUpSimple;
	private SimpleChromosome wetInSimple;

	private SimpleChromosome transmission;
	private SimpleChromosome wetOut;
	private SimpleChromosome delay;

	private DelayChromosome delayDynamics;
	private SimpleChromosome delayDynamicsSimple;

	private SimpleChromosome reverb;
	private SimpleChromosome reverbAutomation;
	private FixedFilterChromosome wetFilter;
	private SimpleChromosome mainFilterDownSimple;

	private List<Integer> reverbChannels;

	public MixdownManager(ConfigurableGenome genome,
						  int channels, int delayLayers,
						  AutomationManager automation,
						  TimeCell clock, int sampleRate) {
		this.automation = automation;
		this.clock = clock;
		this.sampleRate = sampleRate;

		this.volumeAdjustmentScale = new PackedCollection<>(1).fill(1.0);
		this.mainFilterUpAdjustmentScale = new PackedCollection<>(1).fill(1.0);
		this.mainFilterDownAdjustmentScale = new PackedCollection<>(1).fill(1.0);
		this.reverbAdjustmentScale = new PackedCollection<>(1).fill(1.0);

		this.volumeSimple = initializeAdjustment(channels, genome.addSimpleChromosome(ADJUSTMENT_CHROMOSOME_SIZE));
		this.mainFilterUpSimple = initializeAdjustment(channels, genome.addSimpleChromosome(ADJUSTMENT_CHROMOSOME_SIZE));
		this.wetInSimple = initializeAdjustment(channels, genome.addSimpleChromosome(6));

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
		this.delayDynamics.setGlobalTime(clock);

		this.delayDynamicsSimple = initializePolycyclic(delayLayers, genome.addSimpleChromosome(POLYCYCLIC_CHROMOSOME_SIZE));

		this.reverb = genome.addSimpleChromosome(1);
		IntStream.range(0, channels).forEach(i -> reverb.addGene());

		this.reverbAutomation = genome.addSimpleChromosome(AutomationManager.GENE_LENGTH);
		IntStream.range(0, channels).forEach(i -> reverbAutomation.addGene());

		SimpleChromosome wf = genome.addSimpleChromosome(FixedFilterChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> wf.addGene());
		this.wetFilter = new FixedFilterChromosome(wf, sampleRate);

		this.mainFilterDownSimple = initializeAdjustment(channels, genome.addSimpleChromosome(ADJUSTMENT_CHROMOSOME_SIZE));

		initRanges(new Configuration(channels), delayLayers);

		this.reverbChannels = new ArrayList<>();
	}

	public AutomationManager getAutomationManager() { return automation; }

	public void setVolumeAdjustmentScale(double scale) {
		volumeAdjustmentScale.set(0, scale);
	}

	public void setMainFilterUpAdjustmentScale(double scale) {
		mainFilterUpAdjustmentScale.set(0, scale);
	}

	public void setMainFilterDownAdjustmentScale(double scale) {
		mainFilterDownAdjustmentScale.set(0, scale);
	}

	public void setReverbAdjustmentScale(double scale) {
		reverbAdjustmentScale.set(0, scale);
	}

	public List<Integer> getReverbChannels() {
		return reverbChannels;
	}

	public void setReverbChannels(List<Integer> reverbChannels) {
		this.reverbChannels = reverbChannels;
	}

	public void initRanges(Configuration config, int delayLayers) {
		volumeSimple.setParameterRange(0,
				factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMin),
				factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMax));
		volumeSimple.setParameterRange(1,
				factorForPolyAdjustmentDuration(config.overallVolumeDurationMin),
				factorForPolyAdjustmentDuration(config.overallVolumeDurationMax));
		volumeSimple.setParameterRange(2,
				factorForPolyAdjustmentExponent(config.overallVolumeExponentMin),
				factorForPolyAdjustmentExponent(config.overallVolumeExponentMax));
		volumeSimple.setParameterRange(3,
				factorForAdjustmentInitial(config.minVolumeValue),
				factorForAdjustmentInitial(config.maxVolumeValue));
		volumeSimple.setParameterRange(4, -1.0, -1.0);
		volumeSimple.setParameterRange(5,
				factorForAdjustmentOffset(config.overallVolumeOffsetMin),
				factorForAdjustmentOffset(config.overallVolumeOffsetMax));

		if (!enableAutomationManager) {
			mainFilterUpSimple.setParameterRange(0,
					factorForPeriodicAdjustmentDuration(config.periodicFilterUpDurationMin),
					factorForPeriodicAdjustmentDuration(config.periodicFilterUpDurationMax));
			mainFilterUpSimple.setParameterRange(1,
					factorForPolyAdjustmentDuration(config.overallFilterUpDurationMin),
					factorForPolyAdjustmentDuration(config.overallFilterUpDurationMax));
			mainFilterUpSimple.setParameterRange(2,
					factorForPolyAdjustmentExponent(config.overallFilterUpExponentMin),
					factorForPolyAdjustmentExponent(config.overallFilterUpExponentMax));
			mainFilterUpSimple.setParameterRange(3,
					factorForAdjustmentInitial(0),
					factorForAdjustmentInitial(0));
			mainFilterUpSimple.setParameterRange(4, 1.0, 1.0);
			mainFilterUpSimple.setParameterRange(5,
					factorForAdjustmentOffset(config.overallFilterUpOffsetMin),
					factorForAdjustmentOffset(config.overallFilterUpOffsetMax));
		}

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

		delayDynamics.setDelayRange(0.0, 0.0); // Not used
		delayDynamics.setPeriodicSpeedUpDurationRange(config.periodicSpeedUpDurationMin, config.periodicSpeedUpDurationMax);
		delayDynamics.setPeriodicSpeedUpPercentageRange(config.periodicSpeedUpPercentageMin, config.periodicSpeedUpPercentageMax);
		delayDynamics.setPeriodicSlowDownDurationRange(config.periodicSlowDownDurationMin, config.periodicSlowDownDurationMax);
		delayDynamics.setPeriodicSlowDownPercentageRange(config.periodicSlowDownPercentageMin, config.periodicSlowDownPercentageMax);
		delayDynamics.setOverallSpeedUpDurationRange(config.overallSpeedUpDurationMin, config.overallSpeedUpDurationMax);
		delayDynamics.setOverallSpeedUpExponentRange(config.overallSpeedUpExponentMin, config.overallSpeedUpExponentMax);

		delayDynamicsSimple.setParameterRange(0,
				factorForSpeedUpDuration(config.periodicSpeedUpDurationMin),
				factorForSpeedUpDuration(config.periodicSpeedUpDurationMax));
		delayDynamicsSimple.setParameterRange(1,
				factorForSpeedUpPercentage(config.periodicSpeedUpPercentageMin),
				factorForSpeedUpPercentage(config.periodicSpeedUpPercentageMax));
		delayDynamicsSimple.setParameterRange(2,
				factorForSlowDownDuration(config.periodicSlowDownDurationMin),
				factorForSlowDownDuration(config.periodicSlowDownDurationMax));
		delayDynamicsSimple.setParameterRange(3,
				factorForSlowDownPercentage(config.periodicSlowDownPercentageMin),
				factorForSlowDownPercentage(config.periodicSlowDownPercentageMax));
		delayDynamicsSimple.setParameterRange(4,
				factorForPolySpeedUpDuration(config.overallSpeedUpDurationMin),
				factorForPolySpeedUpDuration(config.overallSpeedUpDurationMax));
		delayDynamicsSimple.setParameterRange(5,
				factorForPolySpeedUpExponent(config.overallSpeedUpExponentMin),
				factorForPolySpeedUpExponent(config.overallSpeedUpExponentMax));

		reverb.setParameterRange(0, 0.0, 1.0);

		wetFilter.setHighPassRange(config.minHighPass, config.maxHighPass);
		wetFilter.setLowPassRange(config.minLowPass, config.maxLowPass);

		mainFilterDownSimple.setParameterRange(0,
				factorForPeriodicAdjustmentDuration(config.periodicMasterFilterDownDurationMin),
				factorForPeriodicAdjustmentDuration(config.periodicMasterFilterDownDurationMax));
		mainFilterDownSimple.setParameterRange(1,
				factorForPolyAdjustmentDuration(config.overallMasterFilterDownDurationMin),
				factorForPolyAdjustmentDuration(config.overallMasterFilterDownDurationMax));
		mainFilterDownSimple.setParameterRange(2,
				factorForPolyAdjustmentExponent(config.overallMasterFilterDownExponentMin),
				factorForPolyAdjustmentExponent(config.overallMasterFilterDownExponentMax));
		mainFilterDownSimple.setParameterRange(3,
				factorForAdjustmentInitial(1.0),
				factorForAdjustmentInitial(1.0));
		mainFilterDownSimple.setParameterRange(4, -1.0, -1.0);
		mainFilterDownSimple.setParameterRange(5,
				factorForAdjustmentOffset(config.overallMasterFilterDownOffsetMin),
				factorForAdjustmentOffset(config.overallMasterFilterDownOffsetMax));
	}

	@Override
	public Supplier<Runnable> setup() {
		return new OperationList("Mixdown Manager Setup");
	}

	public CellList cells(CellList sources,
						  List<? extends Receptor<PackedCollection<?>>> stems,
						  Receptor<PackedCollection<?>> output) {
		return cells(sources, null, List.of(), stems, output, i -> i);
	}

	public CellList cells(CellList sources, CellList wetSources,
						  List<? extends Receptor<PackedCollection<?>>> measures,
						  List<? extends Receptor<PackedCollection<?>>> stems,
						  Receptor<PackedCollection<?>> output,
						  IntUnaryOperator channelIndex) {
		CellList cells = sources;

		if (enableMainFilterUp) {
			// Apply dynamic high pass filters
			if (enableAutomationManager) {
				cells = cells.map(fc(i -> {
					Producer<PackedCollection<?>> v =
							automation.getAggregatedValue(
									mainFilterUpSimple.valueAt(channelIndex.applyAsInt(i)),
									p(mainFilterUpAdjustmentScale), -40.0);
					return hp(scalar(20000).multiply(v), scalar(FixedFilterChromosome.defaultResonance));
				}));
			} else {
				cells = cells.map(fc(i -> {
					Factor<PackedCollection<?>> f = toAdjustmentGene(clock, sampleRate,
							p(mainFilterUpAdjustmentScale), mainFilterUpSimple,
							channelIndex.applyAsInt(i)).valueAt(0);
					return hp(scalar(20000).multiply(f.getResultant(c(1.0))), scalar(FixedFilterChromosome.defaultResonance));
				}));
			}
		}

		IntFunction<Factor<PackedCollection<?>>> v = i -> factor(toAdjustmentGene(clock, sampleRate,
														p(volumeAdjustmentScale), volumeSimple,
														channelIndex.applyAsInt(i)).valueAt(0));

		if (enableSourcesOnly) {
			return cells
					.map(fc(v))
					.sum().map(fc(i -> sf(0.8))).map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
		}

		if (enableMixdown)
			cells = cells.mixdown(mixdownDuration);

		boolean reverbActive = enableReverb && !getReverbChannels().isEmpty() &&
			IntStream.range(0, sources.size())
					.map(i -> channelIndex.applyAsInt(i))
					.anyMatch(getReverbChannels()::contains);

		IntFunction<Factor<PackedCollection<?>>> reverbFactor;

		if (!reverbActive) {
			reverbFactor = i -> sf(0.0);
		} else if (enableAutomationManager) {
			reverbFactor = i -> getReverbChannels().contains(channelIndex.applyAsInt(i)) ?
					in -> multiply(in, automation.getAggregatedValue(
								reverbAutomation.valueAt(channelIndex.applyAsInt(i)),
								p(reverbAdjustmentScale), 0.0))
							.multiply(c(reverbLevel)) :
						sf(0.0);
		} else {
			reverbFactor = i -> getReverbChannels().contains(channelIndex.applyAsInt(i)) ?
					factor(reverb.valueAt(channelIndex.applyAsInt(i), 0)) :
						sf(0.0);
		}

		CellList main;
		CellList efx;
		CellList reverb;

		if (enableWetSources) {
			// Apply volume to main
			main = cells.map(fc(v));

			// Branch from wet sources for efx and reverb
			CellList branch[] = wetSources.branch(
					enableEfxFilters ?
							fc(i -> v.apply(i).andThen(wetFilter.valueAt(channelIndex.applyAsInt(i), 0))) :
							fc(v),
					fc(reverbFactor));

			efx = branch[0];
			reverb = branch[1];
		} else {
			// Branch from main
			CellList branch[] = cells.branch(
					fc(v),
					enableEfxFilters ?
							fc(i -> v.apply(i).andThen(wetFilter.valueAt(channelIndex.applyAsInt(i), 0))) :
							fc(v),
					fc(reverbFactor));

			main = branch[0];
			efx = branch[1];
			reverb = branch[2];
		}

		if (stems != null && !stems.isEmpty()) {
			main = main.branch(i -> new ReceptorCell<>(stems.get(i)),
								i -> new PassThroughCell<>())[1];
		}

		// Sum the main layer
		main = main.sum();

		if (enableEfx) {
			if (enableTransmission) {
				int delayLayers = delay.length();

				IntFunction<Factor<PackedCollection<?>>> df =
						i -> toPolycyclicGene(clock, sampleRate, delayDynamicsSimple, i).valueAt(0);

				CellList delays = IntStream.range(0, delayLayers)
						.mapToObj(i -> new AdjustableDelayCell(OutputLine.sampleRate,
								toScalar(delay.valueAt(i, 0).getResultant(c(1.0))),
								toScalar(df.apply(i).getResultant(c(1.0)))))
						.collect(CellList.collector());

				IntFunction<Gene<PackedCollection<?>>> tg =
						i -> delayGene(delayLayers, toAdjustmentGene(clock, sampleRate, null, wetInSimple, channelIndex.applyAsInt(i)));

				// Route each line to each delay layer
				efx = efx.m(fi(), delays, tg)
						// Feedback grid
						.mself(fi(), transmission, fc(wetOut.valueAt(0)))
						.sum();
			} else {
				efx = efx.sum();
			}

			if (reverbActive) {
				// Combine inputs and apply reverb
				reverb = reverb.sum().map(fc(i -> new DelayNetwork(sampleRate, false)));

				if (enableTransmission) {
					// Combine reverb with efx
					efx = cells(efx, reverb).sum();
				} else {
					// There are no other fx
					efx = reverb;
				}
			}

			if (disableClean) {
				Receptor r[] = new Receptor[measures.size() + 1];
				r[0] = output;
				for (int i = 0; i < measures.size(); i++) r[i + 1] = measures.get(i);

				efx.get(0).setReceptor(Receptor.to(r));
				return efx;
			} else {
				List<Receptor<PackedCollection<?>>> efxReceptors = new ArrayList<>();
				efxReceptors.add(main.get(0));
				if (measures.size() > 1) efxReceptors.add(measures.get(1));
				if (stems != null && !stems.isEmpty()) efxReceptors.add(stems.get(sources.size()));
				efx.get(0).setReceptor(Receptor.to(efxReceptors.toArray(Receptor[]::new)));

//				if (stems != null && !stems.isEmpty()) {
//					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1), stems.get(sources.size())));
//				} else {
//					efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));
//				}

				if (enableMasterFilterDown) {
					// Apply dynamic low pass filter
					main = main.map(fc(i -> {
						Factor<PackedCollection<?>> f = toAdjustmentGene(clock, sampleRate,
								p(mainFilterDownAdjustmentScale), mainFilterDownSimple,
								channelIndex.applyAsInt(i)).valueAt(0);
						return lp(scalar(20000).multiply(f.getResultant(c(1.0))), scalar(FixedFilterChromosome.defaultResonance));
					}));
				}

				// Deliver main to the output and measure #1
				if (measures.isEmpty()) {
					main = main.map(i -> new ReceptorCell<>(output));
				} else {
					main = main.map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0))));
				}

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
	private TemporalFactor<PackedCollection<?>> factor(Factor<PackedCollection<?>> f) {
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

		if (enableWetInAdjustment) {
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

			minWetOut = 0.5;
			maxWetOut = 1.4;
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
