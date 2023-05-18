package org.almostrealism.audio.grains.test;

import io.almostrealism.expression.Cast;
import io.almostrealism.expression.Expression;
import io.almostrealism.expression.Product;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.grains.Grain;
import org.almostrealism.audio.grains.GrainSet;
import org.almostrealism.audio.grains.GranularSynthesizer;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.collect.computations.ExpressionComputation;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.PassThroughProducer;
import org.almostrealism.hardware.cl.HardwareOperator;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.computations.Interpolate;
import org.glassfish.grizzly.streams.Output;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GrainTest implements CellFeatures {
	@Test
	public void grainsOld() {
		WaveOutput source = new WaveOutput();
		w(new File("Library/organ.wav")).map(i -> new ReceptorCell<>(source)).sec(1.0, false).get().run();

		Grain grain = new Grain();
		grain.setStart(0.2);
		grain.setDuration(0.015);
		grain.setRate(2.0);

		TraversalPolicy grainShape = new TraversalPolicy(3);
		Producer<PackedCollection<?>> g = (Producer) v(1, 1, -1);

		Producer<Scalar> pos = scalar(grainShape, g, 0).add(
						mod(multiply(scalar(grainShape, g, 2),
								v(Scalar.shape(), 0)), scalar(grainShape, g, 1)))
				.multiply(scalar(OutputLine.sampleRate));
		Producer cursor = pair(pos, v(0.0));

		ScalarBank result = new ScalarBank(10 * OutputLine.sampleRate);
		System.out.println("GrainTest: Evaluating timeline kernel...");
		source.getData().valueAt(cursor).get().into(result).evaluate(WaveOutput.timelineScalar.getValue(), grain);
		System.out.println("GrainTest: Timeline kernel evaluated");

		System.out.println("GrainTest: Rendering grains...");
		w(new WaveData(result, OutputLine.sampleRate)).o(i -> new File("results/grain-test-old.wav")).sec(5).get().run();
		System.out.println("GrainTest: Done");
	}

	@Test
	public void grainsTimeSeries() {
		WaveOutput source = new WaveOutput();
		w(new File("Library/organ.wav")).map(i -> new ReceptorCell<>(source)).sec(1.0, false).get().run();

		Grain grain = new Grain();
		grain.setStart(0.2);
		grain.setDuration(0.015);
		grain.setRate(2.0);

		TraversalPolicy grainShape = new TraversalPolicy(3);
		Producer in = v(Scalar.shape(), 0);
		Producer<PackedCollection<?>> g = v(shape(3).traverseEach(), 1);

		CollectionProducer<Scalar> start = scalar(grainShape, g, 0);
		CollectionProducer<Scalar> duration = scalar(grainShape, g, 1);
		CollectionProducer<Scalar> rate = scalar(grainShape, g, 2);

		int frames = 240 * OutputLine.sampleRate;

//		Producer<Scalar> pos = start.add(mod(multiply(rate, in), duration))
//									.multiply(scalar(OutputLine.sampleRate));
//		Producer pos = _mod(in, c(0.5)).multiply(c(OutputLine.sampleRate));
		Producer cursor = integers(0, frames);

		PackedCollection<?> result = new PackedCollection<>(shape(frames), 1);
		System.out.println("GrainTest: Evaluating timeline kernel...");
		HardwareOperator.verboseLog(() -> {
			source.getData().valueAt(cursor).get().into(result).evaluate();
		});
		System.out.println("GrainTest: Timeline kernel evaluated");

		System.out.println("GrainTest: Rendering grains...");
		w(new WaveData(result, OutputLine.sampleRate)).o(i -> new File("results/grain-timeseries-test.wav")).sec(5).get().run();
		System.out.println("GrainTest: Done");
	}

	@Test
	public void grains() throws IOException {
		WaveData wav = WaveData.load(new File("Library/organ.wav"));

		Grain grain = new Grain();
		grain.setStart(0.2);
		grain.setDuration(0.015);
		grain.setRate(2.0);

		TraversalPolicy grainShape = new TraversalPolicy(3);
		Producer<PackedCollection<?>> g = v(shape(3).traverseEach(), 1);

		CollectionProducer<PackedCollection<?>> start = c(g, 0);
		CollectionProducer<PackedCollection<?>> duration = c(g, 1);
		CollectionProducer<PackedCollection<?>> rate = c(g, 2);

		PackedCollection<?> input = wav.getCollection();
		int frames = input.getCount(); // 5 * OutputLine.sampleRate;
		TraversalPolicy shape = shape(frames).traverse(1);

//		Producer<Scalar> pos = start.add(mod(multiply(rate, in), duration))
//									.multiply(scalar(OutputLine.sampleRate));
//		Producer pos = _mod(in, c(0.5)).multiply(c(OutputLine.sampleRate));
		Producer<PackedCollection<?>> pos = integers(0, frames); // .divide(c(OutputLine.sampleRate));
		PackedCollection<?> timeline = pos.get().evaluate();

		PackedCollection<?> r = new PackedCollection<>(1);
		r.setMem(1.0);

		PackedCollection<?> result = new PackedCollection<>(shape(frames), 1);
		System.out.println("GrainTest: Evaluating interpolate kernel...");
		HardwareOperator.verboseLog(() -> {
			Producer in = v(1, 0); // traverse(0, pos)'

			KernelizedEvaluable<PackedCollection<?>> ev =
					interpolate(in,
							v(1, 1),
							v(1, 2)).get();
			ev.into(result).evaluate(input.traverse(0), timeline, r);
		});
		System.out.println("GrainTest: Interpolate kernel evaluated");

		System.out.println("GrainTest: Rendering grains...");
		w(new WaveData(result, OutputLine.sampleRate)).o(i -> new File("results/grain-test.wav")).sec(5).get().run();
		System.out.println("GrainTest: Done");
	}

	@Test
	public void granularSynth() {
		GranularSynthesizer synth = new GranularSynthesizer();
		GrainSet set = synth.addFile("Library/organ.wav");
		set.addGrain(new Grain(0.2, 0.015, 2.0));
		synth.create(v(0.0), v(0.0), v(0.0), List.of(new Frequency(1.0)))
				.getProviders().get(0).get().save(new File("results/granular-synth-test.wav"));
	}
}
