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

import com.almostrealism.remote.api.Generation;
import com.almostrealism.remote.api.GeneratorGrpc;
import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.data.WaveData;

public class GenerateRequestor implements StreamObserver<Generation.Output> {
	private GeneratorGrpc.GeneratorStub generator;
	private StreamObserver<Generation.GeneratorRequest> requestStream;

	private WaveDataAccumulator accumulator;

	public GenerateRequestor(GeneratorGrpc.GeneratorStub generator, Receiver deliver) {
		this.generator = generator;
		this.accumulator = new WaveDataAccumulator((id, data) -> {
			String key[] = id.split(":");
			deliver.receive(key[0], Integer.parseInt(key[1]), data);
		});
	}

	public void submit(String requestId, String generatorId, int count) {
		ensureRequestStream();

		Generation.GeneratorRequest request = Generation.GeneratorRequest.newBuilder()
				.setRequestId(requestId)
				.setGeneratorId(generatorId)
				.setCount(count)
				.build();

		requestStream.onNext(request);
	}

	protected void ensureRequestStream() {
		if (requestStream == null) {
			requestStream = generator.generate(this);
		}
	}

	@Override
	public void onNext(Generation.Output output) {
		accumulator.process(output.getRequestId() + ":" + output.getIndex(), output.getSegment());
	}

	@Override
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	@Override
	public void onCompleted() {
		System.out.println("GenerateRequestor: Completed");
		requestStream.onCompleted();
		requestStream = null;
	}

	public void destroy() {
		if (requestStream != null) {
			requestStream.onCompleted();
			requestStream = null;
		}
	}

	public interface Receiver {
		void receive(String requestId, int index, WaveData data);
	}
}
