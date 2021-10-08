package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.audio.health.OrganRunner;
import com.almostrealism.audio.optimize.LayeredOrganPopulation;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.Genome;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.OrganFactory;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LayeredOrganPopulationTest extends AdjustmentLayerOrganSystemFactoryTest {
	protected AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organFromPopulation(DesirablesProvider desirables, Receptor meter) {
		List<Genome> genomes = new ArrayList<>();
		genomes.add(layeredOrganGenome());

		LayeredOrganPopulation pop = new LayeredOrganPopulation(genomes, 2);
		pop.init(factory(desirables), genomes.get(0), meter);
		return pop.enableGenome(0);
	}

	@Test
	public void population() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-pop-test.wav")).get(0);
		AdjustmentLayerOrganSystem<Double, Scalar, Double, Scalar> organ = organFromPopulation(samples(), out);

		OrganRunner organRun = new OrganRunner(organ, OutputLine.sampleRate);
		Runnable first = organRun.get();
		Runnable next = organRun.getContinue();

		first.run();
		IntStream.range(0, 7).forEach(i -> next.run());

		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
