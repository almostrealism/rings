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
import org.almostrealism.audio.arrange.DefaultChannelSectionFactory;
import org.almostrealism.audio.optimize.AudioSceneOptimizer;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.notes.NoteAudioChoice;
import org.almostrealism.audio.util.TestUtils;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.mem.Heap;
import org.almostrealism.io.SystemUtils;
import org.almostrealism.time.TemporalRunner;
import org.almostrealism.audio.health.StableDurationHealthComputation;
import org.almostrealism.audio.optimize.AudioScenePopulation;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.graph.ReceptorCell;
import org.almostrealism.heredity.Genome;
import org.almostrealism.util.KeyUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AudioScenePopulationTest extends AdjustmentLayerOrganSystemFactoryTest {
	protected AudioScenePopulation population(AudioScene<?> scene, List<Receptor<PackedCollection<?>>> measures, Receptor output) {
		List<Genome<PackedCollection<?>>> genomes = new ArrayList<>();
		genomes.add(TestUtils.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(TestUtils.genome(0.0, 0.0, false));
		genomes.add(TestUtils.genome(0.0, 0.0, 0.0, 0.0, false));
		genomes.add(TestUtils.genome(0.0, 0.0, false));

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

	@Test
	public void createGenomes() throws IOException {
		int count = 12;

		File settings = new File(SystemUtils.getLocalDestination("scene-settings.json"));

		AudioScene scene = AudioScene.load(settings.getCanonicalPath(), SystemUtils.getLocalDestination("pattern-factory.json"),
									AudioSceneOptimizer.LIBRARY, 120, OutputLine.sampleRate);
		if (!settings.exists()) scene.saveSettings(settings);

		if (new File(AudioSceneOptimizer.POPULATION_FILE).exists()) {
			log(AudioSceneOptimizer.POPULATION_FILE + " already exists");
			return;
		}

		AudioScenePopulation pop = new AudioScenePopulation(scene,
				IntStream.range(0, count)
						.mapToObj(i -> scene.getGenome().random())
						.collect(Collectors.toList()));
		pop.store(new FileOutputStream(AudioSceneOptimizer.POPULATION_FILE));
	}

	@Test
	public void generate() throws Exception {
		DefaultChannelSectionFactory.enableVolumeRiseFall = false;
		DefaultChannelSectionFactory.enableFilter = false;
		// EfxManager.enableEfx = false;

		if (!new File(AudioSceneOptimizer.POPULATION_FILE).exists()) {
			createGenomes();
		}

		int channel = 2;
		double duration = 16;

		AudioScene scene = AudioScene.load(
				SystemUtils.getLocalDestination("scene-settings.json"),
				SystemUtils.getLocalDestination("pattern-factory.json"),
				AudioSceneOptimizer.LIBRARY, 120, OutputLine.sampleRate);

		long activeLayers = scene.getPatternManager().getPatterns()
				.stream().filter(c -> c.getChannel() == channel).filter(c -> c.getLayerCount() > 0).count();
		log("Channel " + channel + " has " + activeLayers  + " active pattern layers");

		int frames = scene.getContext(List.of(channel)).getFrameForPosition().applyAsInt(duration);

		Heap heap = new Heap(8 * 1024 * 1024);

		try {
			scene.setPatternActivityBias(1.0);
			heap.wrap(() -> {
				AudioScenePopulation pop = loadPopulation(scene);
				return pop.generate(channel, frames,
						() -> "generated/" + KeyUtils.generateKey() + ".wav",
						(result) -> log("Generated " + result.getOutputPath()));
			}).call().run();
		} finally {
			scene.setPatternActivityBias(0.0);
			heap.destroy();

			if (NoteAudioChoice.GRANULARITY_DIST != null) {
				log("Granularity distribution:");
				for (int i = 0; i < NoteAudioChoice.GRANULARITY_DIST.length; i++) {
					log("\t" + i + ": " + NoteAudioChoice.GRANULARITY_DIST[i]);
				}
			}

			if (PatternElementFactory.REPEAT_DIST != null) {
				log("Repeat distribution:");
				for (int i = 0; i < PatternElementFactory.REPEAT_DIST.length; i++) {
					log("\t" + i + ": " + PatternElementFactory.REPEAT_DIST[i]);
				}
			}
		}
	}

	protected AudioScenePopulation loadPopulation(AudioScene<?> scene) throws FileNotFoundException {
		File file = new File(AudioSceneOptimizer.POPULATION_FILE);

		if (file.exists()) {
			List<Genome<PackedCollection<?>>> genomes = AudioScenePopulation.read(new FileInputStream(file));
			log("Loaded " + genomes.size() + " genomes from " + file);
			return new AudioScenePopulation(scene, genomes);
		}

		throw new FileNotFoundException(AudioSceneOptimizer.POPULATION_FILE + " not found");
	}
}
