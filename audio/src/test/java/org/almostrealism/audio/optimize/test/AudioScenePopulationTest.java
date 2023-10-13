/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.filter.test.AssignableGenomeTest;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.audio.optimize.AudioScenePopulation;
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

public class AudioScenePopulationTest extends AdjustmentLayerOrganSystemFactoryTest {
	protected AudioScenePopulation population(AudioScene<?> scene, List<Receptor<PackedCollection<?>>> measures, Receptor output) {
		List<Genome<PackedCollection<?>>> genomes = new ArrayList<>();
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(AssignableGenomeTest.genome(0.0, 0.0, false));

		AudioScenePopulation pop = new AudioScenePopulation(scene, genomes);
		pop.init(genomes.get(0), measures, null, output);
		return pop;
	}

	@Test
	public void genomesFromPopulation() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("layered-organ-pop-test.wav")).get(0);
		AudioScenePopulation pop = population(pattern(1, 1), null, out); // TODO

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

		StableDurationHealthComputation health = new StableDurationHealthComputation(1);
		health.setMaxDuration(8);
		health.setOutputFile(() -> "results/layered-organ-pop-health-test" + index.incrementAndGet() + ".wav");

		AudioScenePopulation pop = population(pattern(1, 1), null, health.getOutput()); // TODO

		IntStream.range(0, 4).forEach(i -> {
			health.setTarget(pop.enableGenome(i));
			health.computeHealth();
			pop.disableGenome();
		});
	}
}
