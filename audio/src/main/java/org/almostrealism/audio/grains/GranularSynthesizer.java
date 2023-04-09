/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.grains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarProducerBase;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.DynamicWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.ParameterizedWaveDataProviderFactory;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDataProviderList;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.Input;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.hardware.mem.MemoryBankAdapter;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.computations.AcceleratedTimeSeriesValueAt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GranularSynthesizer implements ParameterizedWaveDataProviderFactory, CellFeatures {
	public static double ampModWavelengthMin = 0.1;
	public static double ampModWavelengthMax = 10;

	private static ContextSpecific<KernelizedEvaluable<Pair<?>>> sourceKernel;
	private static ContextSpecific<KernelizedEvaluable<Scalar>> playbackKernel;
	private static ContextSpecific<KernelizedEvaluable<Scalar>> modKernel;

	static {
		sourceKernel = new DefaultContextSpecific<>(() ->
			Ops.ops().pair(Ops.ops().scalarsMultiply(
						Ops.ops().v(Scalar.class, 1), Ops.ops().v(Scalar.class, 2, -1)),
					Ops.ops().v(Scalar.class, 0)).get()
		);

		playbackKernel = new DefaultContextSpecific<>(() -> {
			TraversalPolicy grainShape = new TraversalPolicy(3);
			Producer<PackedCollection<?>> g = (Producer) Ops.ops().v(PackedCollection.class, 1, -1);
			ScalarProducerBase pos = Ops.ops().scalar(grainShape, g, 0).add(
							Ops.ops().mod(Ops.ops().multiply(Ops.ops().scalar(grainShape, g, 2), Ops.ops().v(Scalar.class, 2, -1))
									.multiply(Ops.ops().v(Scalar.class, 0)), Ops.ops().scalar(grainShape, g, 1)))
					.multiply(OutputLine.sampleRate);
			Producer cursor = Ops.ops().pair(pos, Ops.ops().v(0.0));
			return new AcceleratedTimeSeriesValueAt(Ops.ops().v(AcceleratedTimeSeries.class, 3, -1), cursor).get();
		});

		modKernel = new DefaultContextSpecific<>(() ->
				Ops.ops().sinw(Ops.ops().scalarSubtract(Ops.ops().v(Scalar.class, 0),
							Ops.ops().v(Scalar.class, 2, -1)),
							Ops.ops().v(Scalar.class, 3, -1),
							Ops.ops().v(Scalar.class, 4, -1))
					.multiply(Ops.ops().v(Scalar.class, 1)).get());


	}

	private double gain;
	private List<GrainSet> grains;

	public GranularSynthesizer() {
		gain = 1.0;
		grains = new ArrayList<>();
	}

	@JsonIgnore
	@Override
	public int getCount() {
		return 10 * OutputLine.sampleRate;
		// return WaveOutput.defaultTimelineFrames;
	}

	@JsonIgnore
	public double getDuration() {
		return 10;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public List<GrainSet> getGrains() {
		return grains;
	}

	public void setGrains(List<GrainSet> grains) {
		this.grains = grains;
	}

	public GrainSet addFile(String file) {
		GrainSet g = new GrainSet(new FileWaveDataProvider(file));
		grains.add(g);
		return g;
	}

	public void addGrain(GrainGenerationSettings settings) {
		if (grains.isEmpty()) throw new UnsupportedOperationException();
		grains.get((int) (Math.random() * grains.size())).addGrain(settings);
	}

	@Override
	public WaveDataProviderList create(Producer<Scalar> x, Producer<Scalar> y, Producer<Scalar> z, List<Frequency> playbackRates) {
		Evaluable<Scalar> evX = x.get();
		Evaluable<Scalar> evY = y.get();
		Evaluable<Scalar> evZ = z.get();

		List<WaveDataProvider> providers = new ArrayList<>();
		playbackRates.forEach(rate -> {
			PackedCollection<?> output = WaveData.allocateCollection(getCount());
			WaveData destination = new WaveData(output, OutputLine.sampleRate);
			providers.add(new DynamicWaveDataProvider("synth://" + UUID.randomUUID(), destination));
		});

		AcceleratedTimeSeries sourceRec = AcceleratedTimeSeries.defaultSeries();

		return new WaveDataProviderList(providers, () -> () -> {
			ParameterSet params = new ParameterSet(evX.evaluate().getValue(), evY.evaluate().getValue(), evZ.evaluate().getValue());

			ScalarBank playbackRate = new ScalarBank(1);

			for (int i = 0; i < playbackRates.size(); i++) {
				playbackRate.get(0).setValue(playbackRates.get(i).asHertz());
				if (WaveOutput.enableVerbose)
					System.out.println("GranularSynthesizer: Rendering grains for playback rate " + playbackRates.get(i) + "...");

				List<ScalarBank> results = new ArrayList<>();
				int count = grains.stream().map(GrainSet::getGrains).mapToInt(List::size).sum();

				for (GrainSet grainSet : grains) {
					WaveData source = grainSet.getSource().get();

					for (int n = 0; n < grainSet.getGrains().size(); n++) {
						Grain grain = grainSet.getGrain(n);
						GrainParameters gp = grainSet.getParams(n);

						// TODO  Create a kernel function that inserts every Scalar from a bank into AcceleratedTimeSeries, to replace this
						// w(source).map(k -> new ReceptorCell<>(sourceRec)).iter(source.getWave().getCount(), false).get().run();

						PairBank sourceRecBank = new PairBank(source.getCollection().getMemLength(), sourceRec, 2, MemoryBankAdapter.defaultCacheLevel);
						sourceKernel.getValue().kernelEvaluate(sourceRecBank, source.getCollection(), WaveOutput.timelineScalar.getValue(), new Scalar(source.getSampleRate()));
						sourceRec.set(0, 1, source.getCollection().getMemLength() + 1);

						ScalarBank raw = new ScalarBank(getCount());
						playbackKernel.getValue().kernelEvaluate(raw, WaveOutput.timelineScalar.getValue(), grain, playbackRate, sourceRec);

						ScalarBank result = new ScalarBank(getCount());
						double amp = gp.getAmp().apply(params);
						double phase = gp.getPhase().apply(params);
						double wavelength = ampModWavelengthMin + Math.abs(gp.getWavelength().apply(params)) * (ampModWavelengthMax - ampModWavelengthMin);
						modKernel.getValue().kernelEvaluate(result, WaveOutput.timelineScalar.getValue(), raw, new Scalar(phase), new Scalar(wavelength), new Scalar(amp));

						results.add(result);

						sourceRec.reset();
					}
				}

				ScalarProducerBase sum = scalarAdd(Input.generateArguments(2 * getCount(), 0, results.size())).multiply(gain / count);

				if (WaveOutput.enableVerbose) System.out.println("GranularSynthesizer: Summing grains...");
				sum.get().kernelEvaluate(providers.get(i).get().getCollection(), results.stream().toArray(MemoryBank[]::new));
				if (WaveOutput.enableVerbose) System.out.println("GranularSynthesizer: Done");
			}
		});
	}
}
