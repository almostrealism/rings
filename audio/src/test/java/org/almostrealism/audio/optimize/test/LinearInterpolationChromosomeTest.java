package org.almostrealism.audio.optimize.test;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.audio.optimize.LinearInterpolationChromosome;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.TemporalFactorFromCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.heredity.SimpleGene;
import org.almostrealism.time.TemporalList;
import org.almostrealism.util.TestFeatures;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LinearInterpolationChromosomeTest implements CellFeatures, TestFeatures {
	public static int channels = 1;
	public static int channel = 0;

	public static double totalDuration = 16.0;
	public static double duration = 8.0;

	public static boolean enableDebugReceptor = false;

	protected LinearInterpolationChromosome chromosome(TimeCell clock) {
		SimpleChromosome chromosome = new ConfigurableGenome().addSimpleChromosome(LinearInterpolationChromosome.SIZE);

		for (int i = 0; i < channels; i++) {
			SimpleGene g = chromosome.addGene();
			g.set(0, 0.0);
			g.set(1, 1.0);
		}

		LinearInterpolationChromosome interpolate =
				new LinearInterpolationChromosome(chromosome, 2000.0, 20000.0, OutputLine.sampleRate);
		interpolate.setGlobalTime(clock.frame());
		return interpolate;
	}

	@Test
	public void lowPassFilter() throws IOException {
		TimeCell clock = new TimeCell();
		LinearInterpolationChromosome lowPassFilter = chromosome(clock);

		double time = 0.5;

		OperationList setup = new OperationList();
		setup.add(() -> () -> lowPassFilter.setDuration(10 * time));
		setup.add(lowPassFilter.expand());

		TemporalList temporals = lowPassFilter.getTemporals();

		WaveData data = WaveData.load(new File("Library/Snare Perc DD.wav"));
		PackedCollection<?> input = data.getCollection();

		Factor<PackedCollection<?>> factor = lowPassFilter.valueAt(channel, 0);
		setup.add(((Setup) factor).setup());

		CellList cells = cells(1, i -> new WaveCell(input.traverseEach(), OutputLine.sampleRate))
				.addSetup((Setup) factor)
				.addRequirements(clock, temporals)
				.map(fc(i -> lp(factor.getResultant(c(1.0)),
						v(DefaultAudioGenome.defaultResonance))))
				.o(i -> new File("results/lowpass-filter-interpolation.wav"));

		OperationList op = new OperationList();
		op.add(setup);
		op.add(cells.sec(0.5, true));
		op.get().run();

		Scalar count = clock.frame().get().evaluate();
		System.out.println("Clock: " + count.toDouble(0) + " frames after " + time + " seconds");
		assertEquals(22050.0, count);
	}

	@Test
	public void interpolate() {
		TimeCell clock = new TimeCell();
		LinearInterpolationChromosome interpolate = chromosome(clock);

		OperationList setup = new OperationList();
		setup.add(() -> () -> interpolate.setDuration(totalDuration));
		setup.add(interpolate.expand());

		TemporalList temporal = new TemporalList();
		temporal.add(clock);
		temporal.add(interpolate.getTemporals());

		PackedCollection<?> out = new PackedCollection<>(1);
		TemporalFactorFromCell factor = (TemporalFactorFromCell) interpolate.valueAt(channel, 0);
		Producer<PackedCollection<?>> result = factor.getResultant(c(1.0));
		setup.add(factor.setup());

		OperationList process = new OperationList();
		process.add(sec(temporal, duration, false));
		process.add(a(1, p(out), result));

		setup.get().run();
		process.get().run();

		System.out.println("Value after " + duration + " seconds: " + out.toDouble(0));

		Scalar count = clock.frame().get().evaluate();
		System.out.println("Clock: " + count.toDouble(0) + " frames after " + duration + " seconds");

		Assert.assertFalse(out.toDouble(0) == 0.0);
		Assert.assertFalse(Double.isNaN(out.toDouble(0)));
		Assert.assertFalse(Double.isInfinite(out.toDouble(0)));
		assertEquals(duration * OutputLine.sampleRate, count);
	}

	@Test
	public void waveCell() {
		TimeCell clock = new TimeCell();
		LinearInterpolationChromosome interpolate = chromosome(clock);

		OperationList setup = new OperationList();
		setup.add(() -> () -> interpolate.setDuration(totalDuration));
		setup.add(interpolate.expand());

		TemporalFactorFromCell factor = (TemporalFactorFromCell) interpolate.valueAt(channel, 0);
		WaveCell cell = (WaveCell) factor.getCell();
		setup.add(cell.setup());

		TemporalList temporal = new TemporalList();
		temporal.add(() -> cell.push(c(0.0)));
		temporal.add(clock);
		temporal.add(cell);

		Producer<PackedCollection<?>> result = factor.getResultant(c(1.0));
		PackedCollection<?> out = new PackedCollection<>(1);

		if (enableDebugReceptor) {
			cell.setReceptor(protein -> a(1, p(out), protein));
		}

		OperationList process = new OperationList();
		process.add(sec(temporal, duration, false));
		if (!enableDebugReceptor) process.add(a(1, p(out), result));

		setup.get().run();
		process.get().run();

		System.out.println("Value after " + duration + " seconds: " + out.toDouble(0));

		Scalar count = clock.frame().get().evaluate();
		System.out.println("Clock: " + count.toDouble(0) + " frames after " + duration + " seconds");

		Assert.assertFalse(out.toDouble(0) == 0.0);
		Assert.assertFalse(Double.isNaN(out.toDouble(0)));
		Assert.assertFalse(Double.isInfinite(out.toDouble(0)));
		assertEquals(duration * OutputLine.sampleRate, count);
	}
}
