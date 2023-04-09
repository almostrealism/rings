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

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.MemoryDataTemporalCellularChromosomeExpansion;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class WavCellChromosomeExpansion extends
		MemoryDataTemporalCellularChromosomeExpansion<PackedCollection<PackedCollection<?>>, PackedCollection<?>, PackedCollection<?>> implements CellFeatures {

	private int sampleRate;
	private Producer<Scalar> time;

	// TODO  Can't inputGenes just be inferred via source::length?
	public WavCellChromosomeExpansion(Chromosome<PackedCollection<?>> source, int inputGenes, int inputFactors, int sampleRate) {
		super((Class) PackedCollection.class, source, 1, PackedCollection.bank(new TraversalPolicy(1)),
				PackedCollection.table(new TraversalPolicy(1), (delegateSpec, width) ->
						new PackedCollection<>(new TraversalPolicy(width, 1), 1, delegateSpec.getDelegate(), delegateSpec.getOffset())),
				inputGenes, inputFactors);
		this.sampleRate = sampleRate;
	}

	public void setGlobalTime(Producer<Scalar> time) {
		this.time = time;
	}

	@Override
	public Supplier<Runnable> setup() {
		return () -> () -> setTimeline(WaveOutput.timeline.getValue());
	}

	@Override
	protected Cell<PackedCollection<?>> cell(PackedCollection<PackedCollection<?>> data) {
		return new WaveCell(data, sampleRate, time);
	}

	@Override
	protected Producer<PackedCollection<PackedCollection<?>>> parameters(Gene<PackedCollection<?>> gene) {
		return c(IntStream.range(0, gene.length()).mapToObj(gene).map(f -> f.getResultant(c(1.0))).toArray(Producer[]::new));
	}

	@Override
	protected Supplier<PackedCollection<?>> value() {
		return Scalar::new;
	}

	@Override
	protected BiFunction<Producer<PackedCollection<?>>, Producer<PackedCollection<?>>, Producer<PackedCollection<?>>> combine() {
		return this::multiply;
	}
}
