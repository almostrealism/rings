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

package org.almostrealism.audio.generative;

import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.notes.PatternNoteSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoOpGenerationProvider implements GenerationProvider {
	private Map<String, List<PatternNoteSource>> sources;

	public NoOpGenerationProvider() {
		sources = new HashMap<>();
	}

	@Override
	public void refresh(String id, List<PatternNoteSource> sources) {
		this.sources.put(id, sources);
	}

	@Override
	public GeneratorStatus getStatus(String id) {
		return sources.containsKey(id) ? GeneratorStatus.READY : GeneratorStatus.NONE;
	}

	@Override
	public List<PatternNoteSource> generate(String requestId, String generatorId, int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> sources.get(generatorId).get(i % sources.get(generatorId).size()))
				.collect(Collectors.toList());
	}

	@Override
	public int getSampleRate() {
		return OutputLine.sampleRate;
	}
}