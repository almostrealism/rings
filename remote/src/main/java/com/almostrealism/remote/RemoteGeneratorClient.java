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
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;

import java.util.ArrayList;
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
	private Map<String, Runnable> refreshEndListeners;

	private Map<String, Consumer<WaveData>> generateListeners;
	private Map<String, Runnable> generateEndListeners;

	private Map<String, AtomicInteger> completion;

	public RemoteGeneratorClient(String host, int port, RemoteAccessKey key) {
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(), key);
	}

	public RemoteGeneratorClient(ManagedChannelBuilder<?> channelBuilder, RemoteAccessKey key) {
		this.key = key;
		this.channel = channelBuilder.build();
		this.generator = GeneratorGrpc.newStub(channel);
		this.refreshListeners = new HashMap<>();
		this.refreshEndListeners = new HashMap<>();
		this.generateListeners = new HashMap<>();
		this.generateEndListeners = new HashMap<>();
		this.completion = new HashMap<>();
	}

	public boolean refresh(String requestId, String generatorId, List<NoteAudioSource> sources, Consumer<Boolean> success, Runnable end) {
		ensureRefresh();

		List<WaveData> data = sources.stream()
				.map(NoteAudioSource::getNotes)
				.flatMap(List::stream)
				.filter(NoteAudioProvider::isValid)
				.map(NoteAudioProvider::getAudio)
				.filter(Objects::nonNull)
				.map(c -> new WaveData(c, OutputLine.sampleRate))
				.collect(Collectors.toList());

		if (data.isEmpty()) return false;

		System.out.println("RemoteGeneratorClient: Submitting refresh request " + requestId);

		refreshListeners.put(requestId, success);
		refreshEndListeners.put(requestId, end);
		completion.put(requestId, new AtomicInteger(1));
		refresh.submit(requestId, generatorId, data);
		return true;
	}

	public void generate(String requestId, String generatorId, int count, Consumer<WaveData> output, Runnable end) {
		ensureGenerate();

		System.out.println("RemoteGeneratorClient: Submitting generate request " + requestId);

		generateListeners.put(requestId, output);
		generateEndListeners.put(requestId, end);
		completion.put(requestId, new AtomicInteger(count));
		generate.submit(requestId, generatorId, count);
	}

	private void ensureRefresh() {
		if (refresh == null) {
			refresh = new RefreshRequestor(key, generator, this::deliver, this::refreshEnd);
		}
	}

	private void refreshDone(String requestId) {
		refreshListeners.remove(requestId);
		refreshEndListeners.remove(requestId);
		completion.remove(requestId);
		System.out.println("RemoteGeneratorClient: Finished receiving results for " + requestId);
	}

	private void refreshEnd() {
		refresh = null;

		List<String> all = new ArrayList<>();
		all.addAll(refreshEndListeners.keySet());

		all.stream().map(refreshEndListeners::get).forEach(Runnable::run);
		all.forEach(this::refreshDone);
	}

	private void ensureGenerate() {
		if (generate == null) {
			generate = new GenerateRequestor(key, generator, this::deliver, this::generateEnd);
		}
	}

	private void generateDone(String requestId) {
		generateListeners.remove(requestId);
		generateEndListeners.remove(requestId);
		completion.remove(requestId);
		System.out.println("RemoteGeneratorClient: Finished receiving results for " + requestId);
	}

	private void generateEnd() {
		generate = null;

		List<String> all = new ArrayList<>();
		all.addAll(generateEndListeners.keySet());

		all.stream().map(generateEndListeners::get).forEach(Runnable::run);
		all.forEach(this::generateDone);
	}

	protected void deliver(String requestId, boolean success) {
		if (!refreshListeners.containsKey(requestId)) {
			System.out.println("WARN: No listener for request " + requestId);
			return;
		}

		refreshListeners.get(requestId).accept(success);
		if (completion.get(requestId).decrementAndGet() <= 0) {
			refreshDone(requestId);
		}
	}

	protected void deliver(String requestId, int index, WaveData data) {
		if (!generateListeners.containsKey(requestId)) {
			System.out.println("WARN: No listener for request " + requestId);
			return;
		}

		generateListeners.get(requestId).accept(data);
		if (completion.get(requestId).decrementAndGet() <= 0) {
			generateDone(requestId);
		}
	}

	public void destroy() {
		if (refresh != null) refresh.destroy();
		if (generate != null) generate.destroy();
	}
}
