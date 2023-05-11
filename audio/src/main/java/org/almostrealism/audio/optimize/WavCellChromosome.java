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

import io.almostrealism.code.ComputeRequirement;
import io.almostrealism.code.ProducerComputation;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.KernelList;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.computations.Assignment;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WavCellChromosome implements Chromosome<PackedCollection<?>>, Temporal, Setup, CellFeatures {
	private Chromosome<PackedCollection<?>> source;
	private Map<Integer, Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>>> transforms;

	private ArrayListChromosome<PackedCollection<?>> destination;

	private KernelList<PackedCollection<?>> kernels;
	private int inputGenes, inputFactors;
	private IntFunction<MemoryBank<PackedCollection<?>>> bankProvider;
	private BiFunction<Integer, Integer, MemoryBank<PackedCollection<?>>> tableProvider;

	private int sampleRate;
	private Producer<Scalar> time;

	public WavCellChromosome(Chromosome<PackedCollection<?>> source, int inputFactors, int sampleRate) {
		this.source = source;
		this.transforms = new HashMap<>();
		this.inputGenes = source.length();
		this.inputFactors = inputFactors;
		this.bankProvider = PackedCollection.bank(new TraversalPolicy(1));
		this.tableProvider = PackedCollection.table(new TraversalPolicy(1), (delegateSpec, width) ->
				new PackedCollection<>(new TraversalPolicy(width, 1), 1, delegateSpec.getDelegate(), delegateSpec.getOffset()));
		this.sampleRate = sampleRate;
	}

	public Chromosome<PackedCollection<?>> getSource() {return source;}

	public void setTransform(int factor, Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>> transform) {
		this.transforms.put(factor, transform);
	}

	public void setGlobalTime(Producer<Scalar> time) {
		this.time = time;
	}

	public void setTimeline(PackedCollection<PackedCollection<?>> timeline) { kernels.setInput(timeline); }

	public KernelList<PackedCollection<?>> getKernelList() { return kernels; }

	public int getFactorCount() { return 1; }

	public void setFactor(BiFunction<Producer<MemoryBank<PackedCollection<?>>>, Producer<PackedCollection<?>>, ProducerComputation<PackedCollection<?>>> computation) {
		this.kernels = new KernelList(bankProvider, tableProvider, computation, inputGenes, inputFactors);
	}

	public Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>> id(int index) {
		return g -> (Producer<PackedCollection<?>>) g.valueAt(index).getResultant(c(1.0));
	}

	public Supplier<Runnable> expand() {
		OperationList prepare = new OperationList("TemporalChromosome Preparation");
		prepare.add(setup());
		prepare.add(() -> () -> {
			ArrayListChromosome<PackedCollection<?>> destination = new ArrayListChromosome<>();

			for (int i = 0; i < source.length(); i++) {
				destination.add(new ArrayListGene<>());
			}

			for (int i = 0; i < transforms.size(); i++) {
				for (int j = 0; j < source.length(); j++) {
					ArrayListGene g = (ArrayListGene) destination.get(j);
					Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>> transform = transforms.get(i);
					Gene<PackedCollection<?>> input = source.valueAt(j);
					g.add(protein -> transform.apply(input));
				}
			}

			this.destination = IntStream.range(0, destination.size())
					.mapToObj(i -> assemble(i, destination.valueAt(i)))
					.collect(Collectors.toCollection(ArrayListChromosome::new));
		});

		prepare.get().run();
		return process();
	}

	private Gene<PackedCollection<?>> assemble(int pos, Gene<PackedCollection<?>> transformed) {
		ArrayListGene<PackedCollection<?>> result = new ArrayListGene<>();

		kernels.setParameters(pos, parameters(transformed));
		WaveCell cell = new WaveCell((PackedCollection) kernels.valueAt(pos), sampleRate, time);
		Factor<PackedCollection<?>> factor = cell.toFactor(Scalar::new,
				p -> protein -> new Assignment<>(1, p, protein), combine());
		// return cell.toFactor();
		result.add(factor);

		return result;
	}

	@Override
	public Supplier<Runnable> tick() {
		System.out.println("WavCellChromosome.tick()");
		return destination.stream()
				.flatMap(g -> IntStream.range(0, g.length()).mapToObj(g::valueAt))
				.map(f -> f instanceof TemporalFactor ? (TemporalFactor) f : null)
				.filter(Objects::nonNull)
				.map(Temporal::tick)
				.collect(OperationList.collector());
	}

	public TemporalList getTemporals() {
		TemporalList all = destination.stream()
				.flatMap(g -> IntStream.range(0, g.length()).mapToObj(g::valueAt))
				.map(f -> f instanceof Temporal ? (Temporal) f : null)
				.filter(Objects::nonNull)
				.collect(TemporalList.collector());
		return all;
	}

	@Override
	public Gene<PackedCollection<?>> valueAt(int pos) { return destination.valueAt(pos); }

	@Override
	public int length() { return destination.length(); }

	protected Supplier<Runnable> process() {
		Runnable run = kernels.get();

		return () -> () -> {
			if (cc().isKernelSupported()) {
				run.run();
			} else {
				cc(() -> kernels.get().run(), ComputeRequirement.CL);
			}
		};
	}

	protected WaveCell cell(PackedCollection<PackedCollection<?>> data) {
		return new WaveCell(data, sampleRate, time);
	}

	@Override
	public Supplier<Runnable> setup() {
		return () -> () -> setTimeline(WaveOutput.timeline.getValue());
	}

	protected Producer<PackedCollection<PackedCollection<?>>> parameters(Gene<PackedCollection<?>> gene) {
		return concat(IntStream.range(0, gene.length()).mapToObj(gene).map(f -> f.getResultant(c(1.0))).toArray(Producer[]::new));
	}

	protected BiFunction<Producer<PackedCollection<?>>, Producer<PackedCollection<?>>, Producer<PackedCollection<?>>> combine() {
		return (a, b) -> (Producer) toScalar(a).multiply(toScalar(b));
		// return (a, b) -> a;
	}
}
