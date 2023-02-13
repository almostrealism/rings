package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.optimize.AudioSceneGenome;
import org.almostrealism.audio.optimize.CellularAudioOptimizer;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.CellularTemporalFactor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.util.TestFeatures;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

public class DefaultAudioGenomeTest implements HeredityFeatures, TestFeatures {
	@Test
	public void delay() {
		ArrayListChromosome<PackedCollection<?>> generators = new ArrayListChromosome();
		generators.add(g(0.4, 0.6));
		generators.add(g(0.8, 0.2));

		ArrayListChromosome<PackedCollection<?>> processors = new ArrayListChromosome();
		processors.add(g(1.0, 0.35));
		processors.add(g(1.0, 0.31));

		ArrayListChromosome<PackedCollection<?>> transmission = new ArrayListChromosome();
		transmission.add(g(0.0, 0.4));
		transmission.add(g(0.4, 0.0));

		ArrayListChromosome<Double> filters = new ArrayListChromosome();
		filters.add(new ArrayListGene<>(0.0, 1.0));
		filters.add(new ArrayListGene<>(0.0, 1.0));

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(c(g(1.0), g(1.0))); // VOLUME
//		genome.add(c(g())) TODO  WET IN
		genome.add(processors);
		genome.add(transmission);
		genome.add(c(g(1.0), g(1.0))); // WET
		genome.add(filters);

		DefaultAudioGenome organGenome = new DefaultAudioGenome(2, 2, null);
		organGenome.assignTo(genome);

		Gene<?> delayGene = organGenome.valueAt(DefaultAudioGenome.PROCESSORS).valueAt(0);
		Scalar result = (Scalar) delayGene.valueAt(1).getResultant((Producer) v(60)).get().evaluate();
		System.out.println(result);
		Assert.assertTrue(Math.abs(2.687736711 - result.getValue()) < 0.001);
	}

	@Test
	public void generated() {
		AudioScene<?> scene = new AudioScene<>(null, 120, 2, 2, OutputLine.sampleRate);
		DefaultAudioGenome g = new DefaultAudioGenome(2, 2, null);
		g.assignTo(AudioSceneGenome.generator(scene).get().get());
		g.setup().get().run();

		CellularTemporalFactor<PackedCollection<?>> volume = (CellularTemporalFactor<PackedCollection<?>>) g.valueAt(DefaultAudioGenome.VOLUME, 0, 0);
		volume.setup().get().run();
		Evaluable<PackedCollection<?>> result = volume.getResultant(c(1.0)).get();
		Runnable tick = volume.tick().get();

		for (int n = 0; n < 10; n++) {
			System.out.println("After " + n * 5 + " seconds: " + result.evaluate());
			IntStream.range(0, 5 * OutputLine.sampleRate).forEach(i -> tick.run());
		}
	}

	@Test
	public void setup() {
		AudioScene<?> scene = new AudioScene<>(null, 120, 2, 2, OutputLine.sampleRate);

		IntStream.range(0, 10).forEach(i -> {
			DefaultAudioGenome genome = new DefaultAudioGenome(2, 2, OutputLine.sampleRate, null);
			genome.assignTo(AudioSceneGenome.generator(scene).get().get());
			genome.setup().get().run();
			System.out.println("DefaultAudioGenomeTest: Setup " + i + " complete");
		});
	}
}
