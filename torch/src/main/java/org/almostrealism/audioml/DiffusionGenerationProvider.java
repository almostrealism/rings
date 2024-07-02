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

package org.almostrealism.audioml;

import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.GenerationResourceManager;
import org.almostrealism.audio.generative.GeneratorStatus;
import org.almostrealism.audio.notes.NoteAudioSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiffusionGenerationProvider implements GenerationProvider {
	public static final int SAMPLE_RATE = 44100;

	private TorchDiffusion model;
	private GenerationResourceManager resources;

	public DiffusionGenerationProvider(GenerationResourceManager resources) {
		this.model = new TorchDiffusion();
		this.resources = resources;
	}

	@Override
	public boolean refresh(String requestId, String generatorId, List<NoteAudioSource> sources) {
		try {
			if (resources.isModelVersionAvailable(requestId)) {
				// If the model was already refreshed, just return true
				return true;
			}

			model.clearDatasets();
			model.clearModel();

			model.loadAudio(sources);
			model.train();
			resources.storeModel(generatorId, requestId, new File("models/latest.zip"));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public GeneratorStatus getStatus(String id) {
		return resources.isModelAvailable(id) ? GeneratorStatus.READY : GeneratorStatus.NONE;
	}

	@Override
	public List<NoteAudioSource> generate(String requestId, String generatorId, int count) {
		List<NoteAudioSource> existing = new ArrayList<>();

		boolean available = true;

		i: for (int i = 0; i < count; i++) {
			NoteAudioSource out = resources.getAudio(requestId + ":" + i);
			if (out == null) {
				available = false;
				break i;
			} else {
				existing.add(out);
			}
		}

		if (available) {
			System.out.println("DiffusionGenerationProvider: Request " + requestId + " already completed");
			return existing;
		}

		resources.loadModel(generatorId, new File("models/latest.zip"));
		model.generate(count);
		return IntStream.range(0, count).mapToObj(i ->
			resources.storeAudio(requestId + ":" + i, new File("output/" + i + ".wav")))
				.collect(Collectors.toList());
	}

	@Override
	public int getSampleRate() {
		return SAMPLE_RATE;
	}
}
