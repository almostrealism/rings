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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerationProviderQueue {
	private ExecutorService executor;
	private GenerationProvider provider;

	public GenerationProviderQueue(GenerationProvider provider) {
		this.executor = Executors.newSingleThreadExecutor();
		this.provider = provider;
	}

	public GenerationProvider getProvider() {
		return provider;
	}

	public void submit(Operation op) {
		executor.submit(() -> op.accept(provider));
	}
}
