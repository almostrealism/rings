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

package com.almostrealism.remote.ops;

import com.almostrealism.remote.AccessManager;
import com.almostrealism.remote.GenerationProviderQueue;
import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.notes.NoteAudio;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.remote.api.Generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class RemoteRefresh implements StreamObserver<Generation.RefreshRequest>, ConsoleFeatures {
	private final AccessManager accessManager;
	private final GenerationProviderQueue queue;
	private final StreamObserver<Generation.Status> reply;

	private final Map<String, RefreshOperation> operations;

	public RemoteRefresh(AccessManager accessManager,
						 GenerationProviderQueue queue,
						 StreamObserver<Generation.Status> reply) {
		this.accessManager = accessManager;
		this.queue = queue;
		this.reply = reply;
		this.operations = new HashMap<>();
	}

	@Override
	public void onNext(Generation.RefreshRequest value) {
		if (accessManager.authorize(value.getAccessKey(), value.getRequestId())) {
			operations.computeIfAbsent(value.getRequestId(),
					(key) -> new RefreshOperation(value.getRequestId(), value.getGeneratorId(),
							success -> respond(value.getRequestId(), value.getGeneratorId(), success))).append(value);

			if (value.getIsFinal()) {
				System.out.println("RemoteRefresh: Submitting refresh operation to queue...");
				queue.submit(operations.remove(value.getRequestId()));
			}
		} else {
			System.out.println("Access denied for user \"" + value.getAccessKey().getUserId() + "\"");
		}
	}

	protected void respond(String requestId, String generatorId, boolean success) {
		System.out.println("RemoteRefresh: Sending status for request " + requestId + "...");
		reply.onNext(Generation.Status.newBuilder()
				.setRequestId(requestId)
				.setGeneratorId(generatorId)
				.setState(success ? Generation.State.FINISHED : Generation.State.FAILED)
				.build());
	}

	@Override
	public void onError(Throwable t) {
		t.printStackTrace();
		reply.onCompleted();
	}

	@Override
	public void onCompleted() {
		System.out.println("Completed");
		reply.onCompleted();
	}

	public static class RefreshOperation implements Operation, ConsoleFeatures {
		private final String requestId;
		private final String generatorId;

		private final List<NoteAudio> sources;
		private PackedCollection currentSource;
		private int currentIndex;

		private final Consumer<Boolean> success;

		public RefreshOperation(String requestId, String generatorId, Consumer<Boolean> success) {
			this.requestId = requestId;
			this.generatorId = generatorId;
			this.sources = new ArrayList<>();
			this.success = success;
		}

		@Override
		public String getRequestId() {
			return requestId;
		}

		public String getGeneratorId() { return generatorId; }

		public List<NoteAudio> getSources() { return sources; }

		public void append(Generation.RefreshRequest request) {
			if (!Objects.equals(request.getRequestId(), requestId))
				throw new IllegalArgumentException();

			if (!Objects.equals(request.getGeneratorId(), generatorId))
				throw new IllegalArgumentException();

			if (request.getSource().getSegment().getSampleRate() != OutputLine.sampleRate)
				throw new IllegalArgumentException();

			if (currentSource == null) {
				currentSource = new PackedCollection(request.getSource().getSegment().getTotalSamples());
			}

			currentSource.setMem(currentIndex,
					request.getSource().getSegment().getDataList().stream().mapToDouble(d -> d).toArray());
			currentIndex += request.getSource().getSegment().getDataList().size();

			if (request.getSource().getSegment().getIsFinal()) {
				log("Adding source (" + currentSource.getMemLength() + " samples)");
				sources.add(NoteAudioProvider.create(() -> currentSource));
				currentSource = null;
				currentIndex = 0;
			}
		}

		@Override
		public void accept(GenerationProvider provider) {
			success.accept(provider.refresh(requestId, generatorId, sources));
		}
	}
}
