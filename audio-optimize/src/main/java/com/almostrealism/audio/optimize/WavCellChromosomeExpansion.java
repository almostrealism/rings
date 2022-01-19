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

import com.almostrealism.audio.health.HealthComputationAdapter;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.ScalarTable;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.sources.WavCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.MemoryDataTemporalCellularChromosomeExpansion;
import org.almostrealism.hardware.ContextSpecific;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WavCellChromosomeExpansion extends MemoryDataTemporalCellularChromosomeExpansion<ScalarBank, Scalar, Scalar> implements CellFeatures {

	private int sampleRate;

	public WavCellChromosomeExpansion(Chromosome<Scalar> source, int inputGenes, int inputFactors, int sampleRate) {
		super(Scalar.class, source, 2, ScalarBank::new, ScalarTable::new, inputGenes, inputFactors);
		this.sampleRate = sampleRate;
	}

	@Override
	public Supplier<Runnable> setup() {
		return () -> () -> setTimeline(WaveOutput.timeline.getValue());
	}

	@Override
	protected Cell<Scalar> cell(ScalarBank data) {
		return new WavCell(data, sampleRate);
	}

	@Override
	protected Producer<ScalarBank> parameters(Gene<Scalar> gene) {
		return scalars(IntStream.range(0, gene.length()).mapToObj(gene).map(f -> f.getResultant(v(1.0))).toArray(Producer[]::new));
	}

	@Override
	protected Supplier<Scalar> value() {
		return Scalar::new;
	}

	@Override
	protected BiFunction<Producer<Scalar>, Producer<Scalar>, Producer<Scalar>> combine() {
		return this::scalarsMultiply;
	}
}
