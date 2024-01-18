/*
 * Copyright 2024 Michael Murray
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

import io.almostrealism.code.ProducerComputation;
import io.almostrealism.collect.Shape;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Provider;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.KernelList;
import org.almostrealism.hardware.MemoryBank;
import org.almostrealism.hardware.MemoryData;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.PassThroughProducer;
import org.almostrealism.hardware.computations.Assignment;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.Chromosome;
import io.almostrealism.relation.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.TemporalFactor;
import org.almostrealism.io.TimingMetric;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Deprecated
public class WavCellChromosome implements Chromosome<PackedCollection<?>>, Temporal, Setup, CellFeatures {
	public static TimingMetric timing = CellFeatures.console.timing("WavCellChromosome");
	public static boolean enableKernels = true;

	private ProducerComputation<PackedCollection<?>> computation;
	private BiFunction<Producer<MemoryBank<PackedCollection<?>>>, Producer<PackedCollection<?>>, ProducerComputation<PackedCollection<?>>> computationProvider;

	private PackedCollection<PackedCollection<?>> input;
	private MemoryBank<PackedCollection<?>> parameters;
	private MemoryBank<PackedCollection<MemoryData>> data;

	private Map<Integer, Producer<PackedCollection<PackedCollection<?>>>> parameterValues;

	private Chromosome<PackedCollection<?>> source;
	private Map<Integer, Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>>> transforms;

	private ArrayListChromosome<PackedCollection<?>> destination;

	private int inputGenes;

	private int sampleRate;
	private TimeCell time;

	public WavCellChromosome(Chromosome<PackedCollection<?>> source, int inputFactors, int sampleRate) {
		this.source = source;
		this.transforms = new HashMap<>();
		this.inputGenes = source.length();
		this.parameters = inputFactors > 0 ?
				PackedCollection.bank(new TraversalPolicy(1)).apply(inputFactors) : null;
		this.parameterValues = new HashMap<>();
		this.sampleRate = sampleRate;
	}

	public Chromosome<PackedCollection<?>> getSource() { return source; }

	public void setTransform(int factor, Function<Gene<PackedCollection<?>>, Producer<PackedCollection<?>>> transform) {
		this.transforms.put(factor, transform);
	}

	public void setGlobalTime(TimeCell time) {
		this.time = time;
	}

	public void setTimeline(PackedCollection<PackedCollection<?>> timeline) {
		this.input = timeline;
		this.data = PackedCollection.table(new TraversalPolicy(1), (delegateSpec, width) ->
				new PackedCollection<>(new TraversalPolicy(width, 1), 1,
						delegateSpec.getDelegate(), delegateSpec.getOffset()))
				.apply(input.getCount(), inputGenes);

		TraversalPolicy shape = ((Shape) input).getShape();
		TraversalPolicy parameterShape = ((Shape) parameters).getShape();
		this.computation = computationProvider.apply(
				new PassThroughProducer<>(parameterShape.traverse(0), 1),
				new PassThroughProducer<>(shape, 0));
	}

	public void setFactor(BiFunction<Producer<MemoryBank<PackedCollection<?>>>, Producer<PackedCollection<?>>, ProducerComputation<PackedCollection<?>>> computation) {
		this.computationProvider = computation;
	}

	public void setParameters(int pos, Producer<PackedCollection<PackedCollection<?>>> parameters) {
		this.parameterValues.put(pos, parameters);
	}

	public Supplier<Runnable> assignParameters(Producer<PackedCollection<PackedCollection<?>>> parameters) {
		return new Assignment<>(this.parameters.getMemLength(), () -> new Provider(this.parameters), parameters);
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

		long start = System.nanoTime();

		try {
			prepare.get().run();
		} finally {
			timing.addEntry("prepare", System.nanoTime() - start);
		}

		return process();
	}

	private Gene<PackedCollection<?>> assemble(int pos, Gene<PackedCollection<?>> transformed) {
		setParameters(pos, parameters(transformed));
		WaveCell cell = new WaveCell(PolymorphicAudioData.supply(PackedCollection.factory()).get(),
				data.get(pos), sampleRate, 1.0, time.frame());
		Factor<PackedCollection<?>> factor = cell.toFactor(() -> new Scalar(0.0),
				p -> protein -> new Assignment<>(1, p, protein), combine());

		ArrayListGene<PackedCollection<?>> result = new ArrayListGene<>();
		result.add(factor);
		return result;
	}

	@Override
	public Supplier<Runnable> tick() {
		throw new UnsupportedOperationException();
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
		Evaluable<PackedCollection<?>> ev = computation.get();

		OperationList op = new OperationList("KernelList Parameter Assignments and Kernel Evaluations");
		IntStream.range(0, inputGenes).forEach(i -> {
			if (parameterValues.containsKey(i)) op.add(assignParameters(parameterValues.get(i)));
			op.add(() -> () -> {
				if (enableKernels) {
					ev.into(data.get(i)).evaluate(input, ((Shape) this.parameters).traverse(0));
				} else {
					ev.into(((Shape) data.get(i)).traverse(0))
							.evaluate(((Shape) input).traverse(0),
									((Shape) this.parameters).traverse(0));
				}
			});
		});

		Runnable run = op.get();

		return () -> () -> {
			long start = System.nanoTime();

			try {
				run.run();
			} finally {
				timing.addEntry("process", System.nanoTime() - start);
			}
		};
	}

	protected WaveCell cell(PackedCollection<PackedCollection<?>> data) {
		return new WaveCell(data, sampleRate, time.frame());
	}

	@Override
	public Supplier<Runnable> setup() {
		return () -> () -> setTimeline(WaveOutput.timeline.getValue());
	}

	protected Producer<PackedCollection<PackedCollection<?>>> parameters(Gene<PackedCollection<?>> gene) {
		return concat(IntStream.range(0, gene.length()).mapToObj(gene).map(f -> f.getResultant(c(1.0))).toArray(Producer[]::new));
	}

	protected BiFunction<Producer<PackedCollection<?>>, Producer<PackedCollection<?>>, Producer<PackedCollection<?>>> combine() {
		return (a, b) -> (Producer) scalarsMultiply(toScalar(a), toScalar(b));
	}
}
