/*
 * Copyright 2024 Michael Murray
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.audio.notes.TreeNoteSource;
import org.almostrealism.audio.pattern.ChordProgressionManager;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.NoteAudioChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audio.pattern.PatternLayerManager;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
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
import java.util.Objects;
import java.util.stream.Collectors;

public class PatternFactoryTest implements CellFeatures {

	public static String LIBRARY = "Library";

	static {
		String env = System.getenv("AR_RINGS_LIBRARY");
		if (env != null) LIBRARY = env;

		String arg = System.getProperty("AR_RINGS_LIBRARY");
		if (arg != null) LIBRARY = arg;
	}

	@Test
	public void fixChoices() throws IOException {
		List<NoteAudioChoice> choices = readChoices();
		new ObjectMapper().writeValue(new File("pattern-factory-new.json"), choices);
	}

	// @Test
	public void consolidateChoices() throws IOException {
		List<NoteAudioChoice> choices = readChoices();

		Map<String, List<File>> dirs = new HashMap<>();

		choices.forEach(c -> {
			dirs.put(c.getName().replaceAll(" ", "_"),
					c.getSources().stream().map(NoteAudioSource::getOrigin).map(File::new).collect(Collectors.toList()));
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

	public static List<NoteAudioChoice> createChoices() {
		List<NoteAudioChoice> choices = new ArrayList<>();

		NoteAudioChoice kick = new NoteAudioChoice(new PatternElementFactory("Kicks", NoteAudioProvider.create("Kit/Kick.wav")));
		kick.setSeed(true);
		kick.setMinScale(0.25);
		choices.add(kick);

		NoteAudioChoice clap = new NoteAudioChoice(new PatternElementFactory("Clap/Snare", NoteAudioProvider.create("Kit/Clap.wav")));
		clap.setMaxScale(0.5);
		choices.add(clap);

		NoteAudioChoice toms = new NoteAudioChoice(
				new PatternElementFactory("Toms", NoteAudioProvider.create("Kit/Tom1.wav"),
						NoteAudioProvider.create("Kit/Tom2.wav")));
		toms.setMaxScale(0.25);
		choices.add(toms);

		NoteAudioChoice hats = new NoteAudioChoice(new PatternElementFactory("Hats"));
		hats.setMaxScale(0.25);
		choices.add(hats);

		return choices;
	}

	public PatternFactoryChoiceList readChoices() throws IOException {
		return readChoices(true);
	}

	public PatternFactoryChoiceList readChoices(boolean useOld) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		File f = new File("pattern-factory.json.old");
		if (useOld && f.exists()) {
			return mapper.readValue(f, PatternFactoryChoiceList.class);
		} else {
			return mapper.readValue(new File("pattern-factory.json"), PatternFactoryChoiceList.class);
		}
	}

	@Test
	public void storeChoices() throws IOException {
		new ObjectMapper().writeValue(new File("pattern-factory.json"), createChoices());
	}

	@Test
	public void runLayers() throws IOException {
		Frequency bpm = bpm(120);

		int measures = 32;
		int beats = 4;
		double measureDuration = bpm.l(beats);
		double measureFrames = measureDuration * OutputLine.sampleRate;

		AudioScene.Settings settings = new ObjectMapper().readValue(new File("scene-settings.json"), AudioScene.Settings.class);

		FileWaveDataProviderNode library = new FileWaveDataProviderNode(new File(LIBRARY));

		List<NoteAudioChoice> choices = readChoices(false);
		choices.stream()
				.flatMap(c -> c.getSources().stream())
				.map(c -> c instanceof TreeNoteSource ? (TreeNoteSource) c : null)
				.filter(Objects::nonNull)
				.forEach(c -> c.setTree(library));

		DefaultKeyboardTuning tuning = new DefaultKeyboardTuning();

		choices.forEach(c -> {
			c.setTuning(tuning);
			c.setBias(1.0);
		});

		ChordProgressionManager chordProgression = new ChordProgressionManager();
		chordProgression.setSettings(settings.getChordProgression());
		chordProgression.refreshParameters();

		PatternLayerManager manager = new PatternLayerManager(choices, new SimpleChromosome(3), 3, 16.0, true);
		manager.setScaleTraversalDepth(3);

		double a = Math.random(); // 0.2;
		manager.addLayer(new ParameterSet(a, 0.3, 0.9));
		manager.addLayer(new ParameterSet(a, 0.1, 0.9));
		manager.addLayer(new ParameterSet(a, 0.1, 0.9));
//		manager.addLayer(new ParameterSet(0.2, 0.1, 0.9));

		System.out.println("a = " + a);

		AudioSceneContext context = new AudioSceneContext();
		context.setMeasures(measures);
		context.setFrames((int) (measures * measureFrames));
		context.setFrameForPosition(pos -> (int) (pos * measureFrames));
		context.setTimeForDuration(pos -> pos * measureDuration);
		context.setScaleForPosition(chordProgression::forPosition);

		manager.updateDestination(context);
		manager.sum(() -> context);

		WaveData out = new WaveData(manager.getDestination(), OutputLine.sampleRate);
		out.save(new File("results/pattern-layer-test.wav"));
	}
}
