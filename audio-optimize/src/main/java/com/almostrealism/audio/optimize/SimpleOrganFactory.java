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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.almostrealism.audio.DesirablesProvider;
import io.almostrealism.code.ProducerComputation;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.PolymorphicAudioCell;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.data.PolymorphicAudioDataBank;
import org.almostrealism.audio.filter.AudioCellAdapter;
import org.almostrealism.audio.filter.DelayCellFactory;
import org.almostrealism.audio.sources.SineWaveCellFactory;
import org.almostrealism.audio.sources.WavCellFactory;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.CellFactory;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.OrganFactory;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.util.Ops;

public class SimpleOrganFactory<T, C> implements OrganFactory<T, SimpleOrgan<T>>, CodeFeatures {
	public static final int minNoteLength = 40;
	public static final int maxNoteLength = 800;
	
	private final CellFactory<Double, T, C> generator;
	private final CellFactory<Double, T, C> processor;
	private final C config;

	public SimpleOrganFactory(CellFactory<Double, T, C> generator, CellFactory<Double, T, C> processor) {
		this(generator, processor, null);
	}
	
	public SimpleOrganFactory(CellFactory<Double, T, C> generator, CellFactory<Double, T, C> processor, C config) {
		this.generator = generator;
		this.processor = processor;
		this.config = config;
	}

	@Override
	public SimpleOrgan<T> generateOrgan(Genome genome) {
		return generateOrgan((Chromosome<Double>) genome.valueAt(0),
				(Chromosome<Double>) genome.valueAt(1),
				(Chromosome<T>) genome.valueAt(2),
				(Chromosome<T>) genome.valueAt(3));
	}

	public SimpleOrgan<T> generateOrgan(Chromosome<Double> generators, Chromosome<Double> processors,
										Chromosome<T> transmission, Chromosome<T> filters) {
		List<Cell<T>> generatorCells = IntStream.range(0, generators.length())
				.mapToObj(i -> generator.generateCell(generators.valueAt(i), config))
				.collect(Collectors.toList());
		List<Cell<T>> processorCells = IntStream.range(0, processors.length())
				.mapToObj(i -> processor.generateCell(processors.valueAt(i), config))
				.collect(Collectors.toList());
		return new SimpleOrgan<>(generatorCells, processorCells, transmission, filters);
	}

	public static SimpleOrganFactory<Scalar, DesirablesProvider> getDefault(DesirablesProvider provider) {
		List<CellFactory> choices = new ArrayList<>();

		if (!provider.getFrequencies().isEmpty()) {
			provider.getFrequencies().stream()
					.map(freq -> new SineWaveCellFactory(0, 0, minNoteLength, maxNoteLength,
														Collections.singleton(freq), -1, 1))
					.forEach(choices::add);
		}

		if (!provider.getSamples().isEmpty()) {
			provider.getSamples().stream()
					.map(sample -> new WavCellFactory(Collections.singletonList(sample)))
					.forEach(choices::add);
		}

		Supplier<PolymorphicAudioData> dataSupplier = new Supplier<>() {
			private final int SIZE = 50;
			private final PolymorphicAudioDataBank bank = new PolymorphicAudioDataBank(SIZE);
			private int count = 0;

			@Override
			public PolymorphicAudioData get() {
				if (count >= SIZE) throw new IllegalArgumentException("No more audio data space available");
				return bank.get(count++);
			}
		};

		return new SimpleOrganFactory((CellFactory<Scalar, Scalar, Supplier<PolymorphicAudioData>>) (g, config) ->
				new PolymorphicAudioCell(config.get(), (ProducerComputation<Scalar>) g.valueAt(0).getResultant(Ops.ops().v(1.0)),
					choices.stream()
							.map(c ->
									(Function<PolymorphicAudioData, ? extends AudioCellAdapter>) data -> (AudioCellAdapter) c.generateCell(g, data))
							.collect(Collectors.toList())), new DelayCellFactory(1), dataSupplier);
	}
}
