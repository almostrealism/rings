package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.filter.test.AssignableGenomeTest;
import com.almostrealism.audio.health.OrganRunner;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.time.Temporal;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LayeredOrganPopulationTest extends AdjustmentLayerOrganSystemFactoryTest {
	protected LayeredOrganPopulation population(DesirablesProvider desirables, Receptor meter) {
		List<Genome> genomes = new ArrayList<>();
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));

		LayeredOrganPopulation pop = new LayeredOrganPopulation(genomes, 2);
		pop.init(factory(desirables), genomes.get(0), meter);
		return pop;
	}

	@Test
	public void genomesFromPopulation() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-pop-test.wav")).get(0);
		LayeredOrganPopulation pop = population(notes(), out);

		OrganRunner organRun = new OrganRunner(pop.enableGenome(0), OutputLine.sampleRate);
		pop.disableGenome();

		IntStream.range(0, 4).forEach(i -> {
			pop.enableGenome(i);
			Runnable first = organRun.get();
			Runnable next = organRun.getContinue();

			first.run();
			IntStream.range(0, 7).forEach(j -> next.run());

			((WaveOutput) out.getReceptor()).write().get().run();
			((WaveOutput) out.getReceptor()).reset();

			pop.disableGenome();
		});
	}

	@Test
	public void genomesFromPopulationHealth() {
		AtomicInteger index = new AtomicInteger();

		StableDurationHealthComputation health = new StableDurationHealthComputation();
		health.setMaxDuration(8);
		health.setDebugOutputFile(() -> "layered-organ-pop-health-test" + index.incrementAndGet() + ".wav");

		LayeredOrganPopulation pop = population(notes(), health.getMonitor());

		IntStream.range(0, 4).forEach(i -> {
			Temporal organ = pop.enableGenome(i);
			health.computeHealth(organ);
			pop.disableGenome();
		});
	}
}
