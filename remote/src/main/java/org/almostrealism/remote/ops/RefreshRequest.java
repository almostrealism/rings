/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.remote.ops;

import org.almostrealism.audio.notes.NoteAudio;

import java.util.List;

public class RefreshRequest {
	private String requestId;
	private String generatorId;
	private List<NoteAudio> sources;
	private boolean complete;
	private boolean error;

	public RefreshRequest() { }

	public RefreshRequest(String requestId, String generatorId, List<NoteAudio> sources) {
		this.requestId = requestId;
		this.generatorId = generatorId;
		this.sources = sources;
	}

	public String getRequestId() { return requestId; }
	public void setRequestId(String requestId) { this.requestId = requestId; }

	public String getGeneratorId() { return generatorId; }
	public void setGeneratorId(String generatorId) { this.generatorId = generatorId; }

	public List<NoteAudio> getSources() { return sources; }
	public void setSources(List<NoteAudio> sources) { this.sources = sources; }

	public boolean isComplete() { return complete; }
	public void setComplete(boolean complete) { this.complete = complete; }

	public boolean isError() { return error; }
	public void setError(boolean error) { this.error = error; }
}
