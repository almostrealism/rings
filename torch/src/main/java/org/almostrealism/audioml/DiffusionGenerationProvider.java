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
import org.almostrealism.audio.generative.GeneratorStatus;
import org.almostrealism.audio.notes.PatternNoteSource;

import java.io.File;
import java.util.List;

public class DiffusionGenerationProvider implements GenerationProvider {
	private TorchDiffusion model;
	private GenerationResourceManager resources;

	public DiffusionGenerationProvider(GenerationResourceManager resources) {
		this.model = new TorchDiffusion();
		this.resources = resources;
	}

	@Override
	public void refresh(String id, List<PatternNoteSource> sources) {
		model.clearDatasets();
		model.loadAudio(sources);
		model.train();
		resources.store(id, new File("models/latest.zip"));
	}

	@Override
	public GeneratorStatus getStatus(String id) {
		return resources.isAvailable(id) ? GeneratorStatus.READY : GeneratorStatus.NONE;
	}

	@Override
	public List<PatternNoteSource> generate(String id, int count) {
		return null;
	}
}
