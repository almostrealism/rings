package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.optimize.DefaultAudioGenome;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.ArrayListGenome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.HeredityFeatures;
import org.almostrealism.util.TestFeatures;
import org.junit.Assert;
import org.junit.Test;

public class DefaultAudioGenomeTest implements HeredityFeatures, TestFeatures {
	@Test
	public void delay() {
		ArrayListChromosome<Scalar> generators = new ArrayListChromosome();
		generators.add(g(0.4, 0.6));
		generators.add(g(0.8, 0.2));

		ArrayListChromosome<Scalar> processors = new ArrayListChromosome();
		processors.add(g(1.0, 0.35));
		processors.add(g(1.0, 0.31));

		ArrayListChromosome<Scalar> transmission = new ArrayListChromosome();
		transmission.add(g(0.0, 0.4));
		transmission.add(g(0.4, 0.0));

		ArrayListChromosome<Double> filters = new ArrayListChromosome();
		filters.add(new ArrayListGene<>(0.0, 1.0));
		filters.add(new ArrayListGene<>(0.0, 1.0));

		ArrayListGenome genome = new ArrayListGenome();
		genome.add(generators);
		genome.add(c(g(1.0), g(1.0))); // VOLUME
		genome.add(processors);
		genome.add(transmission);
		genome.add(c(g(1.0), g(1.0))); // WET
		genome.add(filters);

		DefaultAudioGenome organGenome = new DefaultAudioGenome(2, 2);
		organGenome.assignTo(genome);

		Gene<?> delayGene = organGenome.valueAt(DefaultAudioGenome.PROCESSORS).valueAt(0);
		Scalar result = (Scalar) delayGene.valueAt(1).getResultant((Producer) v(60)).get().evaluate();
		System.out.println(result);
		Assert.assertTrue(Math.abs(2.687736711 - result.getValue()) < 0.001);
	}
}
