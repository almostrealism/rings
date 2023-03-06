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
import org.almostrealism.audio.notes.PatternNoteSource;

import java.util.List;

public class Generator {
	private String id;
	private List<PatternNoteSource> sources;
	private GenerationProvider provider;

	public Generator() { }

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public List<PatternNoteSource> getSources() { return sources; }
	public void setSources(List<PatternNoteSource> sources) { this.sources = sources; }

	@JsonIgnore
	public GenerationProvider getProvider() { return provider; }

	@JsonIgnore
	public void setProvider(GenerationProvider provider) { this.provider = provider; }
}
