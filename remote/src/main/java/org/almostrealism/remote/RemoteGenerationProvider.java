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

package org.almostrealism.remote;

import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.GenerationResourceManager;
import org.almostrealism.audio.generative.GeneratorStatus;
import org.almostrealism.audio.notes.NoteAudio;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteGenerationProvider implements GenerationProvider {
	private RemoteGeneratorClient client;
	private GenerationResourceManager resources;

	public RemoteGenerationProvider(String host, int port, RemoteAccessKey key, GenerationResourceManager resources) {
		this.client = new RemoteGeneratorClient(host, port, key);
		this.resources = resources;
	}

	@Override
	public boolean refresh(String requestId, String generatorId, List<NoteAudio> sources) {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean success = new AtomicBoolean(false);

		boolean valid = client.refresh(requestId, generatorId, sources, s -> {
			success.set(s);
			latch.countDown();
		}, () -> { while (latch.getCount() > 0) latch.countDown(); });

		if (!valid) return false;

		try {
			System.out.println("Awaiting status...");
			latch.await();
			System.out.println("RemoteGenerationProvider: Done");
			return success.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GeneratorStatus getStatus(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<NoteAudio> generate(String requestId, String generatorId, int count) {
		List<NoteAudio> results = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(count);

		client.generate(requestId, generatorId, count, wave -> {
			System.out.println("RemoteGeneratorProvider: Store result " + results.size());
			results.add(resources.storeAudio(KeyUtils.generateKey(), wave));
			latch.countDown();
		}, () -> { while (latch.getCount() > 0) latch.countDown(); });

		try {
			System.out.println("Awaiting results...");
			latch.await();
			System.out.println("RemoteGenerationProvider: Returning " + results.size() + " results");
			return results;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getSampleRate() {
		throw new UnsupportedOperationException();
	}

	public void destroy() {
		client.destroy();
	}
}
