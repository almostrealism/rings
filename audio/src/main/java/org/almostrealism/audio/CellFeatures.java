/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.audio;

import io.almostrealism.code.ProducerComputation;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.computations.DefaultEnvelopeComputation;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.audio.sequence.ValueSequenceCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.graph.FilteredCell;
import org.almostrealism.graph.MultiCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.graph.SummationCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.heredity.IdentityFactor;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalFeatures;
import org.almostrealism.CodeFeatures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface CellFeatures extends HeredityFeatures, TemporalFeatures, CodeFeatures {
	default Receptor<PackedCollection<?>> a(Supplier<Evaluable<? extends PackedCollection<?>>>... destinations) {
		if (destinations.length == 1) {
			return protein -> a(1, destinations[0], protein);
		} else {
			return protein -> Stream.of(destinations).map(v -> a(1, v, protein)).collect(OperationList.collector());
		}
	}

	default CollectionTemporalCellAdapter silent() {
		return CollectionTemporalCellAdapter.from(c(0.0));
	}

	default CellList silence() {
		CellList cells = new CellList();
		cells.addRoot(silent());
		return cells;
	}

	default CellList cells(int count, IntFunction<Cell<PackedCollection<?>>> cells) {
		CellList c = new CellList();
		IntStream.range(0, count).mapToObj(cells).forEach(c::addRoot);
		return c;
	}

	default CellList cells(CellList... cells) {
		return all(cells.length, i -> cells[i]);
	}

	default CellList all(int count, IntFunction<CellList> cells) {
		CellList all[] = new CellList[count];
		IntStream.range(0, count).forEach(i -> all[i] = cells.apply(i));

		CellList result = new CellList(all);
		IntStream.range(0, all.length).mapToObj(i -> all[i]).flatMap(Collection::stream).forEach(result::add);
		return result;
	}

	default CellList map(CellList cells, IntFunction<Cell<PackedCollection<?>>> dest) {
		CellList c = new CellList(cells);

		IntStream.range(0, cells.size()).forEach(i -> {
			Cell<PackedCollection<?>> d = dest.apply(i);
			cells.get(i).setReceptor(d);
			c.add(d);
		});

		return c;
	}

	default CellList[] branch(CellList cells, IntFunction<Cell<PackedCollection<?>>>... dest) {
		CellList c[] = IntStream.range(0, dest.length).mapToObj(i -> new CellList(cells)).toArray(CellList[]::new);

		IntStream.range(0, cells.size()).forEach(i -> {
			Cell<PackedCollection<?>> d[] = new Cell[dest.length];

			IntStream.range(0, dest.length).forEach(j -> {
				d[j] = dest[j].apply(i);
				c[j].add(d[j]);
			});

			cells.get(i).setReceptor(Receptor.to(d));
		});

		return c;
	}

	default CellList w(WaveData... waves) {
		return w((Supplier) PolymorphicAudioData::new, waves);
	}

	default CellList w(Supplier<PolymorphicAudioData> data, WaveData... waves) {
		return w(data, null, null, waves);
	}

	default CellList w(Collection<Frequency> frequencies) {
		return w(PolymorphicAudioData::new, frequencies);
	}

	default CellList w(Supplier<PolymorphicAudioData> data, Collection<Frequency> frequencies) {
		return w(data, frequencies.stream());
	}

	default CellList w(Frequency... frequencies) {
		return w(PolymorphicAudioData::new, frequencies);
	}

	default CellList w(Supplier<PolymorphicAudioData> data, Frequency... frequencies) {
		return w(data, Stream.of(frequencies));
	}

	default CellList w(Supplier<PolymorphicAudioData> data, Stream<Frequency> frequencies) {
		CellList cells = new CellList();
		frequencies.map(f -> {
			SineWaveCell c = new SineWaveCell(data.get());
			c.setFreq(f.asHertz());
			c.setNoteLength(500);
			c.setAmplitude(0.5);
			c.setEnvelope(DefaultEnvelopeComputation::new);
			return c;
		}).forEach(cells::addRoot);
		return cells;
	}

	default CellList w(String... path) {
		return w(Stream.of(path).map(File::new).toArray(File[]::new));
	}

	default CellList w(Producer<PackedCollection<?>> repeat, String... path) {
		return w(null, repeat, path);
	}

	default CellList w(Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat, String... path) {
		return w(offset, repeat, Stream.of(path).map(File::new).toArray(File[]::new));
	}

	default CellList w(File... files) {
		return w((Supplier<PolymorphicAudioData>) PolymorphicAudioData::new, files);
	}

	default CellList w(Producer<PackedCollection<?>> repeat, File... files) {
		return w(null, repeat, files);
	}

	default CellList w(Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat, File... files) {
		return w(PolymorphicAudioData::new, offset, repeat, files);
	}

	default CellList w(Producer<PackedCollection<?>> repeat, WaveData... data) {
		return w(null, repeat, data);
	}

	default CellList w(Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat, WaveData... data) {
		return w(PolymorphicAudioData::new, offset, repeat, data);
	}

	default CellList w(Supplier<PolymorphicAudioData> data, File... files) {
		return w(data, null, null, files);
	}

	default CellList w(Supplier<PolymorphicAudioData> data, Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat, File... files) {
		CellList cells = new CellList();
		Stream.of(files).map(f -> {
			try {
				return WavFile.load(f, 1.0, offset, repeat).apply(data.get());
			} catch (IOException e) {
				e.printStackTrace();
				return silence().get(0);
			}
		}).forEach(cells::addRoot);
		return cells;
	}

	default CellList w(Supplier<PolymorphicAudioData> data, Producer<PackedCollection<?>> offset, Producer<PackedCollection<?>> repeat, WaveData... waves) {
		CellList cells = new CellList();
		Stream.of(waves).map(f -> {
			try {
				return WavFile.load(f, 1.0, offset, repeat).apply(data.get());
			} catch (IOException e) {
				e.printStackTrace();
				return silence().get(0);
			}
		}).forEach(cells::addRoot);
		return cells;
	}

	default CellList poly(int count, Supplier<PolymorphicAudioData> data, IntFunction<ProducerComputation<PackedCollection<?>>> decision, String... choices) {
		return poly(count, data, decision, Stream.of(choices).map(File::new).toArray(File[]::new));
	}

	default CellList poly(int count, Supplier<PolymorphicAudioData> data, IntFunction<ProducerComputation<PackedCollection<?>>> decision, File... choices) {
		return poly(count, data, decision, Stream.of(choices)
				.map(f -> (Function<PolymorphicAudioData, CollectionTemporalCellAdapter>) d -> (CollectionTemporalCellAdapter) w(data, f).get(0)).
				toArray(Function[]::new));
	}

	default CellList poly(int count, Supplier<PolymorphicAudioData> data, IntFunction<ProducerComputation<PackedCollection<?>>> decision, Frequency... choices) {
		return poly(count, data, decision, Stream.of(choices)
				.map(f -> (Function<PolymorphicAudioData, CollectionTemporalCellAdapter>) d -> (CollectionTemporalCellAdapter) w(data, f).get(0)).
				toArray(Function[]::new));
	}

	default CellList poly(int count, Supplier<PolymorphicAudioData> data, IntFunction<ProducerComputation<PackedCollection<?>>> decision,
						  Function<PolymorphicAudioData, CollectionTemporalCellAdapter>... choices) {
		return poly(count, i -> data.get(), decision, choices);
	}

	default CellList poly(int count, IntFunction<PolymorphicAudioData> data, IntFunction<ProducerComputation<PackedCollection<?>>> decision,
						  Function<PolymorphicAudioData, CollectionTemporalCellAdapter>... choices) {
		CellList cells = new CellList();
		IntStream.range(0, count).mapToObj(i -> new PolymorphicAudioCell(data.apply(i), decision.apply(i), choices)).forEach(cells::addRoot);
		return cells;
	}

	default CellList sum(CellList cells) {
		SummationCell sum = new SummationCell();
		cells.forEach(c -> c.setReceptor(sum));

		CellList result = new CellList(cells);
		result.add(sum);
		return result;
	}

	default CellList o(int count, IntFunction<File> f) {
		CellList result = new CellList();

		for (int i = 0; i < count; i++) {
			WaveOutput out = new WaveOutput(f.apply(i));
			result.add(new ReceptorCell<>(out));
			result.getFinals().add(out.write().get());
		}

		return result;
	}

	default CellList csv(CellList cells, IntFunction<File> f) {
		CellList result = new CellList(cells);

		for (int i = 0; i < cells.size(); i++) {
			WaveOutput out = new WaveOutput(f.apply(i));
			cells.get(i).setReceptor(out);
			result.getFinals().add(out.writeCsv(f.apply(i)).get());
		}

		return result;
	}

	default CellList o(CellList cells, IntFunction<File> f) {
		CellList result = new CellList(cells);

		for (int i = 0; i < cells.size(); i++) {
			WaveOutput out = new WaveOutput(f.apply(i));
			ReceptorCell c = new ReceptorCell(out);
			cells.get(i).setReceptor(c);
			result.add(c);
			result.getFinals().add(out.write().get());
		}

		return result;
	}

	default CellList om(CellList cells, IntFunction<File> f) {
		CellList result = new CellList(cells);

		for (int i = 0; i < cells.size(); i++) {
			WaveOutput out = new WaveOutput(f.apply(i));

			if (cells.get(i) instanceof CellAdapter) {
				((CellAdapter) cells.get(i)).setMeter(out);
			} else {
				cells.get(i).setReceptor(out);
			}

			result.getFinals().add(out.write().get());
		}

		return result;
	}

	default IntFunction<Cell<PackedCollection<?>>> fi() {
		return i -> new FilteredCell<>(i().apply(i));
	}

	default IntFunction<Factor<PackedCollection<?>>> i() {
		return i -> new IdentityFactor<>();
	}

	default CellList fi(int count) {
		return f(count, i());
	}

	default IntFunction<Cell<PackedCollection<?>>> fc(IntFunction<Factor<PackedCollection<?>>> filter) {
		return i -> new FilteredCell<>(filter.apply(i));
	}

	default CellList f(int count, IntFunction<Factor<PackedCollection<?>>> filter) {
		CellList layer = new CellList();

		for (int i = 0; i < count; i++) {
			layer.add(new FilteredCell<>(filter.apply(i)));
		}

		return layer;
	}

	default CellList f(CellList cells, IntFunction<Factor<PackedCollection<?>>> filter) {
		CellList layer = new CellList(cells);
		Iterator<Cell<PackedCollection<?>>> itr = cells.iterator();

		for (int i = 0; itr.hasNext(); i++) {
			FilteredCell<PackedCollection<?>> f = new FilteredCell<>(filter.apply(i));
			Cell<PackedCollection<?>> c = itr.next();

			c.setReceptor(f);
			layer.add(f);
		}

		return layer;
	}

	default CellList d(int count, IntFunction<Producer<Scalar>> d) {
		return d(count, d, i -> v(1.0));
	}

	default CellList d(int count, IntFunction<Producer<Scalar>> d, IntFunction<Producer<Scalar>> s) {
		CellList result = new CellList();

		for (int i = 0; i < count; i++) {
			result.add(new AdjustableDelayCell(OutputLine.sampleRate, d.apply(i), s.apply(i)));
		}

		return result;
	}

	default CellList d(CellList cells, IntFunction<Producer<Scalar>> delay) {
		return d(cells, delay, i -> v(1.0));
	}

	default CellList d(CellList cells, IntFunction<Producer<Scalar>> delay, IntFunction<Producer<Scalar>> scale) {
		return map(cells, i -> new AdjustableDelayCell(OutputLine.sampleRate, delay.apply(i), scale.apply(i)));
	}

	default CellList m(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(cells, adapter, cells::get, transmission);
	}

	default CellList mself(CellList cells, List<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(cells, adapter, cells, transmission);
	}

	default CellList m(CellList cells, List<Cell<PackedCollection<?>>> adapter, List<Cell<PackedCollection<?>>> destinations, IntFunction<Gene<PackedCollection<?>>> transmission) {
		CellList result = m(cells, adapter::get, destinations, transmission);

		if (adapter instanceof CellList) {
			result.getFinals().addAll(((CellList) adapter).getFinals());
			((CellList) adapter).getRequirements().forEach(result::addRequirement);
		}

		return result;
	}

	default CellList mself(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(cells, adapter, cells, transmission);
	}

	default CellList mself(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission, IntFunction<Cell<PackedCollection<?>>> passthrough) {
		return m(cells, adapter, cells, transmission, passthrough);
	}

	default CellList m(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter,
					   List<Cell<PackedCollection<?>>> destinations,
					   IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(cells, adapter, destinations, transmission, null);
	}

	default CellList m(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter,
					   List<Cell<PackedCollection<?>>> destinations,
					   IntFunction<Gene<PackedCollection<?>>> transmission,
					   IntFunction<Cell<PackedCollection<?>>> passthrough) {
		CellList result = m(cells, adapter, destinations::get, transmission, passthrough);

		if (destinations instanceof CellList) {
			result.getFinals().addAll(((CellList) destinations).getFinals());
			((CellList) destinations).getRequirements().forEach(result::addRequirement);
		}

		return result;
	}

	default CellList m(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter,
					   IntFunction<Cell<PackedCollection<?>>> destinations,
					   IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(cells, adapter, destinations, transmission, null);
	}

	default CellList m(CellList cells, IntFunction<Cell<PackedCollection<?>>> adapter,
					   IntFunction<Cell<PackedCollection<?>>> destinations,
					   IntFunction<Gene<PackedCollection<?>>> transmission,
					   IntFunction<Cell<PackedCollection<?>>> passthrough) {
		CellList layer = new CellList(cells);
		CellList cleanLayer = passthrough == null ? null : new CellList(layer);
		Iterator<Cell<PackedCollection<?>>> itr = cells.iterator();

		for (AtomicInteger i = new AtomicInteger(); itr.hasNext(); i.incrementAndGet()) {
			Gene g = transmission.apply(i.get());
			Cell<PackedCollection<?>> source = itr.next();
			Cell<PackedCollection<?>> clean = Optional.ofNullable(passthrough).map(p -> p.apply(i.get())).orElse(null);

			List<Cell<PackedCollection<?>>> dest = new ArrayList<>();
			IntStream.range(0, g.length()).mapToObj(j -> destinations.apply(j)).forEach(dest::add);

			layer.addRequirement(MultiCell.split(source, adapter.apply(i.get()), dest, g, clean));
			dest.forEach(c -> append(layer, c));
			if (cleanLayer != null) cleanLayer.add(clean);
		}

		return cleanLayer == null ? layer : cleanLayer;
	}

	default <T> void append(List<T> dest, T v) {
		for (T c : dest) {
			if (c == v) return;
		}

		dest.add(v);
	}

	default CellList seq(IntFunction<Producer<PackedCollection<?>>> values, Producer<Scalar> duration, int steps) {
		CellList cells = new CellList();
		cells.addRoot(new ValueSequenceCell(values, duration, steps));
		return cells;
	}

	default CellList gr(CellList cells, double duration, int segments, IntUnaryOperator choices) {
		return grid(cells, duration, segments, (IntToDoubleFunction) i -> (2.0 * choices.applyAsInt(i) + 1) / (2.0 * cells.size()));
	}

	default CellList grid(CellList cells, double duration, int segments, IntToDoubleFunction choices) {
		return grid(cells, duration, segments, (IntFunction<Producer<PackedCollection<?>>>) i -> c(choices.applyAsDouble(i)));
	}

	default CellList grid(CellList cells, double duration, int segments, IntFunction<Producer<PackedCollection<?>>> choices) {
		PackedCollection<?> out = new PackedCollection<>();
		List<Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>> cellChoices =
				cells.stream()
						.map(c -> (Function<PolymorphicAudioData, ? extends CollectionTemporalCellAdapter>) data -> (CollectionTemporalCellAdapter) c).collect(Collectors.toList());
		DynamicAudioCell cell = new DynamicAudioCell(c(1)._multiply(p(out)), cellChoices);
		ValueSequenceCell c = (ValueSequenceCell) seq(choices, v(duration), segments).get(0);
		c.setReceptor(a(p(out)));

		// WaveOutput csv = new WaveOutput(new File("value-sequence-debug.wav"));
		// c.setMeter(csv);

		// TODO  By dropping the parent, we may be losing necessary dependencies
		// TODO  However, if it is included, operations will be invoked multiple times
		// TODO  Since the new dynamic cell delegates to the operations of the
		// TODO  original cells in this current CellList
		CellList result = new CellList();
		result.addRoot(c);

		result = new CellList(result);
		result.addRoot(cell);
		// result.getFinals().add(csv.writeCsv(new File("value-sequence-debug.csv")).get());

		return result;
	}

	default Supplier<Runnable> export(CellList cells, PackedCollection<PackedCollection<?>> wavs) {
		if (wavs.getCount() != cells.size()) throw new IllegalArgumentException("Destination count must match cell count");

		cells = map(cells, i -> new ReceptorCell<>(new WaveOutput()));

		OperationList export = new OperationList("Export " + wavs.getAtomicMemLength() + " frames");
		export.add(iter(cells, wavs.getAtomicMemLength()));

		for (int i = 0; i < cells.size(); i++) {
			export.add(((WaveOutput) ((ReceptorCell) cells.get(i)).getReceptor()).export(wavs.get(i).traverseEach()));
		}

		return export;
	}

	default CellList mixdown(CellList cells, double seconds) {
		cells = map(cells, i -> new ReceptorCell<>(new WaveOutput()));

		OperationList export = new OperationList("Mixdown Export");

		PackedCollection<PackedCollection<?>> wavs = new PackedCollection(shape(cells.size(), WaveOutput.defaultTimelineFrames)).traverse(1);
		for (int i = 0; i < cells.size(); i++) {
			export.add(((WaveOutput) ((ReceptorCell) cells.get(i)).getReceptor()).export(wavs.get(i)));
		}

		OperationList setup = new OperationList(seconds + " second mixdown");
		setup.add(iter(cells, (int) (seconds * OutputLine.sampleRate), false));
		setup.add(export);

		CellList newCells = new CellList();
		newCells.addSetup(() -> setup);

		IntStream.range(0, wavs.getCount())
				.mapToObj(wavs::get)
				.map(wav -> new WaveCell(wav, OutputLine.sampleRate))
				.forEach(newCells::addRoot);
		CellList fc = cells;
		newCells.getFinals().add(() -> fc.reset());

		return newCells;
	}

	default Supplier<Runnable> min(Temporal t, double minutes) {
		return sec(t, minutes * 60);
	}

	default Supplier<Runnable> sec(Temporal t, double seconds) {
		return iter(t, (int) (seconds * OutputLine.sampleRate));
	}

	default Supplier<Runnable> sec(Temporal t, double seconds, boolean resetAfter) {
		return iter(t, (int) (seconds * OutputLine.sampleRate), resetAfter);
	}

	default ScaleFactor sf(double scale) {
		return sf(new Scalar(scale));
	}

	default ScaleFactor sf(Scalar scale) {
		return new ScaleFactor(scale);
	}

	default AudioPassFilter hp(double frequency, double resonance) {
		return hp(OutputLine.sampleRate, frequency, resonance);
	}

	default AudioPassFilter hp(int sampleRate, double frequency, double resonance) {
		return hp(sampleRate, c(frequency), v(resonance));
	}

	default AudioPassFilter hp(Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance) {
		return hp(OutputLine.sampleRate, frequency, resonance);
	}

	default AudioPassFilter hp(int sampleRate, Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance) {
		return new AudioPassFilter(sampleRate, frequency, resonance, true);
	}

	default AudioPassFilter lp(double frequency, double resonance) {
		return lp(OutputLine.sampleRate, frequency, resonance);
	}

	default AudioPassFilter lp(int sampleRate, double frequency, double resonance) {
		return lp(sampleRate, c(frequency), v(resonance));
	}

	default AudioPassFilter lp(Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance) {
		return lp(OutputLine.sampleRate, frequency, resonance);
	}

	default AudioPassFilter lp(int sampleRate, Producer<PackedCollection<?>> frequency, Producer<Scalar> resonance) {
		return new AudioPassFilter(sampleRate, frequency, resonance, false);
	}
}
