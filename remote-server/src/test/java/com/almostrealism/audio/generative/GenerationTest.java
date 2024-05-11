/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.audio.generative;

import com.almostrealism.remote.AccessManager;
import com.almostrealism.remote.RemoteAccessKey;
import com.almostrealism.remote.RemoteGenerationServer;
import com.almostrealism.remote.RemoteGenerationProvider;
import com.almostrealism.remote.mgr.DefaultAccessManager;
import com.almostrealism.remote.mgr.ManagerDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.PatternFactoryChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audioml.DiffusionGenerationProvider;
import org.almostrealism.audio.generative.LocalResourceManager;
import org.almostrealism.util.KeyUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GenerationTest {
	public static final String ROOT = "/Users/michael/AlmostRealism/";

	protected LocalResourceManager resources(String prefix) {
		return new LocalResourceManager(
				new File(ROOT + prefix + "-models"), new File(ROOT + prefix + "-audio"));
	}

	protected AccessManager accessManager() {
		return new DefaultAccessManager(ManagerDatabase.load(new File(ROOT, "rings-db.json")));
	}

	protected DiffusionGenerationProvider provider() {
		return new DiffusionGenerationProvider(resources("remote"));
	}

	public void startServer() throws IOException {
		RemoteGenerationServer server = new RemoteGenerationServer(accessManager(), provider(), 6566);
		server.start();
	}

	@Test
	public void train() throws IOException {
		List<PatternFactoryChoice> choices =
				new ObjectMapper().readValue(new File(ROOT + "ringsdesktop/pattern-factory.json"),
					PatternFactoryChoiceList.class);

		List<NoteAudioSource> sources = choices.stream()
				.map(PatternFactoryChoice::getFactory)
				// .filter(c -> "Hats".equals(c.getName()))
				.map(PatternElementFactory::getSources)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		DiffusionGenerationProvider provider = provider();
		provider.refresh(KeyUtils.generateKey(), "test9-100", sources);
		provider.generate(KeyUtils.generateKey(), "test9-100", 25);
	}

	@Test
	public void trainRemote() throws IOException {
		List<PatternFactoryChoice> choices =
				new ObjectMapper().readValue(new File(ROOT + "ringsdesktop/pattern-factory.json"),
						PatternFactoryChoiceList.class);

		List<NoteAudioSource> sources = choices.stream()
				.map(PatternFactoryChoice::getFactory)
				// .filter(c -> "Hats".equals(c.getName()))
				.map(PatternElementFactory::getSources)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		RemoteGenerationProvider provider = new RemoteGenerationProvider(
				"localhost", 6566,
				RemoteAccessKey.load("rings-key.json"),
				resources("local"));

		provider.refresh(KeyUtils.generateKey(), "test9", sources);
		provider.generate(KeyUtils.generateKey(), "test9", 25);
	}

	@Test
	public void generate() throws IOException {
		startServer();

		RemoteGenerationProvider provider = new RemoteGenerationProvider(
										"localhost", 6566,
										RemoteAccessKey.load("rings-key.json"),
										resources("local"));

		String req = "test123";
		List<NoteAudioSource> result = provider.generate(req, "test5", 15);
		System.out.println("GenerationTest: Received " + result.size() + " results");
	}
}
