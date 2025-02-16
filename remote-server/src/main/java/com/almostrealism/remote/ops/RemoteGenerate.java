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

import com.almostrealism.remote.AccessManager;
import com.almostrealism.remote.GenerationProviderQueue;
import org.almostrealism.remote.api.Generation;
import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.remote.ops.WaveDataPublisher;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class RemoteGenerate implements StreamObserver<Generation.GeneratorRequest> {
	public static final int MAX_GENERATION_COUNT = 40;

	private final AccessManager accessManager;
	private final GenerationProviderQueue queue;
	private final StreamObserver<Generation.Output> reply;
	private WaveDataPublisher publisher;

	public RemoteGenerate(AccessManager accessManager, GenerationProviderQueue queue, StreamObserver<Generation.Output> reply) {
		this.accessManager = accessManager;
		this.queue = queue;
		this.reply = reply;
		this.publisher = new WaveDataPublisher();
	}

	@Override
	public void onNext(Generation.GeneratorRequest value) {
		System.out.println("Received generator request: " + value.getRequestId() + " for generator " + value.getGeneratorId());
		if (accessManager.authorize(value.getAccessKey(), value.getRequestId())) {
			queue.submit(new GenerationOperation(value.getRequestId(), value.getGeneratorId(),
					value.getCount() < MAX_GENERATION_COUNT ? value.getCount() : MAX_GENERATION_COUNT,
					results -> output(value.getRequestId(), value.getGeneratorId(), results)));
		} else {
			System.out.println("Access denied for user \"" + value.getAccessKey().getUserId() + "\"");
		}
	}

	protected void output(String requestId, String generatorId, List<NoteAudioSource> results) {
		if (results == null) {
			System.out.println("RemoteGenerate: Generation failed");
			// TODO  Send back status info to client
			return;
		}

		System.out.println("RemoteGenerate: Sending " + results.size() + " results");
		IntStream.range(0, results.size()).forEach(i -> output(requestId, generatorId, i, results.get(i)));
	}

	protected void output(String requestId, String generatorId, int index, NoteAudioSource result) {
		if (result.getNotes().size() != 1) throw new UnsupportedOperationException();
		output(requestId, generatorId, index, result.getNotes().get(0));
	}

	protected void output(String requestId, String generatorId, int index, NoteAudioProvider note) {
		if (note.getAudio() == null) {
			System.out.println("RemoteGenerate: Empty result will not be published");
			return;
		}

		publisher.publish(new WaveData(note.getAudio(), queue.getProvider().getSampleRate()), audio -> {
			Generation.Output.Builder builder = Generation.Output.newBuilder();
			builder.setRequestId(requestId);
			builder.setGeneratorId(generatorId);
			builder.setIndex(index);
			builder.setSegment(audio);
			reply.onNext(builder.build());
		});
	}

	@Override
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@Override
	public void onCompleted() {
		System.out.println("RemoteGenerate: Stream completed");
		reply.onCompleted();
	}

	public static class GenerationOperation implements Operation {
		private String requestId;
		private String generatorId;
		private int count;
		private Consumer<List<NoteAudioSource>> results;

		public GenerationOperation(String requestId, String generatorId, int count, Consumer<List<NoteAudioSource>> results) {
			this.requestId = requestId;
			this.generatorId = generatorId;
			this.count = count;
			this.results = results;
		}

		@Override
		public String getRequestId() {
			return requestId;
		}

		@Override
		public void accept(GenerationProvider provider) {
			results.accept(provider.generate(requestId, generatorId, count));
		}
	}
}
