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

import com.almostrealism.remote.api.GeneratorGrpc;
import com.almostrealism.remote.ops.GenerateRequestor;
import com.almostrealism.remote.ops.RefreshRequestor;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Channel;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RemoteGeneratorClient {
	private RemoteAccessKey key;
	private Channel channel;
	private GeneratorGrpc.GeneratorStub generator;

	private RefreshRequestor refresh;
	private GenerateRequestor generate;

	private Map<String, Consumer<Boolean>> refreshListeners;
	private Map<String, Consumer<WaveData>> generateListeners;
	private Map<String, AtomicInteger> completion;

	public RemoteGeneratorClient(String host, int port, RemoteAccessKey key) {
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(), key);
	}

	public RemoteGeneratorClient(ManagedChannelBuilder<?> channelBuilder, RemoteAccessKey key) {
		this.key = key;
		this.channel = channelBuilder.build();
		this.generator = GeneratorGrpc.newStub(channel);
		this.refreshListeners = new HashMap<>();
		this.generateListeners = new HashMap<>();
		this.completion = new HashMap<>();
	}

	public void refresh(String requestId, String generatorId, List<PatternNoteSource> sources, Consumer<Boolean> success) {
		ensureRefresh();

		System.out.println("RemoteGeneratorClient: Submitting refresh request " + requestId);

		refreshListeners.put(requestId, success);
		completion.put(requestId, new AtomicInteger(1));
		refresh.submit(requestId, generatorId,
				sources.stream()
						.map(PatternNoteSource::getNotes)
						.flatMap(List::stream)
						.map(PatternNote::getAudio)
						.filter(Objects::nonNull)
						.map(c -> new WaveData(c, OutputLine.sampleRate))
						.collect(Collectors.toList()));
	}

	public void generate(String requestId, String generatorId, int count, Consumer<WaveData> output) {
		ensureGenerate();

		System.out.println("RemoteGeneratorClient: Submitting generate request " + requestId);

		generateListeners.put(requestId, output);
		completion.put(requestId, new AtomicInteger(count));
		generate.submit(requestId, generatorId, count);
	}

	private void ensureRefresh() {
		if (refresh == null) {
			refresh = new RefreshRequestor(key, generator, this::deliver);
		}
	}

	private void ensureGenerate() {
		if (generate == null) {
			generate = new GenerateRequestor(key, generator, this::deliver);
		}
	}

	protected void deliver(String requestId, boolean success) {
		if (!refreshListeners.containsKey(requestId)) {
			System.out.println("WARN: No listener for request " + requestId);
			return;
		}

		refreshListeners.get(requestId).accept(success);
		if (completion.get(requestId).decrementAndGet() <= 0) {
			refreshListeners.remove(requestId);
			completion.remove(requestId);
			System.out.println("RemoteGeneratorClient: Finished receiving results for " + requestId);
		}
	}

	protected void deliver(String requestId, int index, WaveData data) {
		if (!generateListeners.containsKey(requestId)) {
			System.out.println("WARN: No listener for request " + requestId);
			return;
		}

		generateListeners.get(requestId).accept(data);
		if (completion.get(requestId).decrementAndGet() <= 0) {
			generateListeners.remove(requestId);
			completion.remove(requestId);
			System.out.println("RemoteGeneratorClient: Finished receiving results for " + requestId);
		}
	}

	public void destroy() {
		if (generate != null) generate.destroy();
	}
}
