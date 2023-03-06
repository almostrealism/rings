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

package com.almostrealism.remote;

import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.GeneratorStatus;
import org.almostrealism.audio.notes.PatternNoteSource;

import java.util.List;

public class RemoteGeneratorProvider implements GenerationProvider {

	@Override
	public void refresh(String id, List<PatternNoteSource> sources) {
		// TODO Auto-generated method stub

	}

	@Override
	public GeneratorStatus getStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PatternNoteSource> generate(String id, int count) {
		// TODO Auto-generated method stub
		return null;
	}
}
