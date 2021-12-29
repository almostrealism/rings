package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.filter.test.AssignableGenomeTest;
import org.almostrealism.time.TemporalRunner;
import com.almostrealism.audio.health.StableDurationHealthComputation;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.Genome;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LayeredOrganPopulationTest extends AdjustmentLayerOrganSystemFactoryTest {
	protected LayeredOrganPopulation population(DesirablesProvider desirables, List<Receptor> measures, Receptor output) {
		List<Genome> genomes = new ArrayList<>();
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));

		LayeredOrganPopulation pop = new LayeredOrganPopulation(genomes, 2, 2);
		pop.init(factory(desirables), genomes.get(0), measures, output);
		return pop;
	}

	@Test
	public void genomesFromPopulation() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-pop-test.wav")).get(0);
		LayeredOrganPopulation pop = population(notes(), null, out); // TODO

		TemporalRunner organRun = new TemporalRunner(pop.enableGenome(0), OutputLine.sampleRate);
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
		health.setOutputFile(() -> "layered-organ-pop-health-test" + index.incrementAndGet() + ".wav");

		LayeredOrganPopulation pop = population(notes(), null, health.getOutput()); // TODO

		IntStream.range(0, 4).forEach(i -> {
			health.setTarget(pop.enableGenome(i));
			health.computeHealth();
			pop.disableGenome();
		});
	}
}
