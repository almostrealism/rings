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

package com.almostrealism.remote.ops;

import com.almostrealism.remote.GenerationProviderQueue;
import com.almostrealism.remote.api.Generation;
import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.generative.LocalResourceManager;

import java.util.List;

public class RemoteRefresh implements StreamObserver<Generation.SourceData> {
	private final GenerationProviderQueue queue;
	private final StreamObserver<Generation.Status> reply;

	private LocalResourceManager resources;

	public RemoteRefresh(GenerationProviderQueue queue, StreamObserver<Generation.Status> reply) {
		this.queue = queue;
		this.reply = reply;
	}

	@Override
	public void onNext(Generation.SourceData value) {
		System.out.println("Received source data: " + value);
	}

	@Override
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@Override
	public void onCompleted() {
		System.out.println("Completed");
		reply.onCompleted();
	}

	public static class RefreshOperation implements Operation {
		private String id;
		private List<PatternNoteSource> sources;

		public RefreshOperation(String id, List<PatternNoteSource> sources) {
			this.id = id;
			this.sources = sources;
		}

		@Override
		public void accept(GenerationProvider provider) {
			provider.refresh(id, sources);
		}
	}
}