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

import org.almostrealism.audio.notes.NoteAudioSource;

import java.util.List;

public interface GenerationProvider {
	boolean refresh(String requestId, String generatorId, List<NoteAudioSource> sources);

	GeneratorStatus getStatus(String id);

	List<NoteAudioSource> generate(String requestId, String generatorId, int count);

	int getSampleRate();
}
