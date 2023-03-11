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
import io.grpc.ManagedChannelBuilder;
import io.grpc.Channel;
import org.almostrealism.audio.data.WaveData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RemoteGeneratorClient {
	private Channel channel;
	private GeneratorGrpc.GeneratorStub generator;
	private GenerateRequestor generate;

	private Map<String, Consumer<WaveData>> listeners;
	private Map<String, AtomicInteger> completion;

	public RemoteGeneratorClient(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
	}

	public RemoteGeneratorClient(ManagedChannelBuilder<?> channelBuilder) {
		channel = channelBuilder.build();
		generator = GeneratorGrpc.newStub(channel);
		listeners = new HashMap<>();
		completion = new HashMap<>();
	}

	public void generate(String requestId, String generatorId, int count, Consumer<WaveData> output) {
		ensureGenerate();

		System.out.println("RemoteGeneratorClient: Submitting " + requestId);

		listeners.put(requestId, output);
		completion.put(requestId, new AtomicInteger(count));
		generate.submit(requestId, generatorId, count);
	}

	private void ensureGenerate() {
		if (generate == null) {
			generate = new GenerateRequestor(generator, this::deliver);
		}
	}

	protected void deliver(String requestId, int index, WaveData data) {
		if (!listeners.containsKey(requestId)) {
			System.out.println("WARN: No listener for request " + requestId);
			return;
		}

		listeners.get(requestId).accept(data);
		if (completion.get(requestId).decrementAndGet() <= 0) {
			listeners.remove(requestId);
			completion.remove(requestId);
			System.out.println("RemoteGeneratorClient: Finished receiving results for " + requestId);
		}
	}
}
