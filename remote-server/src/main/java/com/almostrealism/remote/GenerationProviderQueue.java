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

import com.almostrealism.remote.ops.Operation;
import org.almostrealism.audio.generative.GenerationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerationProviderQueue {
	private ExecutorService executor;
	private GenerationProvider provider;
	private List<String> currentRequests;

	public GenerationProviderQueue(GenerationProvider provider) {
		this.executor = Executors.newSingleThreadExecutor();
		this.provider = provider;
		this.currentRequests = new ArrayList<>();
	}

	public GenerationProvider getProvider() {
		return provider;
	}

	public synchronized void submit(Operation op) {
		if (op.getRequestId() == null || "".equals(op.getRequestId())) {
			throw new IllegalArgumentException("Request must have a non-empty ID");
		}

		if (currentRequests.contains(op.getRequestId())) {
			System.out.println("GenerationProviderQueue: Request \"" + op.getRequestId() + "\" already being processed");
			return;
		}

		currentRequests.add(op.getRequestId());
		executor.submit(() -> {
			try {
				op.accept(provider);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				currentRequests.remove(op.getRequestId());
			}
		});
	}
}
