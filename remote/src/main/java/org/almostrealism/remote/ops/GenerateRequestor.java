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

package org.almostrealism.remote.ops;

import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.remote.RemoteAccessKey;
import org.almostrealism.remote.api.Generation;
import org.almostrealism.remote.api.GeneratorGrpc;

public class GenerateRequestor implements StreamObserver<Generation.Output> {
	private final RemoteAccessKey key;
	private final GeneratorGrpc.GeneratorStub generator;
	private StreamObserver<Generation.GeneratorRequest> requestStream;
	private final Runnable end;

	private final WaveDataAccumulator accumulator;

	public GenerateRequestor(RemoteAccessKey key, GeneratorGrpc.GeneratorStub generator, Receiver deliver, Runnable end) {
		this.key = key;
		this.generator = generator;
		this.accumulator = new WaveDataAccumulator((id, data) -> {
			String[] k = id.split(":");
			deliver.receive(k[0], Integer.parseInt(k[1]), data);
		});
		this.end = end;
	}

	public void submit(String requestId, String generatorId, int count) {
		ensureRequestStream();

		Generation.GeneratorRequest request = Generation.GeneratorRequest.newBuilder()
				.setAccessKey(key())
				.setRequestId(requestId)
				.setGeneratorId(generatorId)
				.setCount(count)
				.build();

		requestStream.onNext(request);
	}

	protected Generation.AccessKey key() {
		return Generation.AccessKey.newBuilder()
				.setKey(key.getKey())
				.setUserId(key.getUserId())
				.setToken(key.getToken())
				.build();
	}

	protected synchronized void ensureRequestStream() {
		if (requestStream == null) {
			System.out.println("GenerateRequestor: Creating request stream...");
			requestStream = generator.generate(this);
			System.out.println("GenerateRequestor: Request stream created");
		}
	}

	@Override
	public void onNext(Generation.Output output) {
		accumulator.process(output.getRequestId() + ":" + output.getIndex(), output.getSegment());
	}

	@Override
	public void onError(Throwable e) {
		e.printStackTrace();
		requestStream = null;
		System.out.println("GenerateRequestor: Running end callback");
		end.run();
	}

	@Override
	public void onCompleted() {
		System.out.println("GenerateRequestor: Completed");
		requestStream.onCompleted();
		requestStream = null;
		end.run();
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
