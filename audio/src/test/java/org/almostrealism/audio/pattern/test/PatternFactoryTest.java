/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.pattern.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.notes.FileNoteSource;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.PatternFactoryChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.Scale;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.PackedCollectionHeap;
import org.almostrealism.heredity.SimpleChromosome;
import org.almostrealism.time.Frequency;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatternFactoryTest implements CellFeatures {

	@Test
	public void fixChoices() throws IOException {
		List<PatternFactoryChoice> choices = readChoices();

		choices.forEach(c -> {
			if ("Bass".equals(c.getFactory().getName())) {
				c.setGranularity(4.0);
				c.setSeedScale(4.0);
				c.setSeedBias(0.0);
				c.setMaxScale(8.0);
			}
		});

		new ObjectMapper().writeValue(new File("pattern-factory-new.json"), choices);
	}

	// @Test
	public void consolidateChoices() throws IOException {
		List<PatternFactoryChoice> choices = readChoices();

		Map<String, List<File>> dirs = new HashMap<>();

		choices.forEach(c -> {
			dirs.put(c.getFactory().getName().replaceAll(" ", "_"),
					c.getFactory().getSources().stream().map(PatternNoteSource::getOrigin).map(File::new).collect(Collectors.toList()));
		});

		dirs.forEach((name, files) -> {
			File root = new File("pattern-factory/" + name);
			root.mkdirs();

			files.forEach(file -> {
				try {
					Files.copy(file.toPath(),
							new File(root, file.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					System.out.println("Copied " + file.getName());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		});
	}

	public static List<PatternFactoryChoice> createChoices() {
		List<PatternFactoryChoice> choices = new ArrayList<>();

		PatternFactoryChoice kick = new PatternFactoryChoice(new PatternElementFactory("Kicks", PatternNote.create("Kit/Kick.wav")));
		kick.setSeed(true);
		kick.setMinScale(0.25);
		choices.add(kick);

		PatternFactoryChoice clap = new PatternFactoryChoice(new PatternElementFactory("Clap/Snare", PatternNote.create("Kit/Clap.wav")));
		clap.setMaxScale(0.5);
		choices.add(clap);

		PatternFactoryChoice toms = new PatternFactoryChoice(
				new PatternElementFactory("Toms", PatternNote.create("Kit/Tom1.wav"),
						PatternNote.create("Kit/Tom2.wav")));
		toms.setMaxScale(0.25);
		choices.add(toms);

		PatternFactoryChoice hats = new PatternFactoryChoice(new PatternElementFactory("Hats"));
		hats.setMaxScale(0.25);
		choices.add(hats);

		return choices;
	}

	public PatternFactoryChoiceList readChoices() throws IOException {
		return new ObjectMapper().readValue(new File("pattern-factory.json"), PatternFactoryChoiceList.class);
	}

	@Test
	public void storeChoices() throws IOException {
		new ObjectMapper().writeValue(new File("pattern-factory.json"), createChoices());
	}

	@Test
	public void runLayers() throws IOException {
		Frequency bpm = bpm(120);

		WaveData.setCollectionHeap(() -> new PackedCollectionHeap(600 * OutputLine.sampleRate), PackedCollectionHeap::destroy);
		PackedCollection destination = new PackedCollection((int) (bpm.l(16) * OutputLine.sampleRate));

		List<PatternFactoryChoice> choices = readChoices();

		DefaultKeyboardTuning tuning = new DefaultKeyboardTuning();
		choices.forEach(c -> c.setTuning(tuning));

		PatternLayerManager manager = new PatternLayerManager(choices, new SimpleChromosome(3), 0, 1.0, false);

		System.out.println(PatternLayerManager.layerHeader());
		System.out.println(PatternLayerManager.layerString(manager.getTailElements()));

		for (int i = 0; i < 4; i++) {
			manager.addLayer(new ParameterSet(0.1, 0.2, 0.3));
			System.out.println(PatternLayerManager.layerString(manager.getTailElements()));
		}

		manager.updateDestination(destination);
		manager.sum(pos -> (int) (pos * bpm.l(16) * OutputLine.sampleRate),
				pos -> pos * bpm.l(4), 1, pos -> Scale.of(WesternChromatic.C1));

		WaveData out = new WaveData(destination, OutputLine.sampleRate);
		out.save(new File("results/pattern-layer-test.wav"));
	}
}
