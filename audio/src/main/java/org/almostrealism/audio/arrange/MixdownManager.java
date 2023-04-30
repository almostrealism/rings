package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import org.almostrealism.algebra.Pair;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.optimize.AdjustmentChromosome;
import org.almostrealism.audio.optimize.AudioSceneGenome;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.TemporalFactor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class MixdownManager implements Setup, CellFeatures {
	private ConfigurableGenome genome;
	private AdjustmentChromosome volume;
	private AdjustmentChromosome mainFilterUp;

	public MixdownManager(ConfigurableGenome genome, int channels, TimeCell clock, int sampleRate) {
		this.genome = genome;

		SimpleChromosome v = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> v.addGene());
		this.volume = new AdjustmentChromosome(v, 0.0, 1.0, true, sampleRate);
		this.volume.setGlobalTime(clock.frame());

		SimpleChromosome fup = genome.addSimpleChromosome(AdjustmentChromosome.SIZE);
		IntStream.range(0, channels).forEach(i -> fup.addGene());
		this.mainFilterUp = new AdjustmentChromosome(fup, 0.0, 1.0, false, sampleRate);
		this.mainFilterUp.setGlobalTime(clock.frame());

		initRanges(new AudioSceneGenome.GeneratorConfiguration(channels));
	}

	public void initRanges(AudioSceneGenome.GeneratorConfiguration config) {
		setPeriodicVolumeDurationRange(
				DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMin),
				DefaultAudioGenome.factorForPeriodicAdjustmentDuration(config.periodicVolumeDurationMax));
		setOverallVolumeDurationRange(
				DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallVolumeDurationMin),
				DefaultAudioGenome.factorForPolyAdjustmentDuration(config.overallVolumeDurationMax));
		setOverallVolumeInitialRange(
				DefaultAudioGenome.factorForAdjustmentInitial(config.minVolumeValue),
				DefaultAudioGenome.factorForAdjustmentInitial(config.maxVolumeValue));
		setOverallVolumeScaleRange(-1.0, -1.0);
		setOverallVolumeExponentRange(
				DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallVolumeExponentMin),
				DefaultAudioGenome.factorForPolyAdjustmentExponent(config.overallVolumeExponentMax));
		setOverallVolumeOffsetRange(
				DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMin),
				DefaultAudioGenome.factorForAdjustmentOffset(config.overallVolumeOffsetMax));
	}

	public void setPeriodicVolumeDurationRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(0, min, max);
	}

	public void setOverallVolumeDurationRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(1, min, max);
	}

	public void setOverallVolumeExponentRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(2, min, max);
	}

	public void setOverallVolumeInitialRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(3, min, max);
	}

	public void setOverallVolumeScaleRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(4, min, max);
	}

	public void setOverallVolumeOffsetRange(double min, double max) {
		((SimpleChromosome) volume.getSource()).setParameterRange(5, min, max);
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList();
		setup.add(volume.expand());
		setup.add(mainFilterUp.expand());
		return setup;
	}

	public CellList cells(DefaultAudioGenome legacyGenome, CellList sources, List<? extends Receptor<PackedCollection<?>>> measures, Receptor<PackedCollection<?>> output) {
		CellList cells = sources;

		if (AudioScene.enableMainFilterUp) {
			// Apply dynamic high pass filters
//			cells = cells.map(fc(i -> {
//				TemporalFactor<PackedCollection<?>> f = (TemporalFactor<PackedCollection<?>>) legacyGenome.valueAt(DefaultAudioGenome.MAIN_FILTER_UP, i, 0);
//				return hp(scalar(20000).multiply(f.getResultant(c(1.0))), v(DefaultAudioGenome.defaultResonance));
//			}));
			cells = cells.map(fc(i -> {
				TemporalFactor<PackedCollection<?>> f = (TemporalFactor<PackedCollection<?>>) mainFilterUp.valueAt(i, 0);
				return hp(scalar(20000).multiply(f.getResultant(c(1.0))), v(DefaultAudioGenome.defaultResonance));
			}));
		}

		cells = cells
				.addRequirements(legacyGenome.getTemporals().toArray(TemporalFactor[]::new));
				// .addRequirements(volume, mainFilterUp);

		if (AudioScene.enableSourcesOnly) {
			return cells.map(fc(i -> factor(legacyGenome.valueAt(DefaultAudioGenome.VOLUME, i, 0))))
					.sum().map(fc(i -> sf(0.2))).map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
//			return cells.map(fc(i -> factor(volume.valueAt(i, 0))))
//					.sum().map(fc(i -> sf(0.2))).map(i -> new ReceptorCell<>(Receptor.to(output, measures.get(0), measures.get(1))));
		}

		if (AudioScene.enableMixdown)
			cells = cells.mixdown(AudioScene.mixdownDuration);

		// Volume adjustment
		CellList branch[] = cells.branch(
				fc(i -> factor(legacyGenome.valueAt(DefaultAudioGenome.VOLUME, i, 0))),
				AudioScene.enableEfxFilters ?
						fc(i -> factor(legacyGenome.valueAt(DefaultAudioGenome.VOLUME, i, 0))
								.andThen(legacyGenome.valueAt(DefaultAudioGenome.FX_FILTERS, i, 0))) :
						fc(i -> factor(legacyGenome.valueAt(DefaultAudioGenome.VOLUME, i, 0))));
//		CellList branch[] = cells.branch(
//				fc(i -> factor(volume.valueAt(i, 0))),
//				AudioScene.enableEfxFilters ?
//						fc(i -> factor(volume.valueAt(i, 0))
//								.andThen(legacyGenome.valueAt(DefaultAudioGenome.FX_FILTERS, i, 0))) :
//						fc(i -> factor(volume.valueAt(i, 0))));

		CellList main = branch[0];
		CellList efx = branch[1];

		// Sum the main layer
		main = main.sum();

		if (AudioScene.enableEfx) {
			// Create the delay layers
			int delayLayers = legacyGenome.valueAt(DefaultAudioGenome.PROCESSORS).length();
			CellList delays = IntStream.range(0, delayLayers)
					.mapToObj(i -> new AdjustableDelayCell(OutputLine.sampleRate,
							toScalar(legacyGenome.valueAt(DefaultAudioGenome.PROCESSORS, i, 0).getResultant(c(1.0))),
							toScalar(legacyGenome.valueAt(DefaultAudioGenome.PROCESSORS, i, 1).getResultant(c(1.0)))))
					.collect(CellList.collector());

			// Route each line to each delay layer
			efx = efx.m(fi(), delays, i -> delayGene(delayLayers, legacyGenome.valueAt(DefaultAudioGenome.WET_IN, i)))
					// Feedback grid
					.mself(fi(), legacyGenome.valueAt(DefaultAudioGenome.TRANSMISSION),
							fc(legacyGenome.valueAt(DefaultAudioGenome.WET_OUT, 0)))
					.sum();

			if (AudioScene.disableClean) {
				efx.get(0).setReceptor(Receptor.to(output, measures.get(0), measures.get(1)));
				return efx;
			} else {
				// Mix efx with main and measure #2
				efx.get(0).setReceptor(Receptor.to(main.get(0), measures.get(1)));

				if (AudioScene.enableMasterFilterDown) {
					// Apply dynamic low pass filter
					main = main.map(fc(i -> {
						TemporalFactor<PackedCollection<?>> f = (TemporalFactor<PackedCollection<?>>) legacyGenome.valueAt(DefaultAudioGenome.MASTER_FILTER_DOWN, i, 0);
						return lp(scalar(20000).multiply(f.getResultant(c(1.0))), v(DefaultAudioGenome.defaultResonance));
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
}
