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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.audio.notes.NoteSourceProvider;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;

public class Generator {
	private String id;
	private String name;
	private State state;

	private List<String> sources;
	private List<PatternNoteSource> results;

	private NoteSourceProvider sourceProvider;
	private GenerationProvider generationProvider;

	public Generator() {
		this(KeyUtils.generateKey(), "Generator", State.NONE);
	}

	public Generator(String id, String name, State state) {
		this.id = id;
		this.name = name;
		this.state = state;
		this.sources = new ArrayList<>();
		this.results = new ArrayList<>();
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public State getState() { return state; }
	public void setState(State state) { this.state = state; }

	public List<String> getSources() { return sources; }
	public void setSources(List<String> sources) { this.sources = sources; }

	public List<PatternNoteSource> getResults() { return results; }
	public void setResults(List<PatternNoteSource> results) { this.results = results; }

	@JsonIgnore
	public NoteSourceProvider getSourceProvider() { return sourceProvider; }

	@JsonIgnore
	public void setSourceProvider(NoteSourceProvider sourceProvider) { this.sourceProvider = sourceProvider; }

	@JsonIgnore
	public GenerationProvider getGenerationProvider() { return generationProvider; }

	@JsonIgnore
	public void setGenerationProvider(GenerationProvider provider) { this.generationProvider = provider; }

	public void generate(int count) {
		if (state != State.READY) {
			throw new IllegalStateException("Generator is not ready");
		}

		state = State.GENERATING;
		List<PatternNoteSource> results = generationProvider.generate(KeyUtils.generateKey(), id, count);
		if (results != null) results.addAll(results);
		state = State.READY;
	}

	public enum State {
		NONE, REFRESHING, READY, GENERATING;

		public String getName() {
			switch (this) {
			case NONE:
				return "None";
			case REFRESHING:
				return "Refreshing";
			case READY:
				return "Ready";
			case GENERATING:
				return "Generating";
			default:
				return "Unknown";
			}
		}
	}
}
