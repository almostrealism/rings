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

import com.almostrealism.remote.api.Generation;
import com.almostrealism.remote.api.GeneratorGrpc;
import com.almostrealism.remote.ops.RemoteGenerate;
import com.almostrealism.remote.ops.RemoteRefresh;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audio.generative.GenerationResourceManager;

public class RemoteGenerationService extends GeneratorGrpc.GeneratorImplBase {
	private AccessManager accessManager;
	private GenerationProviderQueue queue;

	public RemoteGenerationService(AccessManager accessManager,
								   GenerationProvider provider) {
		this.accessManager = accessManager;
		this.queue = new GenerationProviderQueue(provider);
	}

	@Override
	public StreamObserver<Generation.RefreshRequest> refresh(StreamObserver<Generation.Status> responseObserver) {
		if (responseObserver instanceof ServerCallStreamObserver)
			((ServerCallStreamObserver) responseObserver).setOnCancelHandler(() -> { });

		return new RemoteRefresh(accessManager, queue, responseObserver);
	}

	@Override
	public StreamObserver<Generation.GeneratorRequest> generate(StreamObserver<Generation.Output> responseObserver) {
		return new RemoteGenerate(accessManager, queue, responseObserver);
	}
}
